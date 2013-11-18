/*
 * Copyright (C) 2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.hive.metastore.client;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.nifty.client.UnframedClientConnector;
import com.facebook.swift.service.ThriftClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

import io.airlift.log.Logger;
import io.airlift.units.Duration;

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
import org.apache.hadoop.hive.metastore.api.InvalidTableLinkDescriptionException;
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
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import javax.annotation.concurrent.NotThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
public class RetryingHiveMetastore implements HiveMetastore
{
    private static final Logger log = Logger.get(RetryingHiveMetastore.class);

    private final HiveMetastoreClientConfig config;
    private final ThriftClient<HiveMetastore> thriftClient;

    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicReference<HiveMetastore> clientHolder = new AtomicReference<>();
    private final AtomicReference<HostAndPort> currentHostAndPort = new AtomicReference<>();

    private final LinkedList<HostAndPort> hostAndPorts;

    RetryingHiveMetastore(final Set<HostAndPort> hostAndPorts,
                          final HiveMetastoreClientConfig config,
                          final ThriftClient<HiveMetastore> thriftClient)
    {
        checkNotNull(hostAndPorts, "hostAndPort is null");
        checkArgument(!hostAndPorts.isEmpty(), "at least one hostAndPort must be present!");
        this.config = checkNotNull(config, "config is null");
        this.thriftClient = checkNotNull(thriftClient, "thriftClient is null");

        final List<HostAndPort> shuffleHostAndPorts = new ArrayList<>(hostAndPorts);
        Collections.shuffle(shuffleHostAndPorts);
        this.hostAndPorts = new LinkedList<>(shuffleHostAndPorts);

    }

    private synchronized HostAndPort getNextHostAndPort()
    {
        final HostAndPort hostAndPort;
        if (hostAndPorts.size() == 1) {
            hostAndPort = hostAndPorts.getFirst();
        }
        else {
            hostAndPort = hostAndPorts.removeFirst();
            hostAndPorts.addLast(hostAndPort);
        }

        currentHostAndPort.set(hostAndPort);
        return hostAndPort;
    }

    @Override
    public void close()
    {
        if (closed.compareAndSet(false, true)) {
            internalClose();
        }
    }

    private void internalClose()
    {
        final HiveMetastore client = clientHolder.getAndSet(null);
        if (client != null) {
            client.close();
        }
        currentHostAndPort.set(null);
    }

    @Override
    public boolean isConnected()
    {
        if (closed.get()) {
            return false;
        }

        return clientHolder.get() != null;
    }

    @VisibleForTesting
    @SuppressWarnings("PMD.PreserveStackTrace")
    HiveMetastore connect()
        throws TException
    {
        if (closed.get()) {
            throw new TTransportException(TTransportException.NOT_OPEN, "Client is already closed");
        }

        HiveMetastore client = clientHolder.get();

        while (client == null) {
            try {
                final HostAndPort hostAndPort = getNextHostAndPort();
                final NiftyClientConnector<? extends NiftyClientChannel> clientConnector = config.isFramed()
                    ? new FramedClientConnector(hostAndPort)
                    : new UnframedClientConnector(hostAndPort);

                client = thriftClient.open(clientConnector).get();
                if (!clientHolder.compareAndSet(null, client)) {
                    client.close();
                    client = clientHolder.get();
                }
            }
            catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                Throwables.propagateIfInstanceOf(t, TTransportException.class);
                throw Throwables.propagate(t);
            }
            catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TTransportException(TTransportException.NOT_OPEN, "Interrupted while connecting");
            }
        }

        return client;
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private <T> T withRetries(final String apiName, final CallableWithMetastore<T> callable)
        throws TException
    {
        checkNotNull(apiName, "apiName is null");
        checkNotNull(callable, "callable is null");

        final long startTime = System.nanoTime();

        int attempt = 0;

        try {
            for (;;) {
                attempt++;
                try {
                    final HiveMetastore client = connect();
                    log.debug("Executing %s (connected to %s, attempt %s)", apiName, currentHostAndPort.get(), attempt);
                    return callable.call(client);
                }
                catch (final Throwable t) {
                    TTransportException te = null;

                    if (t instanceof TTransportException) {
                        te = (TTransportException) t;
                    }
                    else if (t.getCause() instanceof TTransportException) {
                        te = (TTransportException) t.getCause();
                        log.debug("Found a TTransportException (%s) wrapped in a %s", te.getMessage(), t.getClass().getSimpleName());
                    }

                    if (te != null) {
                        final Duration now = Duration.nanosSince(startTime);
                        if (attempt > config.getMaxRetries() || now.compareTo(config.getRetryTimeout()) >= 0) {
                            log.warn("Failed executing %s (last host %s, attempt %s, elapsed time %s), Exception: %s (%s)",
                                apiName, currentHostAndPort.get(),
                                attempt, now.toString(TimeUnit.MILLISECONDS),
                                te.getClass().getSimpleName(), te.getMessage());

                            Throwables.propagateIfInstanceOf(t, TException.class);
                            throw Throwables.propagate(t);
                        }
                        log.debug("Retry executing %s (last host: %s, attempt %s, elapsed time %s), Exception: %s (%s)",
                            apiName, currentHostAndPort.get(),
                            attempt, now.toString(TimeUnit.MILLISECONDS),
                            te.getClass().getSimpleName(), te.getMessage());

                        internalClose();

                        TimeUnit.MILLISECONDS.sleep(config.getRetrySleep().toMillis());
                    }
                    else {
                        log.warn("Failed executing %s, Exception: %s (%s)", apiName, t.getClass().getSimpleName(), t.getMessage());
                        Throwables.propagateIfInstanceOf(t, TException.class);
                        throw Throwables.propagate(t);
                    }
                }
            }
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new TTransportException(TTransportException.NOT_OPEN, "Interrupted while connecting");
        }
    }

    public interface CallableWithMetastore<T>
    {
        T call(HiveMetastore client) throws Exception;
    }

    @Override
    public List<Partition> exchangePartition(final Map<String, String> partitionSpecs, final String sourceDb, final String sourceTableName, final String destDb, final String destTableName, final boolean overwrite) throws MetaException,
        NoSuchObjectException, InvalidObjectException,
        InvalidInputException, AlreadyExistsException, TException
    {
        return withRetries("exchangePartition", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.exchangePartition(partitionSpecs, sourceDb, sourceTableName, destDb, destTableName, overwrite);
            }
        });
    }

    @Override
    public void createTableLink(final String dbName, final String targetDbName, final String targetTableName, final String owner, final boolean isStatic, final Map<String, String> linkProperties) throws AlreadyExistsException, InvalidObjectException,
        MetaException, NoSuchObjectException,
        InvalidTableLinkDescriptionException, TException
    {
        withRetries("createTableLink", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.createTableLink(dbName, targetDbName, targetTableName, owner, isStatic, linkProperties);
                return null;
            }
        });
    }

    @Override
    public void dropTableLink(final String dbName, final String targetDbName, final String targetTableName) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTableLink", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.dropTableLink(dbName, targetDbName, targetTableName);
                return null;
            }
        });
    }

    @Override
    public boolean existsTable(final String dbname, final String tblName) throws MetaException, TException
    {
        return withRetries("existsTable", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.existsTable(dbname, tblName);
            }
        });
    }

    @Override
    public Table getTableLink(final String dbName, final String targetDbName, final String targetTableName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getTableLink", new CallableWithMetastore<Table>() {
            @Override
            public Table call(final HiveMetastore client) throws TException
            {
                return client.getTableLink(dbName, targetDbName, targetTableName);
            }
        });
    }

    @Override
    public void alterTableLink(final String dbName, final String targetDbName, final String targetTableName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTableLink", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterTableLink(dbName, targetDbName, targetTableName, newTbl);
                return null;
            }
        });
    }

    @Override
    public void alterTableLinkProperties(final String dbName, final String targetDbName, final String targetTableName, final Map<String, String> updatedProperties) throws InvalidOperationException, MetaException, NoSuchObjectException, TException
    {
        withRetries("alterTableLinkProperties", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterTableLinkProperties(dbName, targetDbName, targetTableName, updatedProperties);
                return null;
            }
        });
    }

    @Override
    public Partition addTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws InvalidObjectException, AlreadyExistsException, NoSuchObjectException, MetaException, TException
    {
        return withRetries("addTableLinkPartition", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.addTableLinkPartition(dbName, targetDbName, targetTableName, partitionName);
            }
        });
    }

    @Override
    public boolean dropTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropTableLinkPartition", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropTableLinkPartition(dbName, targetDbName, targetTableName, partitionName);
            }
        });
    }

    @Override
    public Partition getPartitionTemplate(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, MetaException, TException
    {
        return withRetries("getPartitionTemplate", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.getPartitionTemplate(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public int getTotalPartitions(final String dbName, final String tblName) throws MetaException, TException
    {
        return withRetries("getTotalPartitions", new CallableWithMetastore<Integer>() {
            @Override
            public Integer call(final HiveMetastore client) throws TException
            {
                return client.getTotalPartitions(dbName, tblName);
            }
        });
    }

    @Override
    public void createDatabase(final Database database) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        withRetries("createDatabase", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.createDatabase(database);
                return null;
            }
        });
    }

    @Override
    public Database getDatabase(final String name) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getDatabase", new CallableWithMetastore<Database>() {
            @Override
            public Database call(final HiveMetastore client) throws TException
            {
                return client.getDatabase(name);
            }
        });
    }

    @Override
    public void dropDatabase(final String name, final boolean deleteData, final boolean cascade) throws NoSuchObjectException, InvalidOperationException, MetaException, TException
    {
        withRetries("dropDatabase", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.dropDatabase(name, deleteData, cascade);
                return null;
            }
        });
    }

    @Override
    public List<String> getDatabases(final String pattern) throws MetaException, TException
    {
        return withRetries("getDatabases", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getDatabases(pattern);
            }
        });
    }

    @Override
    public List<String> getAllDatabases() throws MetaException, TException
    {
        return withRetries("getAllDatabases", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getAllDatabases();
            }
        });
    }

    @Override
    public void alterDatabase(final String dbname, final Database db) throws MetaException, NoSuchObjectException, TException
    {
        withRetries("alterDatabase", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterDatabase(dbname, db);
                return null;
            }
        });
    }

    @Override
    public Type getType(final String name) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getType", new CallableWithMetastore<Type>() {
            @Override
            public Type call(final HiveMetastore client) throws TException
            {
                return client.getType(name);
            }
        });
    }

    @Override
    public boolean createType(final Type type) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        return withRetries("createType", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.createType(type);
            }
        });
    }

    @Override
    public boolean dropType(final String type) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("dropType", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropType(type);
            }
        });
    }

    @Override
    public Map<String, Type> getTypeAll(final String name) throws MetaException, TException
    {
        return withRetries("getTypeAll", new CallableWithMetastore<Map<String, Type>>() {
            @Override
            public Map<String, Type> call(final HiveMetastore client) throws TException
            {
                return client.getTypeAll(name);
            }
        });
    }

    @Override
    public List<FieldSchema> getFields(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        return withRetries("getFields", new CallableWithMetastore<List<FieldSchema>>() {
            @Override
            public List<FieldSchema> call(final HiveMetastore client) throws TException
            {
                return client.getFields(dbName, tableName);
            }
        });
    }

    @Override
    public List<FieldSchema> getSchema(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        return withRetries("getSchema", new CallableWithMetastore<List<FieldSchema>>() {
            @Override
            public List<FieldSchema> call(final HiveMetastore client) throws TException
            {
                return client.getSchema(dbName, tableName);
            }
        });
    }

    @Override
    public void createTable(final Table tbl) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        withRetries("createTable", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.createTable(tbl);
                return null;
            }
        });
    }

    @Override
    public void createTableWithEnvironmentContext(final Table tbl, final EnvironmentContext environmentContext) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        withRetries("createTableWithEnvironmentContext", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.createTableWithEnvironmentContext(tbl, environmentContext);
                return null;
            }
        });
    }

    @Override
    public void dropTable(final String dbname, final String name, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTable", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.dropTable(dbname, name, deleteData);
                return null;
            }
        });
    }

    @Override
    public void dropTableWithEnvironmentContext(final String dbName, final String tblName, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTableWithEnvironmentContext", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.dropTableWithEnvironmentContext(dbName, tblName, deleteData, environmentContext);
                return null;
            }
        });
    }

    @Override
    public List<String> getTables(final String dbName, final String pattern) throws MetaException, TException
    {
        return withRetries("getTables", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getTables(dbName, pattern);
            }
        });
    }

    @Override
    public List<String> getAllTables(final String dbName) throws MetaException, TException
    {
        return withRetries("getAllTables", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getAllTables(dbName);
            }
        });
    }

    @Override
    public Table getTable(final String dbname, final String tblName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getTable", new CallableWithMetastore<Table>() {
            @Override
            public Table call(final HiveMetastore client) throws TException
            {
                return client.getTable(dbname, tblName);
            }
        });
    }

    @Override
    public List<Table> getTableObjectsByName(final String dbname, final List<String> tblNames) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        return withRetries("getTableObjectsByName", new CallableWithMetastore<List<Table>>() {
            @Override
            public List<Table> call(final HiveMetastore client) throws TException
            {
                return client.getTableObjectsByName(dbname, tblNames);
            }
        });
    }

    @Override
    public List<String> getTableNamesByFilter(final String dbname, final String filter, final short maxTables) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        return withRetries("getTableNamesByFilter", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getTableNamesByFilter(dbname, filter, maxTables);
            }
        });
    }

    @Override
    public void alterTable(final String dbname, final String tblName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTable", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterTable(dbname, tblName, newTbl);
                return null;
            }
        });
    }

    @Override
    public void alterTableWithEnvironmentContext(final String dbname, final String tblName, final Table newTbl, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTableWithEnvironmentContext", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterTableWithEnvironmentContext(dbname, tblName, newTbl, environmentContext);
                return null;
            }
        });
    }

    @Override
    public Partition addPartition(final Partition newPart) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartition", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.addPartition(newPart);
            }
        });
    }

    @Override
    public Partition addPartitionWithEnvironmentContext(final Partition newPart, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartitionWithEnvironmentContext", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.addPartitionWithEnvironmentContext(newPart, environmentContext);
            }
        });
    }

    @Override
    public int addPartitions(final List<Partition> newParts) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartitions", new CallableWithMetastore<Integer>() {
            @Override
            public Integer call(final HiveMetastore client) throws TException
            {
                return client.addPartitions(newParts);
            }
        });
    }

    @Override
    public Partition appendPartition(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("appendPartition", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.appendPartition(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public Partition appendPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        return withRetries("appendPartitionWithEnvironmentContext", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.appendPartitionWithEnvironmentContext(dbName, tblName, partVals, environmentContext);
            }
        });
    }

    @Override
    public Partition appendPartitionByName(final String dbName, final String tblName, final String partName) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("appendPartitionByName", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.appendPartitionByName(dbName, tblName, partName);
            }
        });
    }

    @Override
    public Partition appendPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        return withRetries("appendPartitionByNameWithEnvironmentContext", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.appendPartitionByNameWithEnvironmentContext(dbName, tblName, partName, environmentContext);
            }
        });
    }

    @Override
    public boolean dropPartition(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartition", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropPartition(dbName, tblName, partVals, deleteData);
            }
        });
    }

    @Override
    public boolean dropPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionWithEnvironmentContext", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropPartitionWithEnvironmentContext(dbName, tblName, partVals, deleteData, environmentContext);
            }
        });
    }

    @Override
    public boolean dropPartitionByName(final String dbName, final String tblName, final String partName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionByName", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropPartitionByName(dbName, tblName, partName, deleteData);
            }
        });
    }

    @Override
    public boolean dropPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionByNameWithEnvironmentContext", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropPartitionByNameWithEnvironmentContext(dbName, tblName, partName, deleteData, environmentContext);
            }
        });
    }

    @Override
    public Partition getPartition(final String dbName, final String tblName, final List<String> partVals) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartition", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.getPartition(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public Partition getPartitionWithAuth(final String dbName, final String tblName, final List<String> partVals, final String userName, final List<String> groupNames) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionWithAuth", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.getPartitionWithAuth(dbName, tblName, partVals, userName, groupNames);
            }
        });
    }

    @Override
    public Partition getPartitionByName(final String dbName, final String tblName, final String partName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionByName", new CallableWithMetastore<Partition>() {
            @Override
            public Partition call(final HiveMetastore client) throws TException
            {
                return client.getPartitionByName(dbName, tblName, partName);
            }
        });
    }

    @Override
    public List<Partition> getPartitions(final String dbName, final String tblName, final short maxParts) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitions", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitions(dbName, tblName, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsWithAuth(final String dbName, final String tblName, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitionsWithAuth", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionsWithAuth(dbName, tblName, maxParts, userName, groupNames);
            }
        });
    }

    @Override
    public List<String> getPartitionNames(final String dbName, final String tblName, final short maxParts) throws MetaException, TException
    {
        return withRetries("getPartitionNames", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionNames(dbName, tblName, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsPs", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionsPs(dbName, tblName, partVals, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsPsWithAuth(final String dbName, final String tblName, final List<String> partVals, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitionsPsWithAuth", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionsPsWithAuth(dbName, tblName, partVals, maxParts, userName, groupNames);
            }
        });
    }

    @Override
    public List<String> getPartitionNamesPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionNamesPs", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionNamesPs(dbName, tblName, partVals, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsByFilter(final String dbName, final String tblName, final String filter, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsByFilter", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionsByFilter(dbName, tblName, filter, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsByNames(final String dbName, final String tblName, final List<String> names) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsByNames", new CallableWithMetastore<List<Partition>>() {
            @Override
            public List<Partition> call(final HiveMetastore client) throws TException
            {
                return client.getPartitionsByNames(dbName, tblName, names);
            }
        });
    }

    @Override
    public void alterPartition(final String dbName, final String tblName, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartition", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterPartition(dbName, tblName, newPart);
                return null;
            }
        });
    }

    @Override
    public void alterPartitions(final String dbName, final String tblName, final List<Partition> newParts) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartitions", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterPartitions(dbName, tblName, newParts);
                return null;
            }
        });
    }

    @Override
    public void alterPartitionWithEnvironmentContext(final String dbName, final String tblName, final Partition newPart, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartitionWithEnvironmentContext", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterPartitionWithEnvironmentContext(dbName, tblName, newPart, environmentContext);
                return null;
            }
        });
    }

    @Override
    public void renamePartition(final String dbName, final String tblName, final List<String> partVals, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
        withRetries("renamePartition", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.renamePartition(dbName, tblName, partVals, newPart);
                return null;
            }
        });
    }

    @Override
    public boolean partitionNameHasValidCharacters(final List<String> partVals, final boolean throwException) throws MetaException, TException
    {
        return withRetries("partitionNameHasValidCharacters", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.partitionNameHasValidCharacters(partVals, throwException);
            }
        });
    }

    @Override
    public String getConfigValue(final String name, final String defaultValue) throws ConfigValSecurityException, TException
    {
        return withRetries("getConfigValue", new CallableWithMetastore<String>() {
            @Override
            public String call(final HiveMetastore client) throws TException
            {
                return client.getConfigValue(name, defaultValue);
            }
        });
    }

    @Override
    public List<String> partitionNameToVals(final String partName) throws MetaException, TException
    {
        return withRetries("partitionNameToVals", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.partitionNameToVals(partName);
            }
        });
    }

    @Override
    public Map<String, String> partitionNameToSpec(final String partName) throws MetaException, TException
    {
        return withRetries("partitionNameToSpec", new CallableWithMetastore<Map<String, String>>() {
            @Override
            public Map<String, String> call(final HiveMetastore client) throws TException
            {
                return client.partitionNameToSpec(partName);
            }
        });
    }

    @Override
    public void markPartitionForEvent(final String dbName, final String tblName, final Map<String, String> partVals, final PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        UnknownPartitionException,
        InvalidPartitionException, TException
    {
        withRetries("markPartitionForEvent", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.markPartitionForEvent(dbName, tblName, partVals, eventType);
                return null;
            }
        });
    }

    @Override
    public boolean isPartitionMarkedForEvent(final String dbName, final String tblName, final Map<String, String> partVals, final PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        UnknownPartitionException,
        InvalidPartitionException, TException
    {
        return withRetries("isPartitionMarkedForEvent", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.isPartitionMarkedForEvent(dbName, tblName, partVals, eventType);
            }
        });
    }

    @Override
    public Index addIndex(final Index newIndex, final Table indexTable) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addIndex", new CallableWithMetastore<Index>() {
            @Override
            public Index call(final HiveMetastore client) throws TException
            {
                return client.addIndex(newIndex, indexTable);
            }
        });
    }

    @Override
    public void alterIndex(final String dbname, final String baseTblName, final String idxName, final Index newIdx) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterIndex", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.alterIndex(dbname, baseTblName, idxName, newIdx);
                return null;
            }
        });
    }

    @Override
    public boolean dropIndexByName(final String dbName, final String tblName, final String indexName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropIndexByName", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropIndexByName(dbName, tblName, indexName, deleteData);
            }
        });
    }

    @Override
    public Index getIndexByName(final String dbName, final String tblName, final String indexName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getIndexByName", new CallableWithMetastore<Index>() {
            @Override
            public Index call(final HiveMetastore client) throws TException
            {
                return client.getIndexByName(dbName, tblName, indexName);
            }
        });
    }

    @Override
    public List<Index> getIndexes(final String dbName, final String tblName, final short maxIndexes) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getIndexes", new CallableWithMetastore<List<Index>>() {
            @Override
            public List<Index> call(final HiveMetastore client) throws TException
            {
                return client.getIndexes(dbName, tblName, maxIndexes);
            }
        });
    }

    @Override
    public List<String> getIndexNames(final String dbName, final String tblName, final short maxIndexes) throws MetaException, TException
    {
        return withRetries("getIndexNames", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getIndexNames(dbName, tblName, maxIndexes);
            }
        });
    }

    @Override
    public boolean updateTableColumnStatistics(final ColumnStatistics statsObj) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException
    {
        return withRetries("updateTableColumnStatistics", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.updateTableColumnStatistics(statsObj);
            }
        });
    }

    @Override
    public boolean updatePartitionColumnStatistics(final ColumnStatistics statsObj) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException
    {
        return withRetries("updatePartitionColumnStatistics", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.updatePartitionColumnStatistics(statsObj);
            }
        });
    }

    @Override
    public ColumnStatistics getTableColumnStatistics(final String dbName, final String tblName, final String colName) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException
    {
        return withRetries("getTableColumnStatistics", new CallableWithMetastore<ColumnStatistics>() {
            @Override
            public ColumnStatistics call(final HiveMetastore client) throws TException
            {
                return client.getTableColumnStatistics(dbName, tblName, colName);
            }
        });
    }

    @Override
    public ColumnStatistics getPartitionColumnStatistics(final String dbName, final String tblName, final String partName, final String colName) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException
    {
        return withRetries("getPartitionColumnStatistics", new CallableWithMetastore<ColumnStatistics>() {
            @Override
            public ColumnStatistics call(final HiveMetastore client) throws TException
            {
                return client.getPartitionColumnStatistics(dbName, tblName, partName, colName);
            }
        });
    }

    @Override
    public boolean deletePartitionColumnStatistics(final String dbName, final String tblName, final String partName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        return withRetries("deletePartitionColumnStatistics", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.deletePartitionColumnStatistics(dbName, tblName, partName, colName);
            }
        });
    }

    @Override
    public boolean deleteTableColumnStatistics(final String dbName, final String tblName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        return withRetries("deleteTableColumnStatistics", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.deleteTableColumnStatistics(dbName, tblName, colName);
            }
        });
    }

    @Override
    public boolean createRole(final Role role) throws MetaException, TException
    {
        return withRetries("createRole", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.createRole(role);
            }
        });
    }

    @Override
    public boolean dropRole(final String roleName) throws MetaException, TException
    {
        return withRetries("dropRole", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.dropRole(roleName);
            }
        });
    }

    @Override
    public List<String> getRoleNames() throws MetaException, TException
    {
        return withRetries("getRoleNames", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.getRoleNames();
            }
        });
    }

    @Override
    public boolean grantRole(final String roleName, final String principalName, final PrincipalType principalType, final String grantor, final PrincipalType grantorType, final boolean grantOption) throws MetaException, TException
    {
        return withRetries("grantRole", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.grantRole(roleName, principalName, principalType, grantor, grantorType, grantOption);
            }
        });
    }

    @Override
    public boolean revokeRole(final String roleName, final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        return withRetries("revokeRole", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.revokeRole(roleName, principalName, principalType);
            }
        });
    }

    @Override
    public List<Role> listRoles(final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        return withRetries("listRoles", new CallableWithMetastore<List<Role>>() {
            @Override
            public List<Role> call(final HiveMetastore client) throws TException
            {
                return client.listRoles(principalName, principalType);
            }
        });
    }

    @Override
    public PrincipalPrivilegeSet getPrivilegeSet(final HiveObjectRef hiveObject, final String userName, final List<String> groupNames) throws MetaException, TException
    {
        return withRetries("getPrivilegeSet", new CallableWithMetastore<PrincipalPrivilegeSet>() {
            @Override
            public PrincipalPrivilegeSet call(final HiveMetastore client) throws TException
            {
                return client.getPrivilegeSet(hiveObject, userName, groupNames);
            }
        });
    }

    @Override
    public List<HiveObjectPrivilege> listPrivileges(final String principalName, final PrincipalType principalType, final HiveObjectRef hiveObject) throws MetaException, TException
    {
        return withRetries("listPrivileges", new CallableWithMetastore<List<HiveObjectPrivilege>>() {
            @Override
            public List<HiveObjectPrivilege> call(final HiveMetastore client) throws TException
            {
                return client.listPrivileges(principalName, principalType, hiveObject);
            }
        });
    }

    @Override
    public boolean grantPrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        return withRetries("grantPrivileges", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.grantPrivileges(privileges);
            }
        });
    }

    @Override
    public boolean revokePrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        return withRetries("revokePrivileges", new CallableWithMetastore<Boolean>() {
            @Override
            public Boolean call(final HiveMetastore client) throws TException
            {
                return client.revokePrivileges(privileges);
            }
        });
    }

    @Override
    public List<String> setUgi(final String userName, final List<String> groupNames) throws MetaException, TException
    {
        return withRetries("setUgi", new CallableWithMetastore<List<String>>() {
            @Override
            public List<String> call(final HiveMetastore client) throws TException
            {
                return client.setUgi(userName, groupNames);
            }
        });
    }

    @Override
    public String getDelegationToken(final String tokenOwner, final String renewerKerberosPrincipalName) throws MetaException, TException
    {
        return withRetries("getDelegationToken", new CallableWithMetastore<String>() {
            @Override
            public String call(final HiveMetastore client) throws TException
            {
                return client.getDelegationToken(tokenOwner, renewerKerberosPrincipalName);
            }
        });
    }

    @Override
    public long renewDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        return withRetries("renewDelegationToken", new CallableWithMetastore<Long>() {
            @Override
            public Long call(final HiveMetastore client) throws TException
            {
                return client.renewDelegationToken(tokenStrForm);
            }
        });
    }

    @Override
    public void cancelDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        withRetries("cancelDelegationToken", new CallableWithMetastore<Void>() {
            @Override
            public Void call(final HiveMetastore client) throws TException
            {
                client.cancelDelegationToken(tokenStrForm);
                return null;
            }
        });
    }
}
