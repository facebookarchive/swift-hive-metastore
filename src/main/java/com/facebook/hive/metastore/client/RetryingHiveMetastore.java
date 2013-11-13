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
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import javax.annotation.concurrent.NotThreadSafe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
public class RetryingHiveMetastore implements HiveMetastore
{
    private static final Logger log = Logger.get(RetryingHiveMetastore.class);

    private final HiveMetastoreClientConfig config;
    private final ThriftClient<HiveMetastore> thriftClient;

    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicReference<HiveMetastore> clientHolder = new AtomicReference<>();
    private final NiftyClientConnector<? extends NiftyClientChannel> clientConnector;

    public RetryingHiveMetastore(final HostAndPort hostAndPort,
                                 final HiveMetastoreClientConfig config,
                                 final ThriftClient<HiveMetastore> thriftClient)
    {
        checkNotNull(hostAndPort, "hostAndPort is null");
        this.config = checkNotNull(config, "config is null");
        this.thriftClient = checkNotNull(thriftClient, "thriftClient is null");

        this.clientConnector = config.isFramed()
            ? new FramedClientConnector(hostAndPort)
            : new UnframedClientConnector(hostAndPort);
    }

    @Override
    public void close()
    {
        if (!closed.compareAndSet(false, true)) {
            final HiveMetastore client = clientHolder.getAndSet(null);
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        if (closed.get()) {
            return false;
        }
        if (clientHolder.get() != null) {
            return true;
        }

        try {
            withRetries("connect", new Callable<Void>() {
                @Override
                public Void call() throws TException
                {
                    connectClient();
                    return null;
                }
            });
            return true;
        }
        catch (TException t) {
            return false;
        }
    }

    @VisibleForTesting
    HiveMetastore connect()
        throws TException
    {
        if (closed.get()) {
            throw new TApplicationException("Client is already closed");
        }

        HiveMetastore client = clientHolder.get();
        while (client == null) {
            client = connectClient();
        }
        return client;
    }

    private HiveMetastore connectClient()
        throws TException
    {
        HiveMetastore client = null;

        try {
            client = thriftClient.open(clientConnector).get();
            if (!clientHolder.compareAndSet(null, client)) {
                client.close();
                client = clientHolder.get();
            }

            return client;
        }
        catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            Throwables.propagateIfInstanceOf(t, TTransportException.class);
            throw Throwables.propagate(t);
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TApplicationException("Interrupted while connecting");
        }
    }

    private <T> T withRetries(final String apiName, final Callable<T> callable)
        throws TException
    {
        checkNotNull(apiName, "apiName is null");
        checkNotNull(callable, "callable is null");

        final long startTime = System.nanoTime();

        int attempt = 0;

        for (;;) {
            attempt++;
            try {
                return callable.call();
            }
            catch (final TTransportException te) {
                if (attempt >= config.getMaxRetries() || Duration.nanosSince(startTime).compareTo(config.getRetryTimeout()) >= 0) {
                    throw te;
                }
                log.debug("Failed on executing %s with attempt %d, will retry. Exception: %s", apiName, attempt, te.getMessage());

                final HiveMetastore client = clientHolder.getAndSet(null);
                if (client != null) {
                    client.close();
                }

                retrySleep();
            }
            // Manages all the Metastore runtime exceptions
            catch (final Throwable t) {
                Throwables.propagateIfInstanceOf(t, TException.class);
                throw Throwables.propagate(t);
            }
        }
    }

    private void retrySleep()
    {
        try {
            TimeUnit.MILLISECONDS.sleep(config.getRetrySleep().toMillis());
        }
        catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<Partition> exchangePartition(final Map<String, String> partitionSpecs, final String sourceDb, final String sourceTableName, final String destDb, final String destTableName, final boolean overwrite) throws MetaException,
        NoSuchObjectException, InvalidObjectException,
        InvalidInputException, AlreadyExistsException, TException
    {
        return withRetries("exchangePartition", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.exchangePartition(partitionSpecs, sourceDb, sourceTableName, destDb, destTableName, overwrite);
            }
        });
    }

    @Override
    public void createTableLink(final String dbName, final String targetDbName, final String targetTableName, final String owner, final boolean isStatic, final Map<String, String> linkProperties) throws AlreadyExistsException, InvalidObjectException,
        MetaException, NoSuchObjectException,
        InvalidTableLinkDescriptionException, TException
    {
        withRetries("createTableLink", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.createTableLink(dbName, targetDbName, targetTableName, owner, isStatic, linkProperties);
                return null;
            }
        });
    }

    @Override
    public void dropTableLink(final String dbName, final String targetDbName, final String targetTableName) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTableLink", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.dropTableLink(dbName, targetDbName, targetTableName);
                return null;
            }
        });
    }

    @Override
    public boolean existsTable(final String dbname, final String tblName) throws MetaException, TException
    {
        return withRetries("existsTable", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.existsTable(dbname, tblName);
            }
        });
    }

    @Override
    public Table getTableLink(final String dbName, final String targetDbName, final String targetTableName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getTableLink", new Callable<Table>() {
            @Override
            public Table call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTableLink(dbName, targetDbName, targetTableName);
            }
        });
    }

    @Override
    public void alterTableLink(final String dbName, final String targetDbName, final String targetTableName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTableLink", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterTableLink(dbName, targetDbName, targetTableName, newTbl);
                return null;
            }
        });
    }

    @Override
    public void alterTableLinkProperties(final String dbName, final String targetDbName, final String targetTableName, final Map<String, String> updatedProperties) throws InvalidOperationException, MetaException, NoSuchObjectException, TException
    {
        withRetries("alterTableLinkProperties", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterTableLinkProperties(dbName, targetDbName, targetTableName, updatedProperties);
                return null;
            }
        });
    }

    @Override
    public Partition addTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws InvalidObjectException, AlreadyExistsException, NoSuchObjectException, MetaException, TException
    {
        return withRetries("addTableLinkPartition", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.addTableLinkPartition(dbName, targetDbName, targetTableName, partitionName);
            }
        });
    }

    @Override
    public boolean dropTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropTableLinkPartition", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropTableLinkPartition(dbName, targetDbName, targetTableName, partitionName);
            }
        });
    }

    @Override
    public Partition getPartitionTemplate(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, MetaException, TException
    {
        return withRetries("getPartitionTemplate", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionTemplate(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public int getTotalPartitions(final String dbName, final String tblName) throws MetaException, TException
    {
        return withRetries("getTotalPartitions", new Callable<Integer>() {
            @Override
            public Integer call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTotalPartitions(dbName, tblName);
            }
        });
    }

    @Override
    public void createDatabase(final Database database) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        withRetries("createDatabase", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.createDatabase(database);
                return null;
            }
        });
    }

    @Override
    public Database getDatabase(final String name) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getDatabase", new Callable<Database>() {
            @Override
            public Database call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getDatabase(name);
            }
        });
    }

    @Override
    public void dropDatabase(final String name, final boolean deleteData, final boolean cascade) throws NoSuchObjectException, InvalidOperationException, MetaException, TException
    {
        withRetries("dropDatabase", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.dropDatabase(name, deleteData, cascade);
                return null;
            }
        });
    }

    @Override
    public List<String> getDatabases(final String pattern) throws MetaException, TException
    {
        return withRetries("getDatabases", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getDatabases(pattern);
            }
        });
    }

    @Override
    public List<String> getAllDatabases() throws MetaException, TException
    {
        return withRetries("getAllDatabases", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getAllDatabases();
            }
        });
    }

    @Override
    public void alterDatabase(final String dbname, final Database db) throws MetaException, NoSuchObjectException, TException
    {
        withRetries("alterDatabase", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterDatabase(dbname, db);
                return null;
            }
        });
    }

    @Override
    public Type getType(final String name) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getType", new Callable<Type>() {
            @Override
            public Type call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getType(name);
            }
        });
    }

    @Override
    public boolean createType(final Type type) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        return withRetries("createType", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.createType(type);
            }
        });
    }

    @Override
    public boolean dropType(final String type) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("dropType", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropType(type);
            }
        });
    }

    @Override
    public Map<String, Type> getTypeAll(final String name) throws MetaException, TException
    {
        return withRetries("getTypeAll", new Callable<Map<String, Type>>() {
            @Override
            public Map<String, Type> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTypeAll(name);
            }
        });
    }

    @Override
    public List<FieldSchema> getFields(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        return withRetries("getFields", new Callable<List<FieldSchema>>() {
            @Override
            public List<FieldSchema> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getFields(dbName, tableName);
            }
        });
    }

    @Override
    public List<FieldSchema> getSchema(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        return withRetries("getSchema", new Callable<List<FieldSchema>>() {
            @Override
            public List<FieldSchema> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getSchema(dbName, tableName);
            }
        });
    }

    @Override
    public void createTable(final Table tbl) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        withRetries("createTable", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.createTable(tbl);
                return null;
            }
        });
    }

    @Override
    public void createTableWithEnvironmentContext(final Table tbl, final EnvironmentContext environmentContext) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        withRetries("createTableWithEnvironmentContext", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.createTableWithEnvironmentContext(tbl, environmentContext);
                return null;
            }
        });
    }

    @Override
    public void dropTable(final String dbname, final String name, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTable", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.dropTable(dbname, name, deleteData);
                return null;
            }
        });
    }

    @Override
    public void dropTableWithEnvironmentContext(final String dbName, final String tblName, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        withRetries("dropTableWithEnvironmentContext", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.dropTableWithEnvironmentContext(dbName, tblName, deleteData, environmentContext);
                return null;
            }
        });
    }

    @Override
    public List<String> getTables(final String dbName, final String pattern) throws MetaException, TException
    {
        return withRetries("getTables", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTables(dbName, pattern);
            }
        });
    }

    @Override
    public List<String> getAllTables(final String dbName) throws MetaException, TException
    {
        return withRetries("getAllTables", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getAllTables(dbName);
            }
        });
    }

    @Override
    public Table getTable(final String dbname, final String tblName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getTable", new Callable<Table>() {
            @Override
            public Table call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTable(dbname, tblName);
            }
        });
    }

    @Override
    public List<Table> getTableObjectsByName(final String dbname, final List<String> tblNames) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        return withRetries("getTableObjectsByName", new Callable<List<Table>>() {
            @Override
            public List<Table> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTableObjectsByName(dbname, tblNames);
            }
        });
    }

    @Override
    public List<String> getTableNamesByFilter(final String dbname, final String filter, final short maxTables) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        return withRetries("getTableNamesByFilter", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTableNamesByFilter(dbname, filter, maxTables);
            }
        });
    }

    @Override
    public void alterTable(final String dbname, final String tblName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTable", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterTable(dbname, tblName, newTbl);
                return null;
            }
        });
    }

    @Override
    public void alterTableWithEnvironmentContext(final String dbname, final String tblName, final Table newTbl, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterTableWithEnvironmentContext", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterTableWithEnvironmentContext(dbname, tblName, newTbl, environmentContext);
                return null;
            }
        });
    }

    @Override
    public Partition addPartition(final Partition newPart) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartition", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.addPartition(newPart);
            }
        });
    }

    @Override
    public Partition addPartitionWithEnvironmentContext(final Partition newPart, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartitionWithEnvironmentContext", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.addPartitionWithEnvironmentContext(newPart, environmentContext);
            }
        });
    }

    @Override
    public int addPartitions(final List<Partition> newParts) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addPartitions", new Callable<Integer>() {
            @Override
            public Integer call() throws TException
            {
                final HiveMetastore client = connect();
                return client.addPartitions(newParts);
            }
        });
    }

    @Override
    public Partition appendPartition(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("appendPartition", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.appendPartition(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public Partition appendPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        return withRetries("appendPartitionWithEnvironmentContext", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.appendPartitionWithEnvironmentContext(dbName, tblName, partVals, environmentContext);
            }
        });
    }

    @Override
    public Partition appendPartitionByName(final String dbName, final String tblName, final String partName) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("appendPartitionByName", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.appendPartitionByName(dbName, tblName, partName);
            }
        });
    }

    @Override
    public Partition appendPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        return withRetries("appendPartitionByNameWithEnvironmentContext", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.appendPartitionByNameWithEnvironmentContext(dbName, tblName, partName, environmentContext);
            }
        });
    }

    @Override
    public boolean dropPartition(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartition", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropPartition(dbName, tblName, partVals, deleteData);
            }
        });
    }

    @Override
    public boolean dropPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionWithEnvironmentContext", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropPartitionWithEnvironmentContext(dbName, tblName, partVals, deleteData, environmentContext);
            }
        });
    }

    @Override
    public boolean dropPartitionByName(final String dbName, final String tblName, final String partName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionByName", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropPartitionByName(dbName, tblName, partName, deleteData);
            }
        });
    }

    @Override
    public boolean dropPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropPartitionByNameWithEnvironmentContext", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropPartitionByNameWithEnvironmentContext(dbName, tblName, partName, deleteData, environmentContext);
            }
        });
    }

    @Override
    public Partition getPartition(final String dbName, final String tblName, final List<String> partVals) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartition", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartition(dbName, tblName, partVals);
            }
        });
    }

    @Override
    public Partition getPartitionWithAuth(final String dbName, final String tblName, final List<String> partVals, final String userName, final List<String> groupNames) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionWithAuth", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionWithAuth(dbName, tblName, partVals, userName, groupNames);
            }
        });
    }

    @Override
    public Partition getPartitionByName(final String dbName, final String tblName, final String partName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionByName", new Callable<Partition>() {
            @Override
            public Partition call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionByName(dbName, tblName, partName);
            }
        });
    }

    @Override
    public List<Partition> getPartitions(final String dbName, final String tblName, final short maxParts) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitions", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitions(dbName, tblName, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsWithAuth(final String dbName, final String tblName, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitionsWithAuth", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionsWithAuth(dbName, tblName, maxParts, userName, groupNames);
            }
        });
    }

    @Override
    public List<String> getPartitionNames(final String dbName, final String tblName, final short maxParts) throws MetaException, TException
    {
        return withRetries("getPartitionNames", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionNames(dbName, tblName, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsPs", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionsPs(dbName, tblName, partVals, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsPsWithAuth(final String dbName, final String tblName, final List<String> partVals, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getPartitionsPsWithAuth", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionsPsWithAuth(dbName, tblName, partVals, maxParts, userName, groupNames);
            }
        });
    }

    @Override
    public List<String> getPartitionNamesPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionNamesPs", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionNamesPs(dbName, tblName, partVals, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsByFilter(final String dbName, final String tblName, final String filter, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsByFilter", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionsByFilter(dbName, tblName, filter, maxParts);
            }
        });
    }

    @Override
    public List<Partition> getPartitionsByNames(final String dbName, final String tblName, final List<String> names) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getPartitionsByNames", new Callable<List<Partition>>() {
            @Override
            public List<Partition> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionsByNames(dbName, tblName, names);
            }
        });
    }

    @Override
    public void alterPartition(final String dbName, final String tblName, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartition", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterPartition(dbName, tblName, newPart);
                return null;
            }
        });
    }

    @Override
    public void alterPartitions(final String dbName, final String tblName, final List<Partition> newParts) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartitions", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterPartitions(dbName, tblName, newParts);
                return null;
            }
        });
    }

    @Override
    public void alterPartitionWithEnvironmentContext(final String dbName, final String tblName, final Partition newPart, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterPartitionWithEnvironmentContext", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterPartitionWithEnvironmentContext(dbName, tblName, newPart, environmentContext);
                return null;
            }
        });
    }

    @Override
    public void renamePartition(final String dbName, final String tblName, final List<String> partVals, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
        withRetries("renamePartition", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.renamePartition(dbName, tblName, partVals, newPart);
                return null;
            }
        });
    }

    @Override
    public boolean partitionNameHasValidCharacters(final List<String> partVals, final boolean throwException) throws MetaException, TException
    {
        return withRetries("partitionNameHasValidCharacters", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.partitionNameHasValidCharacters(partVals, throwException);
            }
        });
    }

    @Override
    public String getConfigValue(final String name, final String defaultValue) throws ConfigValSecurityException, TException
    {
        return withRetries("getConfigValue", new Callable<String>() {
            @Override
            public String call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getConfigValue(name, defaultValue);
            }
        });
    }

    @Override
    public List<String> partitionNameToVals(final String partName) throws MetaException, TException
    {
        return withRetries("partitionNameToVals", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.partitionNameToVals(partName);
            }
        });
    }

    @Override
    public Map<String, String> partitionNameToSpec(final String partName) throws MetaException, TException
    {
        return withRetries("partitionNameToSpec", new Callable<Map<String, String>>() {
            @Override
            public Map<String, String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.partitionNameToSpec(partName);
            }
        });
    }

    @Override
    public void markPartitionForEvent(final String dbName, final String tblName, final Map<String, String> partVals, final PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        UnknownPartitionException,
        InvalidPartitionException, TException
    {
        withRetries("markPartitionForEvent", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
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
        return withRetries("isPartitionMarkedForEvent", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.isPartitionMarkedForEvent(dbName, tblName, partVals, eventType);
            }
        });
    }

    @Override
    public Index addIndex(final Index newIndex, final Table indexTable) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        return withRetries("addIndex", new Callable<Index>() {
            @Override
            public Index call() throws TException
            {
                final HiveMetastore client = connect();
                return client.addIndex(newIndex, indexTable);
            }
        });
    }

    @Override
    public void alterIndex(final String dbname, final String baseTblName, final String idxName, final Index newIdx) throws InvalidOperationException, MetaException, TException
    {
        withRetries("alterIndex", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.alterIndex(dbname, baseTblName, idxName, newIdx);
                return null;
            }
        });
    }

    @Override
    public boolean dropIndexByName(final String dbName, final String tblName, final String indexName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("dropIndexByName", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropIndexByName(dbName, tblName, indexName, deleteData);
            }
        });
    }

    @Override
    public Index getIndexByName(final String dbName, final String tblName, final String indexName) throws MetaException, NoSuchObjectException, TException
    {
        return withRetries("getIndexByName", new Callable<Index>() {
            @Override
            public Index call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getIndexByName(dbName, tblName, indexName);
            }
        });
    }

    @Override
    public List<Index> getIndexes(final String dbName, final String tblName, final short maxIndexes) throws NoSuchObjectException, MetaException, TException
    {
        return withRetries("getIndexes", new Callable<List<Index>>() {
            @Override
            public List<Index> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getIndexes(dbName, tblName, maxIndexes);
            }
        });
    }

    @Override
    public List<String> getIndexNames(final String dbName, final String tblName, final short maxIndexes) throws MetaException, TException
    {
        return withRetries("getIndexNames", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getIndexNames(dbName, tblName, maxIndexes);
            }
        });
    }

    @Override
    public boolean updateTableColumnStatistics(final ColumnStatistics statsObj) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException
    {
        return withRetries("updateTableColumnStatistics", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.updateTableColumnStatistics(statsObj);
            }
        });
    }

    @Override
    public boolean updatePartitionColumnStatistics(final ColumnStatistics statsObj) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException
    {
        return withRetries("updatePartitionColumnStatistics", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.updatePartitionColumnStatistics(statsObj);
            }
        });
    }

    @Override
    public ColumnStatistics getTableColumnStatistics(final String dbName, final String tblName, final String colName) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException
    {
        return withRetries("getTableColumnStatistics", new Callable<ColumnStatistics>() {
            @Override
            public ColumnStatistics call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getTableColumnStatistics(dbName, tblName, colName);
            }
        });
    }

    @Override
    public ColumnStatistics getPartitionColumnStatistics(final String dbName, final String tblName, final String partName, final String colName) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException
    {
        return withRetries("getPartitionColumnStatistics", new Callable<ColumnStatistics>() {
            @Override
            public ColumnStatistics call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPartitionColumnStatistics(dbName, tblName, partName, colName);
            }
        });
    }

    @Override
    public boolean deletePartitionColumnStatistics(final String dbName, final String tblName, final String partName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        return withRetries("deletePartitionColumnStatistics", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.deletePartitionColumnStatistics(dbName, tblName, partName, colName);
            }
        });
    }

    @Override
    public boolean deleteTableColumnStatistics(final String dbName, final String tblName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        return withRetries("deleteTableColumnStatistics", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.deleteTableColumnStatistics(dbName, tblName, colName);
            }
        });
    }

    @Override
    public boolean createRole(final Role role) throws MetaException, TException
    {
        return withRetries("createRole", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.createRole(role);
            }
        });
    }

    @Override
    public boolean dropRole(final String roleName) throws MetaException, TException
    {
        return withRetries("dropRole", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.dropRole(roleName);
            }
        });
    }

    @Override
    public List<String> getRoleNames() throws MetaException, TException
    {
        return withRetries("getRoleNames", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getRoleNames();
            }
        });
    }

    @Override
    public boolean grantRole(final String roleName, final String principalName, final PrincipalType principalType, final String grantor, final PrincipalType grantorType, final boolean grantOption) throws MetaException, TException
    {
        return withRetries("grantRole", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.grantRole(roleName, principalName, principalType, grantor, grantorType, grantOption);
            }
        });
    }

    @Override
    public boolean revokeRole(final String roleName, final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        return withRetries("revokeRole", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.revokeRole(roleName, principalName, principalType);
            }
        });
    }

    @Override
    public List<Role> listRoles(final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        return withRetries("listRoles", new Callable<List<Role>>() {
            @Override
            public List<Role> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.listRoles(principalName, principalType);
            }
        });
    }

    @Override
    public PrincipalPrivilegeSet getPrivilegeSet(final HiveObjectRef hiveObject, final String userName, final List<String> groupNames) throws MetaException, TException
    {
        return withRetries("getPrivilegeSet", new Callable<PrincipalPrivilegeSet>() {
            @Override
            public PrincipalPrivilegeSet call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getPrivilegeSet(hiveObject, userName, groupNames);
            }
        });
    }

    @Override
    public List<HiveObjectPrivilege> listPrivileges(final String principalName, final PrincipalType principalType, final HiveObjectRef hiveObject) throws MetaException, TException
    {
        return withRetries("listPrivileges", new Callable<List<HiveObjectPrivilege>>() {
            @Override
            public List<HiveObjectPrivilege> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.listPrivileges(principalName, principalType, hiveObject);
            }
        });
    }

    @Override
    public boolean grantPrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        return withRetries("grantPrivileges", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.grantPrivileges(privileges);
            }
        });
    }

    @Override
    public boolean revokePrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        return withRetries("revokePrivileges", new Callable<Boolean>() {
            @Override
            public Boolean call() throws TException
            {
                final HiveMetastore client = connect();
                return client.revokePrivileges(privileges);
            }
        });
    }

    @Override
    public List<String> setUgi(final String userName, final List<String> groupNames) throws MetaException, TException
    {
        return withRetries("setUgi", new Callable<List<String>>() {
            @Override
            public List<String> call() throws TException
            {
                final HiveMetastore client = connect();
                return client.setUgi(userName, groupNames);
            }
        });
    }

    @Override
    public String getDelegationToken(final String tokenOwner, final String renewerKerberosPrincipalName) throws MetaException, TException
    {
        return withRetries("getDelegationToken", new Callable<String>() {
            @Override
            public String call() throws TException
            {
                final HiveMetastore client = connect();
                return client.getDelegationToken(tokenOwner, renewerKerberosPrincipalName);
            }
        });
    }

    @Override
    public long renewDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        return withRetries("renewDelegationToken", new Callable<Long>() {
            @Override
            public Long call() throws TException
            {
                final HiveMetastore client = connect();
                return client.renewDelegationToken(tokenStrForm);
            }
        });
    }

    @Override
    public void cancelDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        withRetries("cancelDelegationToken", new Callable<Void>() {
            @Override
            public Void call() throws TException
            {
                final HiveMetastore client = connect();
                client.cancelDelegationToken(tokenStrForm);
                return null;
            }
        });
    }
}
