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
package org.apache.hadoop.hive.metastore.api;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

@ThriftService("FacebookHiveMetastore")
public interface FacebookHiveMetastore extends org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore, Closeable
{
    @Override
    void close();

    @ThriftMethod(value = "exchange_partition",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2),
                                    @ThriftException(type = InvalidObjectException.class, id = 3),
                                    @ThriftException(type = InvalidInputException.class, id = 4),
                                    @ThriftException(type = AlreadyExistsException.class, id = 5)
                    })
    List<Partition> exchangePartition(
                                      @ThriftField(value = 1, name = "partitionSpecs") final Map<String, String> partitionSpecs,
                                      @ThriftField(value = 2, name = "source_db") final String sourceDb,
                                      @ThriftField(value = 3, name = "source_table_name") final String sourceTableName,
                                      @ThriftField(value = 4, name = "dest_db") final String destDb,
                                      @ThriftField(value = 5, name = "dest_table_name") final String destTableName,
                                      @ThriftField(value = 6, name = "overwrite") final boolean overwrite
                    ) throws MetaException, NoSuchObjectException, InvalidObjectException, InvalidInputException, AlreadyExistsException, org.apache.thrift.TException;

    @ThriftMethod(value = "create_table_link",
                    exception = {
                                    @ThriftException(type = AlreadyExistsException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3),
                                    @ThriftException(type = NoSuchObjectException.class, id = 4),
                                    @ThriftException(type = InvalidTableLinkDescriptionException.class, id = 5)
                    })
    void createTableLink(
                         @ThriftField(value = 1, name = "dbName") final String dbName,
                         @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                         @ThriftField(value = 3, name = "targetTableName") final String targetTableName,
                         @ThriftField(value = 4, name = "owner") final String owner,
                         @ThriftField(value = 5, name = "isStatic") final boolean isStatic,
                         @ThriftField(value = 6, name = "linkProperties") final Map<String, String> linkProperties
                    ) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, InvalidTableLinkDescriptionException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_table_link",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void dropTableLink(
                       @ThriftField(value = 1, name = "dbName") final String dbName,
                       @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                       @ThriftField(value = 3, name = "targetTableName") final String targetTableName
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "exists_table",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean existsTable(
                        @ThriftField(value = 1, name = "dbname") final String dbname,
                        @ThriftField(value = 2, name = "tbl_name") final String tblName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_table_link",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Table getTableLink(
                       @ThriftField(value = 1, name = "dbName") final String dbName,
                       @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                       @ThriftField(value = 3, name = "targetTableName") final String targetTableName
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_table_link",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterTableLink(
                        @ThriftField(value = 1, name = "dbName") final String dbName,
                        @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                        @ThriftField(value = 3, name = "targetTableName") final String targetTableName,
                        @ThriftField(value = 4, name = "new_tbl") final Table newTbl
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_table_link_properties",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2),
                                    @ThriftException(type = NoSuchObjectException.class, id = 3)
                    })
    void alterTableLinkProperties(
                                  @ThriftField(value = 1, name = "dbName") final String dbName,
                                  @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                                  @ThriftField(value = 3, name = "targetTableName") final String targetTableName,
                                  @ThriftField(value = 4, name = "updatedProperties") final Map<String, String> updatedProperties
                    ) throws InvalidOperationException, MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "add_table_link_partition",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = NoSuchObjectException.class, id = 3),
                                    @ThriftException(type = MetaException.class, id = 4)
                    })
    Partition addTableLinkPartition(
                                    @ThriftField(value = 1, name = "dbName") final String dbName,
                                    @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                                    @ThriftField(value = 3, name = "targetTableName") final String targetTableName,
                                    @ThriftField(value = 4, name = "partitionName") final String partitionName
                    ) throws InvalidObjectException, AlreadyExistsException, NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_table_link_partition",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropTableLinkPartition(
                                   @ThriftField(value = 1, name = "dbName") final String dbName,
                                   @ThriftField(value = 2, name = "targetDbName") final String targetDbName,
                                   @ThriftField(value = 3, name = "targetTableName") final String targetTableName,
                                   @ThriftField(value = 4, name = "partitionName") final String partitionName
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_template",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    Partition getPartitionTemplate(
                                   @ThriftField(value = 1, name = "db_name") final String dbName,
                                   @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                   @ThriftField(value = 3, name = "part_vals") final List<String> partVals
                    ) throws InvalidObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_total_partitions",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    int getTotalPartitions(
                           @ThriftField(value = 1, name = "db_name") final String dbName,
                           @ThriftField(value = 2, name = "tbl_name") final String tblName
                    ) throws MetaException, org.apache.thrift.TException;
}
