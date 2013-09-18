/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.metastore;

import com.facebook.hive.metastore.api.ThriftHiveMetastore;
import com.facebook.hive.metastore.client.HiveMetastoreClientConfig;
import com.facebook.hive.metastore.client.HiveMetastoreFactory;
import com.facebook.hive.metastore.client.SimpleHiveMetastoreFactory;
import com.facebook.swift.service.ThriftClientConfig;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import com.google.common.net.HostAndPort;

import io.airlift.log.Logger;
import io.airlift.units.Duration;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.ColumnStatistics;
import org.apache.hadoop.hive.metastore.api.ConfigValSecurityException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.HiveObjectPrivilege;
import org.apache.hadoop.hive.metastore.api.HiveObjectRef;
import org.apache.hadoop.hive.metastore.api.Index;
import org.apache.hadoop.hive.metastore.api.InvalidInputException;
import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
import org.apache.hadoop.hive.metastore.api.InvalidOperationException;
import org.apache.hadoop.hive.metastore.api.InvalidPartitionException;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PartitionEventType;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
import org.apache.hadoop.hive.metastore.api.Role;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.Type;
import org.apache.hadoop.hive.metastore.api.UnknownDBException;
import org.apache.hadoop.hive.metastore.api.UnknownPartitionException;
import org.apache.hadoop.hive.metastore.api.UnknownTableException;
import org.apache.hadoop.hive.shims.HadoopShims;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import javax.security.auth.login.LoginException;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.apache.hadoop.hive.metastore.MetaStoreUtils.DEFAULT_DATABASE_NAME;
import static org.apache.hadoop.hive.metastore.MetaStoreUtils.isIndexTable;

/**
 * Hive Metastore Client.
 */
public class HiveMetaStoreClient implements IMetaStoreClient, Closeable {
    private final HiveConf conf;

    private ThriftHiveMetastore client = null;
    private boolean isConnected = false;
    private HostAndPort metastoreHosts[];

    // for thrift connects
    private final int retries;
    private final int retryDelaySeconds;

    private final Closer closer = Closer.create();

    private static final Logger logger = Logger.get("hive.metastore");

    private final ThriftClientManager thriftClientManager;


    public HiveMetaStoreClient(HiveConf conf)
        throws MetaException
    {
        this.thriftClientManager = closer.register(new ThriftClientManager());

        if (conf == null) {
            conf = new HiveConf(HiveMetaStoreClient.class);
        }
        this.conf = conf;

        String msUri = conf.getVar(HiveConf.ConfVars.METASTOREURIS);
        if (msUri == null || msUri.trim().length() == 0) {
            throw new MetaException("Local metastore is not supported!");
        }

        if (conf.getBoolVar(ConfVars.METASTORE_USE_THRIFT_SASL)) {
            throw new MetaException("SASL is not supported");
        }

        // get the number retries
        this.retries = HiveConf.getIntVar(conf, HiveConf.ConfVars.METASTORETHRIFTCONNECTIONRETRIES);
        this.retryDelaySeconds = conf.getIntVar(ConfVars.METASTORE_CLIENT_CONNECT_RETRY_DELAY);

        // user wants file store based configuration
        List<String> metastoreUris = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(msUri));

        metastoreHosts = new HostAndPort [metastoreUris.size()];

        for (int i = 0; i < metastoreUris.size(); i++) {
            URI uri = URI.create(metastoreUris.get(i));
            if (!uri.getScheme().equals("thrift")) {
                throw new MetaException("Only thrift:// URIs are supported!");
            }
            metastoreHosts[i] = HostAndPort.fromParts(uri.getHost(), uri.getPort());
        }

        // finally open the store
        open();
    }

    /**
     * Swaps the first element of the metastoreUris array with a random element from the
     * remainder of the array.
     */
    private void promoteRandomMetaStoreURI() {
        if (metastoreHosts.length <= 1) {
            return;
        }
        Random rng = new Random();
        int index = rng.nextInt(metastoreHosts.length - 1) + 1;
        HostAndPort tmp = metastoreHosts[0];
        metastoreHosts[0] = metastoreHosts[index];
        metastoreHosts[index] = tmp;
    }

    public void reconnect() throws MetaException {
        // Swap the first element of the metastoreUris[] with a random element from the rest
        // of the array. Rationale being that this method will generally be called when the default
        // connection has died and the default connection is likely to be the first array element.
        promoteRandomMetaStoreURI();
        open();
    }

    /**
     * @param dbname
     * @param tbl_name
     * @param new_tbl
     * @throws InvalidOperationException
     * @throws MetaException
     * @throws TException
     * @see
     *   org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#alter_table(
     *   java.lang.String, java.lang.String,
     *   org.apache.hadoop.hive.metastore.api.Table)
     */
    public void alter_table(String dbname, String tbl_name, Table new_tbl)
        throws InvalidOperationException, MetaException, TException {
        alter_table(dbname, tbl_name, new_tbl, null);
    }

    public void alter_table(String dbname, String tbl_name, Table new_tbl,
                            EnvironmentContext envContext) throws InvalidOperationException, MetaException, TException {
        client.alter_table_with_environment_context(dbname, tbl_name, new_tbl, envContext);
    }

    /**
     * @param dbname
     * @param name
     * @param part_vals
     * @param newPart
     * @throws InvalidOperationException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#rename_partition(
     *      java.lang.String, java.lang.String, java.util.List, org.apache.hadoop.hive.metastore.api.Partition)
     */
    public void renamePartition(final String dbname, final String name, final List<String> part_vals, final Partition newPart)
        throws InvalidOperationException, MetaException, TException {
        client.rename_partition(dbname, name, part_vals, newPart);
    }

    private void open() throws MetaException {
        isConnected = false;
        TTransportException tte = null;
        HadoopShims shim = ShimLoader.getHadoopShims();

        boolean useFramedTransport = conf.getBoolVar(ConfVars.METASTORE_USE_THRIFT_FRAMED_TRANSPORT);
        int clientSocketTimeout = conf.getIntVar(ConfVars.METASTORE_CLIENT_SOCKET_TIMEOUT);

        for (int attempt = 0; !isConnected && attempt < retries; ++attempt) {
            for (HostAndPort store : metastoreHosts) {
                logger.info("Trying to connect to metastore at %s", store);
                try {
                    final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig()
                        .setHost(store.getHostText())
                        .setPort(store.getPort())
                        .setFramed(useFramedTransport);

                    final ThriftClientConfig clientConfig = new ThriftClientConfig()
                        .setConnectTimeout(new Duration(clientSocketTimeout, TimeUnit.SECONDS));

                    final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(thriftClientManager, clientConfig, metastoreConfig);

                    try {
                        client = closer.register(ThriftHiveMetastore.Client.forHiveMetastore(factory.getDefaultClient()));
                        isConnected = true;
                    }
                    catch (TTransportException e) {
                        tte = e;
                        if (logger.isDebugEnabled()) {
                            logger.warn(e, "Failed to connect to the MetaStore Server...");
                        } else {
                            // Don't print full exception trace if DEBUG is not on.
                            logger.warn("Failed to connect to the MetaStore Server...");
                        }
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break; // stop trying to connect
                    }

                    if (isConnected && conf.getBoolVar(ConfVars.METASTORE_EXECUTE_SET_UGI)) {
                        // Call set_ugi, only in unsecure mode.
                        try {
                            UserGroupInformation ugi = shim.getUGIForConf(conf);
                            client.set_ugi(ugi.getUserName(), Arrays.asList(ugi.getGroupNames()));
                        } catch (LoginException e) {
                            logger.warn(e, "Failed to do login. set_ugi() is not successful, Continuing without it.");
                        } catch (IOException e) {
                            logger.warn(e, "Failed to find ugi of client set_ugi() is not successful, Continuing without it.");
                        } catch (TException e) {
                            logger.warn(e, "set_ugi() not successful, Likely cause: new client talking to old server. Continuing without it.", e);
                        }
                    }
                } catch (MetaException e) {
                    logger.error(e, "Unable to connect to metastore at %s in attempt %s", store, attempt);
                }
                if (isConnected) {
                    break;
                }
            }

            // Wait before launching the next round of connection retries.
            if (!isConnected && retryDelaySeconds > 0) {
                try {
                    logger.info("Waiting %s seconds before next connection attempt.", retryDelaySeconds);
                    Thread.sleep(retryDelaySeconds * 1000L);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!isConnected) {
            throw new MetaException("Could not connect to meta store using any of the URIs provided." +
                                    " Most recent failure: " + StringUtils.stringifyException(tte));
        }
        logger.info("Connected to metastore.");
    }

    @Override
    public void close()
    {
        isConnected = false;

        try {
            closer.close();
        }
        catch (IOException ioe) {
            logger.error(ioe, "While closing");
        }

    }

    /**
     * @param new_part
     * @return the added partition
     * @throws InvalidObjectException
     * @throws AlreadyExistsException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#add_partition(org.apache.hadoop.hive.metastore.api.Partition)
     */
    public Partition add_partition(Partition new_part)
        throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException {
        return add_partition(new_part, null);
    }

    public Partition add_partition(Partition new_part, EnvironmentContext envContext)
        throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException {
        return deepCopy(client.add_partition_with_environment_context(new_part, envContext));
    }

    /**
     * @param new_parts
     * @throws InvalidObjectException
     * @throws AlreadyExistsException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#add_partitions(List)
     */
    public int add_partitions(List<Partition> new_parts)
        throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException {
        return client.add_partitions(new_parts);
    }

    /**
     * @param table_name
     * @param db_name
     * @param part_vals
     * @return the appended partition
     * @throws InvalidObjectException
     * @throws AlreadyExistsException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#append_partition(java.lang.String,
     *      java.lang.String, java.util.List)
     */
    public Partition appendPartition(String db_name, String table_name,
                                     List<String> part_vals) throws InvalidObjectException,
        AlreadyExistsException, MetaException, TException {
        return appendPartition(db_name, table_name, part_vals, null);
    }

    public Partition appendPartition(String db_name, String table_name, List<String> part_vals,
                                     EnvironmentContext envContext) throws InvalidObjectException, AlreadyExistsException,
        MetaException, TException {
        return deepCopy(client.append_partition_with_environment_context(db_name, table_name,
                                                                         part_vals, envContext));
    }

    public Partition appendPartition(String dbName, String tableName, String partName)
        throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
        return appendPartition(dbName, tableName, partName, null);
    }

    public Partition getPartitionTemplate(String db_name, String table_name,
                                          List<String> part_vals) throws InvalidObjectException,
        MetaException, TException {
        return deepCopy(client.get_partition_template(db_name, table_name, part_vals));
    }

    public Partition appendPartition(String dbName, String tableName, String partName,
                                     EnvironmentContext envContext) throws InvalidObjectException, AlreadyExistsException,
        MetaException, TException {
        return deepCopy(client.append_partition_by_name_with_environment_context(dbName, tableName,
                                                                                 partName, envContext));
    }

    /**
     * Exchange the partition between two tables
     * @param partitionSpecs partitions specs of the parent partition to be exchanged
     * @param destDb the db of the destination table
     * @param destinationTableName the destination table name
     * @param overwrite overwrite partition if exists in destination table
     @ @return list of new partitions after exchanging
    */
    @Override
    public List<Partition> exchange_partition(Map<String, String> partitionSpecs,
                                              String sourceDb, String sourceTable, String destDb,
                                              String destinationTableName, boolean overwrite) throws MetaException,
        NoSuchObjectException, InvalidObjectException, TException, AlreadyExistsException,
        InvalidInputException, InvalidOperationException {
        return client.exchange_partition(partitionSpecs, sourceDb, sourceTable,
                                         destDb, destinationTableName, overwrite);
    }

    public void validatePartitionNameCharacters(List<String> partVals)
        throws TException, MetaException {
        client.partition_name_has_valid_characters(partVals, true);
    }

    /**
     * Create a new Database
     * @param db
     * @throws AlreadyExistsException
     * @throws InvalidObjectException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_database(Database)
     */
    public void createDatabase(Database db)
        throws AlreadyExistsException, InvalidObjectException, MetaException, TException {
        client.create_database(db);
    }

    /**
     * @param tbl
     * @throws MetaException
     * @throws NoSuchObjectException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_table(org.apache.hadoop.hive.metastore.api.Table)
     */
    public void createTable(Table tbl) throws AlreadyExistsException,
        InvalidObjectException, MetaException, NoSuchObjectException, TException {
        createTable(tbl, null);
    }

    public void createTable(Table tbl, EnvironmentContext envContext) throws AlreadyExistsException,
        InvalidObjectException, MetaException, NoSuchObjectException, TException {

        client.create_table_with_environment_context(tbl, envContext);
    }

    /**
     * @param type
     * @return true or false
     * @throws AlreadyExistsException
     * @throws InvalidObjectException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_type(org.apache.hadoop.hive.metastore.api.Type)
     */
    public boolean createType(Type type) throws AlreadyExistsException,
        InvalidObjectException, MetaException, TException {
        return client.create_type(type);
    }

    /**
     * @param name
     * @throws NoSuchObjectException
     * @throws InvalidOperationException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_database(java.lang.String, boolean, boolean)
     */
    public void dropDatabase(String name)
        throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
        dropDatabase(name, true, false, false);
    }

    public void dropDatabase(String name, boolean deleteData, boolean ignoreUnknownDb)
        throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
        dropDatabase(name, deleteData, ignoreUnknownDb, false);
    }

    public void dropDatabase(String name, boolean deleteData, boolean ignoreUnknownDb, boolean cascade)
        throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
        try {
            getDatabase(name);
        } catch (NoSuchObjectException e) {
            if (!ignoreUnknownDb) {
                throw e;
            }
            return;
        }

        if (cascade) {
            List<String> tableList = getAllTables(name);
            for (String table : tableList) {
                try {
                    dropTable(name, table, deleteData, false);
                } catch (UnsupportedOperationException e) {
                    // Ignore Index tables, those will be dropped with parent tables
                    logger.debug(e, "ignored");
                }
            }
        }
        client.drop_database(name, deleteData, cascade);
    }


    /**
     * @param tbl_name
     * @param db_name
     * @param part_vals
     * @return true or false
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,
     *      java.lang.String, java.util.List, boolean)
     */
    public boolean dropPartition(String db_name, String tbl_name,
                                 List<String> part_vals) throws NoSuchObjectException, MetaException,
        TException {
        return dropPartition(db_name, tbl_name, part_vals, true, null);
    }

    public boolean dropPartition(String db_name, String tbl_name, List<String> part_vals,
                                 EnvironmentContext env_context) throws NoSuchObjectException, MetaException, TException {
        return dropPartition(db_name, tbl_name, part_vals, true, env_context);
    }

    public boolean dropPartition(String dbName, String tableName, String partName, boolean deleteData)
        throws NoSuchObjectException, MetaException, TException {
        return dropPartition(dbName, tableName, partName, deleteData, null);
    }

    public boolean dropPartition(String dbName, String tableName, String partName, boolean deleteData,
                                 EnvironmentContext envContext) throws NoSuchObjectException, MetaException, TException {
        return client.drop_partition_by_name_with_environment_context(dbName, tableName, partName,
                                                                      deleteData, envContext);
    }

    /**
     * @param db_name
     * @param tbl_name
     * @param part_vals
     * @param deleteData
     *          delete the underlying data or just delete the table in metadata
     * @return true or false
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,
     *      java.lang.String, java.util.List, boolean)
     */
    public boolean dropPartition(String db_name, String tbl_name,
                                 List<String> part_vals, boolean deleteData) throws NoSuchObjectException,
        MetaException, TException {
        return dropPartition(db_name, tbl_name, part_vals, deleteData, null);
    }

    public boolean dropPartition(String db_name, String tbl_name, List<String> part_vals,
                                 boolean deleteData, EnvironmentContext envContext) throws NoSuchObjectException,
        MetaException, TException {
        return client.drop_partition_with_environment_context(db_name, tbl_name, part_vals, deleteData,
                                                              envContext);
    }

    /**
     * @param name
     * @param dbname
     * @throws NoSuchObjectException
     * @throws ExistingDependentsException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,
     *      java.lang.String, boolean)
     */
    public void dropTable(String dbname, String name)
        throws NoSuchObjectException, MetaException, TException {
        dropTable(dbname, name, true, true, null);
    }

    /** {@inheritDoc} */
    @Deprecated
    public void dropTable(String tableName, boolean deleteData)
        throws MetaException, UnknownTableException, TException, NoSuchObjectException {
        dropTable(DEFAULT_DATABASE_NAME, tableName, deleteData, false, null);
    }

    /**
     * @param dbname
     * @param name
     * @param deleteData
     *          delete the underlying data or just delete the table in metadata
     * @throws NoSuchObjectException
     * @throws ExistingDependentsException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,
     *      java.lang.String, boolean)
     */
    public void dropTable(String dbname, String name, boolean deleteData,
                          boolean ignoreUnknownTab) throws MetaException, TException,
        NoSuchObjectException, UnsupportedOperationException {
        dropTable(dbname, name, deleteData, ignoreUnknownTab, null);
    }

    public void dropTable(String dbname, String name, boolean deleteData,
                          boolean ignoreUnknownTab, EnvironmentContext envContext) throws MetaException, TException,
        NoSuchObjectException, UnsupportedOperationException {
        Table tbl;
        try {
            tbl = getTable(dbname, name);
        } catch (NoSuchObjectException e) {
            if (!ignoreUnknownTab) {
                throw e;
            }
            return;
        }
        if (isIndexTable(tbl)) {
            throw new UnsupportedOperationException("Cannot drop index tables");
        }
        client.drop_table_with_environment_context(dbname, name, deleteData, envContext);
    }

    /**
     * @param type
     * @return true if the type is dropped
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_type(java.lang.String)
     */
    public boolean dropType(String type) throws NoSuchObjectException, MetaException, TException {
        return client.drop_type(type);
    }

    /**
     * @param name
     * @return map of types
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type_all(java.lang.String)
     */
    public Map<String, Type> getTypeAll(String name) throws MetaException,
        TException {
        Map<String, Type> result = null;
        Map<String, Type> fromClient = client.get_type_all(name);
        if (fromClient != null) {
            result = new LinkedHashMap<String, Type>();
            for (Map.Entry<String, Type> entry : fromClient.entrySet()) {
                result.put(entry.getKey(), deepCopy(entry.getValue()));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public List<String> getDatabases(String databasePattern)
        throws MetaException {
        try {
            return client.get_databases(databasePattern);
        } catch (Exception e) {
            MetaStoreUtils.logAndThrowMetaException(e);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<String> getAllDatabases() throws MetaException {
        try {
            return client.get_all_databases();
        } catch (Exception e) {
            MetaStoreUtils.logAndThrowMetaException(e);
        }
        return null;
    }

    /**
     * @param tbl_name
     * @param db_name
     * @param max_parts
     * @return list of partitions
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     */
    public List<Partition> listPartitions(String db_name, String tbl_name,
                                          short max_parts) throws NoSuchObjectException, MetaException, TException {
        return deepCopyPartitions(
            client.get_partitions(db_name, tbl_name, max_parts));
    }

    @Override
    public List<Partition> listPartitions(String db_name, String tbl_name,
                                          List<String> part_vals, short max_parts)
        throws NoSuchObjectException, MetaException, TException {
        return deepCopyPartitions(
            client.get_partitions_ps(db_name, tbl_name, part_vals, max_parts));
    }

    @Override
    public List<Partition> listPartitionsWithAuthInfo(String db_name,
                                                      String tbl_name, short max_parts, String user_name, List<String> group_names)
        throws NoSuchObjectException, MetaException, TException {
        return deepCopyPartitions(
            client.get_partitions_with_auth(db_name, tbl_name, max_parts, user_name, group_names));
    }

    @Override
    public List<Partition> listPartitionsWithAuthInfo(String db_name,
                                                      String tbl_name, List<String> part_vals, short max_parts,
                                                      String user_name, List<String> group_names) throws NoSuchObjectException,
        MetaException, TException {
        return deepCopyPartitions(client.get_partitions_ps_with_auth(db_name,
                                                                     tbl_name, part_vals, max_parts, user_name, group_names));
    }

    /**
     * Get list of partitions matching specified filter
     * @param db_name the database name
     * @param tbl_name the table name
     * @param filter the filter string,
     *    for example "part1 = \"p1_abc\" and part2 <= "\p2_test\"". Filtering can
     *    be done only on string partition keys.
     * @param max_parts the maximum number of partitions to return,
     *    all partitions are returned if -1 is passed
     * @return list of partitions
     * @throws MetaException
     * @throws NoSuchObjectException
     * @throws TException
     */
    public List<Partition> listPartitionsByFilter(String db_name, String tbl_name,
                                                  String filter, short max_parts) throws MetaException,
        NoSuchObjectException, TException {
        return deepCopyPartitions(
            client.get_partitions_by_filter(db_name, tbl_name, filter, max_parts));
    }

    /**
     * @param name
     * @return the database
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_database(java.lang.String)
     */
    public Database getDatabase(String name) throws NoSuchObjectException,
        MetaException, TException {
        return deepCopy(client.get_database(name));
    }

    /**
     * @param tbl_name
     * @param db_name
     * @param part_vals
     * @return the partition
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_partition(java.lang.String,
     *      java.lang.String, java.util.List)
     */
    public Partition getPartition(String db_name, String tbl_name,
                                  List<String> part_vals) throws NoSuchObjectException, MetaException, TException {
        return deepCopy(client.get_partition(db_name, tbl_name, part_vals));
    }

    public List<Partition> getPartitionsByNames(String db_name, String tbl_name,
                                                List<String> part_names) throws NoSuchObjectException, MetaException, TException {
        return deepCopyPartitions(client.get_partitions_by_names(db_name, tbl_name, part_names));
    }

    @Override
    public Partition getPartitionWithAuthInfo(String db_name, String tbl_name,
                                              List<String> part_vals, String user_name, List<String> group_names)
        throws MetaException, UnknownTableException, NoSuchObjectException,
        TException {
        return deepCopy(client.get_partition_with_auth(db_name, tbl_name, part_vals, user_name, group_names));
    }

    /**
     * @param name
     * @param dbname
     * @return the table
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     * @throws NoSuchObjectException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_table(java.lang.String,
     *      java.lang.String)
     */
    public Table getTable(String dbname, String name) throws MetaException,
        TException, NoSuchObjectException {
        return deepCopy(client.get_table(dbname, name));
    }

    /** {@inheritDoc} */
    @Deprecated
    public Table getTable(String tableName) throws MetaException, TException,
        NoSuchObjectException {
        return getTable(DEFAULT_DATABASE_NAME, tableName);
    }

    /** {@inheritDoc} */
    public List<Table> getTableObjectsByName(String dbName, List<String> tableNames)
        throws MetaException, InvalidOperationException, UnknownDBException, TException {
        return deepCopyTables(client.get_table_objects_by_name(dbName, tableNames));
    }

    /** {@inheritDoc} */
    public List<String> listTableNamesByFilter(String dbName, String filter, short maxTables)
        throws MetaException, TException, InvalidOperationException, UnknownDBException {
        return client.get_table_names_by_filter(dbName, filter, maxTables);
    }

    /**
     * @param name
     * @return the type
     * @throws MetaException
     * @throws TException
     * @throws NoSuchObjectException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type(java.lang.String)
     */
    public Type getType(String name) throws NoSuchObjectException, MetaException, TException {
        return deepCopy(client.get_type(name));
    }

    /** {@inheritDoc} */
    public List<String> getTables(String dbname, String tablePattern) throws MetaException {
        try {
            return client.get_tables(dbname, tablePattern);
        } catch (Exception e) {
            MetaStoreUtils.logAndThrowMetaException(e);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<String> getAllTables(String dbname) throws MetaException {
        try {
            return client.get_all_tables(dbname);
        } catch (Exception e) {
            MetaStoreUtils.logAndThrowMetaException(e);
        }
        return null;
    }

    public boolean tableExists(String databaseName, String tableName) throws MetaException,
        TException, UnknownDBException {
        try {
            client.get_table(databaseName, tableName);
        } catch (NoSuchObjectException e) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Deprecated
    public boolean tableExists(String tableName) throws MetaException,
        TException, UnknownDBException {
        return tableExists(DEFAULT_DATABASE_NAME, tableName);
    }

    public List<String> listPartitionNames(String dbName, String tblName,
                                           short max) throws MetaException, TException {
        return client.get_partition_names(dbName, tblName, max);
    }

    @Override
    public int getTotalPartitions(String dbName, String tblName) throws MetaException, TException {
        return client.get_total_partitions(dbName, tblName);
    }

    @Override
    public List<String> listPartitionNames(String db_name, String tbl_name,
                                           List<String> part_vals, short max_parts)
        throws MetaException, TException, NoSuchObjectException {
        return client.get_partition_names_ps(db_name, tbl_name, part_vals, max_parts);
    }

    public void alter_partition(String dbName, String tblName, Partition newPart)
        throws InvalidOperationException, MetaException, TException {
        client.alter_partition(dbName, tblName, newPart);
    }

    public void alter_partitions(String dbName, String tblName, List<Partition> newParts)
        throws InvalidOperationException, MetaException, TException {
        client.alter_partitions(dbName, tblName, newParts);
    }

    public void alterDatabase(String dbName, Database db)
        throws MetaException, NoSuchObjectException, TException {
        client.alter_database(dbName, db);
    }
    /**
     * @param db
     * @param tableName
     * @throws UnknownTableException
     * @throws UnknownDBException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_fields(java.lang.String,
     *      java.lang.String)
     */
    public List<FieldSchema> getFields(String db, String tableName)
        throws MetaException, TException, UnknownTableException,
        UnknownDBException {
        return deepCopyFieldSchemas(client.get_fields(db, tableName));
    }

    /**
     * create an index
     * @param index the index object
     * @param indexTable which stores the index data
     * @throws InvalidObjectException
     * @throws MetaException
     * @throws NoSuchObjectException
     * @throws TException
     * @throws AlreadyExistsException
     */
    public void createIndex(Index index, Table indexTable) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
        client.add_index(index, indexTable);
    }

    /**
     * @param dbname
     * @param base_tbl_name
     * @param idx_name
     * @param new_idx
     * @throws InvalidOperationException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#alter_index(java.lang.String,
     *      java.lang.String, java.lang.String, org.apache.hadoop.hive.metastore.api.Index)
     */
    public void alter_index(String dbname, String base_tbl_name, String idx_name, Index new_idx)
        throws InvalidOperationException, MetaException, TException {
        client.alter_index(dbname, base_tbl_name, idx_name, new_idx);
    }

    /**
     * @param dbName
     * @param tblName
     * @param indexName
     * @return the index
     * @throws MetaException
     * @throws UnknownTableException
     * @throws NoSuchObjectException
     * @throws TException
     */
    public Index getIndex(String dbName, String tblName, String indexName)
        throws MetaException, UnknownTableException, NoSuchObjectException,
        TException {
        return deepCopy(client.get_index_by_name(dbName, tblName, indexName));
    }

    /**
     * list indexes of the give base table
     * @param dbName
     * @param tblName
     * @param max
     * @return the list of indexes
     * @throws NoSuchObjectException
     * @throws MetaException
     * @throws TException
     */
    public List<String> listIndexNames(String dbName, String tblName, short max)
        throws MetaException, TException {
        return client.get_index_names(dbName, tblName, max);
    }

    /**
     * list all the index names of the give base table.
     *
     * @param dbName
     * @param tblName
     * @param max
     * @return list of indexes
     * @throws MetaException
     * @throws TException
     */
    public List<Index> listIndexes(String dbName, String tblName, short max)
        throws NoSuchObjectException, MetaException, TException {
        return client.get_indexes(dbName, tblName, max);
    }

     /** {@inheritDoc} */
     public boolean updateTableColumnStatistics(ColumnStatistics statsObj)
         throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
         InvalidInputException{
         return client.update_table_column_statistics(statsObj);
     }

     /** {@inheritDoc} */
     public boolean updatePartitionColumnStatistics(ColumnStatistics statsObj)
         throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
         InvalidInputException{
         return client.update_partition_column_statistics(statsObj);
     }

     /** {@inheritDoc} */
     public ColumnStatistics getTableColumnStatistics(String dbName, String tableName,String colName)
         throws NoSuchObjectException, MetaException, TException, InvalidInputException,
         InvalidObjectException {
         return client.get_table_column_statistics(dbName, tableName, colName);
     }

     /** {@inheritDoc} */
     public ColumnStatistics getPartitionColumnStatistics(String dbName, String tableName,
                                                          String partName, String colName) throws NoSuchObjectException, MetaException, TException,
         InvalidInputException, InvalidObjectException {
         return client.get_partition_column_statistics(dbName, tableName, partName, colName);
     }

    /** {@inheritDoc} */
    public boolean deletePartitionColumnStatistics(String dbName, String tableName, String partName,
                                                   String colName) throws NoSuchObjectException, InvalidObjectException, MetaException,
        TException, InvalidInputException
    {
        return client.delete_partition_column_statistics(dbName, tableName, partName, colName);
    }

    /** {@inheritDoc} */
    public boolean deleteTableColumnStatistics(String dbName, String tableName, String colName)
        throws NoSuchObjectException, InvalidObjectException, MetaException, TException,
        InvalidInputException
    {
        return client.delete_table_column_statistics(dbName, tableName, colName);
    }

    /**
     * @param db
     * @param tableName
     * @throws UnknownTableException
     * @throws UnknownDBException
     * @throws MetaException
     * @throws TException
     * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_schema(java.lang.String,
     *      java.lang.String)
     */
    public List<FieldSchema> getSchema(String db, String tableName)
        throws MetaException, TException, UnknownTableException,
        UnknownDBException {
        return deepCopyFieldSchemas(client.get_schema(db, tableName));
    }

    public String getConfigValue(String name, String defaultValue)
        throws TException, ConfigValSecurityException {
        return client.get_config_value(name, defaultValue);
    }

    public Partition getPartition(String db, String tableName, String partName)
        throws MetaException, TException, UnknownTableException, NoSuchObjectException {
        return deepCopy(client.get_partition_by_name(db, tableName, partName));
    }

    public Partition appendPartitionByName(String dbName, String tableName, String partName)
        throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
        return appendPartitionByName(dbName, tableName, partName, null);
    }

    public Partition appendPartitionByName(String dbName, String tableName, String partName,
                                           EnvironmentContext envContext) throws InvalidObjectException, AlreadyExistsException,
        MetaException, TException {
        return deepCopy(client.append_partition_by_name_with_environment_context(dbName, tableName,
                                                                                 partName, envContext));
    }

    public boolean dropPartitionByName(String dbName, String tableName, String partName,
                                       boolean deleteData) throws NoSuchObjectException, MetaException, TException {
        return dropPartitionByName(dbName, tableName, partName, deleteData, null);
    }

    public boolean dropPartitionByName(String dbName, String tableName, String partName,
                                       boolean deleteData, EnvironmentContext envContext) throws NoSuchObjectException,
        MetaException, TException {
        return client.drop_partition_by_name_with_environment_context(dbName, tableName, partName,
                                                                      deleteData, envContext);
    }

    @Override
    public List<String> partitionNameToVals(String name) throws MetaException, TException {
        return client.partition_name_to_vals(name);
    }

    @Override
    public Map<String, String> partitionNameToSpec(String name) throws MetaException, TException{
        return client.partition_name_to_spec(name);
    }

    /**
     * @param partition
     * @return
     */
    private Partition deepCopy(Partition partition) {
        Partition copy = null;
        if (partition != null) {
            copy = new Partition(partition.getValues(),
                                 partition.getDbName(),
                                 partition.getTableName(),
                                 partition.getCreateTime(),
                                 partition.getLastAccessTime(),
                                 partition.getSd(),
                                 partition.getParameters(),
                                 partition.getPrivileges(),
                                 partition.getLinkTarget(),
                                 partition.getLinkPartitions());
        }
        return copy;
    }

    private Database deepCopy(Database database) {
        Database copy = null;
        if (database != null) {
            copy = new Database(database.getName(),
                                database.getDescription(),
                                database.getLocationUri(),
                                database.getParameters(),
                                database.getPrivileges());
        }
        return copy;
    }

    private Table deepCopy(Table table) {
        Table copy = null;
        if (table != null) {
            copy = new Table(table.getTableName(),
                             table.getDbName(),
                             table.getOwner(),
                             table.getCreateTime(),
                             table.getLastAccessTime(),
                             table.getRetention(),
                             table.getSd(),
                             table.getPartitionKeys(),
                             table.getParameters(),
                             table.getViewOriginalText(),
                             table.getViewExpandedText(),
                             table.getTableType(),
                             table.getPrivileges(),
                             table.getLinkTarget(),
                             table.getLinkTables());

        }
        return copy;
    }

    private Index deepCopy(Index index) {
        Index copy = null;
        if (index != null) {
            copy = new Index(index.getIndexName(),
                             index.getIndexHandlerClass(),
                             index.getDbName(),
                             index.getOrigTableName(),
                             index.getCreateTime(),
                             index.getLastAccessTime(),
                             index.getIndexTableName(),
                             index.getSd(), index.getParameters(),
                             index.isDeferredRebuild());
        }
        return copy;
    }

    private Type deepCopy(Type type) {
        Type copy = null;
        if (type != null) {
            copy = new Type(type.getName(),
                            type.getType1(),
                            type.getType2(),
                            type.getFields());
        }
        return copy;
    }

    private FieldSchema deepCopy(FieldSchema schema) {
        FieldSchema copy = null;
        if (schema != null) {
            copy = new FieldSchema(schema.getName(),
                                   schema.getType(),
                                   schema.getComment());
        }
        return copy;
    }

    private List<Partition> deepCopyPartitions(List<Partition> partitions) {
        List<Partition> copy = null;
        if (partitions != null) {
            copy = new ArrayList<Partition>();
            for (Partition part : partitions) {
                copy.add(deepCopy(part));
            }
        }
        return copy;
    }

    private List<Table> deepCopyTables(List<Table> tables) {
        List<Table> copy = null;
        if (tables != null) {
            copy = new ArrayList<Table>();
            for (Table tab : tables) {
                copy.add(deepCopy(tab));
            }
        }
        return copy;
    }

    private List<FieldSchema> deepCopyFieldSchemas(List<FieldSchema> schemas) {
        List<FieldSchema> copy = null;
        if (schemas != null) {
            copy = new ArrayList<FieldSchema>();
            for (FieldSchema schema : schemas) {
                copy.add(deepCopy(schema));
            }
        }
        return copy;
    }

    @Override
    public boolean dropIndex(String dbName, String tblName, String name,
                             boolean deleteData) throws NoSuchObjectException, MetaException,
        TException {
        return client.drop_index_by_name(dbName, tblName, name, deleteData);
    }

    @Override
    public boolean grant_role(String roleName, String userName,
                              PrincipalType principalType, String grantor, PrincipalType grantorType,
                              boolean grantOption) throws MetaException, TException {
        return client.grant_role(roleName, userName, principalType, grantor,
                                 grantorType, grantOption);
    }

    @Override
    public boolean create_role(Role role)
        throws MetaException, TException {
        return client.create_role(role);
    }

    @Override
    public boolean drop_role(String roleName) throws MetaException, TException {
        return client.drop_role(roleName);
    }

    @Override
    public List<Role> list_roles(String principalName,
                                 PrincipalType principalType) throws MetaException, TException {
        return client.list_roles(principalName, principalType);
    }

    @Override
    public List<String> listRoleNames() throws MetaException, TException {
        return client.get_role_names();
    }

    @Override
    public boolean grant_privileges(PrivilegeBag privileges)
        throws MetaException, TException {
        return client.grant_privileges(privileges);
    }

    @Override
    public boolean revoke_role(String roleName, String userName,
                               PrincipalType principalType) throws MetaException, TException {
        return client.revoke_role(roleName, userName, principalType);
    }

    @Override
    public boolean revoke_privileges(PrivilegeBag privileges) throws MetaException,
        TException {
        return client.revoke_privileges(privileges);
    }

    @Override
    public PrincipalPrivilegeSet get_privilege_set(HiveObjectRef hiveObject,
                                                   String userName, List<String> groupNames) throws MetaException,
        TException {
        return client.get_privilege_set(hiveObject, userName, groupNames);
    }

    @Override
    public List<HiveObjectPrivilege> list_privileges(String principalName,
                                                     PrincipalType principalType, HiveObjectRef hiveObject)
        throws MetaException, TException {
        return client.list_privileges(principalName, principalType, hiveObject);
    }

    public String getDelegationToken(String renewerKerberosPrincipalName) throws
        MetaException, TException, IOException {
        //a convenience method that makes the intended owner for the delegation
        //token request the current user
        String owner = conf.getUser();
        return getDelegationToken(owner, renewerKerberosPrincipalName);
    }

    @Override
    public String getDelegationToken(String owner, String renewerKerberosPrincipalName) throws
        MetaException, TException {
        return client.get_delegation_token(owner, renewerKerberosPrincipalName);
    }

    @Override
    public long renewDelegationToken(String tokenStrForm) throws MetaException, TException {
        return client.renew_delegation_token(tokenStrForm);

    }

    @Override
    public void cancelDelegationToken(String tokenStrForm) throws MetaException, TException {
        client.cancel_delegation_token(tokenStrForm);
    }

    /**
     * Creates a synchronized wrapper for any {@link IMetaStoreClient}.
     * This may be used by multi-threaded applications until we have
     * fixed all reentrancy bugs.
     *
     * @param client unsynchronized client
     *
     * @return synchronized client
     */
    public static IMetaStoreClient newSynchronizedClient(
        IMetaStoreClient client) {
        return (IMetaStoreClient) Proxy.newProxyInstance(
            HiveMetaStoreClient.class.getClassLoader(),
            new Class [] { IMetaStoreClient.class },
            new SynchronizedHandler(client));
    }

    private static class SynchronizedHandler implements InvocationHandler {
        private final IMetaStoreClient client;
        private static final Object lock = SynchronizedHandler.class;

        SynchronizedHandler(IMetaStoreClient client) {
            this.client = client;
        }

        public Object invoke(Object proxy, Method method, Object [] args)
            throws Throwable {
            try {
                synchronized (lock) {
                    return method.invoke(client, args);
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    @Override
    public void markPartitionForEvent(String db_name, String tbl_name, Map<String,String> partKVs, PartitionEventType eventType)
        throws MetaException, TException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        InvalidPartitionException, UnknownPartitionException {
        assert db_name != null;
        assert tbl_name != null;
        assert partKVs != null;
        client.markPartitionForEvent(db_name, tbl_name, partKVs, eventType);
    }

    @Override
    public boolean isPartitionMarkedForEvent(String db_name, String tbl_name, Map<String,String> partKVs, PartitionEventType eventType)
        throws MetaException, NoSuchObjectException, UnknownTableException, UnknownDBException, TException,
        InvalidPartitionException, UnknownPartitionException {
        assert db_name != null;
        assert tbl_name != null;
        assert partKVs != null;
        return client.isPartitionMarkedForEvent(db_name, tbl_name, partKVs, eventType);
    }

    @Override
    public boolean exists_table(String dbName, String tblName) throws MetaException, TException {
        return client.exists_table(dbName, tblName);
    }

}
