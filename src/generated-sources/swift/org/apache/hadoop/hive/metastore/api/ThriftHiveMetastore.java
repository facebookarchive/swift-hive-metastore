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

@ThriftService("ThriftHiveMetastore")
public interface ThriftHiveMetastore extends Closeable
{
    @Override
    void close();

    @ThriftMethod(value = "create_database",
                    exception = {
                                    @ThriftException(type = AlreadyExistsException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    void createDatabase(
                        @ThriftField(value = 1, name = "database") final Database database
                    ) throws AlreadyExistsException, InvalidObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_database",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    Database getDatabase(
                         @ThriftField(value = 1, name = "name") final String name
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_database",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = InvalidOperationException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    void dropDatabase(
                      @ThriftField(value = 1, name = "name") final String name,
                      @ThriftField(value = 2, name = "deleteData") final boolean deleteData,
                      @ThriftField(value = 3, name = "cascade") final boolean cascade
                    ) throws NoSuchObjectException, InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_databases",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getDatabases(
                              @ThriftField(value = 1, name = "pattern") final String pattern
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_all_databases",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getAllDatabases() throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_database",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    void alterDatabase(
                       @ThriftField(value = 1, name = "dbname") final String dbname,
                       @ThriftField(value = 2, name = "db") final Database db
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_type",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Type getType(
                 @ThriftField(value = 1, name = "name") final String name
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "create_type",
                    exception = {
                                    @ThriftException(type = AlreadyExistsException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    boolean createType(
                       @ThriftField(value = 1, name = "type") final Type type
                    ) throws AlreadyExistsException, InvalidObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_type",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    boolean dropType(
                     @ThriftField(value = 1, name = "type") final String type
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_type_all",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    Map<String, Type> getTypeAll(
                                 @ThriftField(value = 1, name = "name") final String name
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_fields",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = UnknownTableException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3)
                    })
    List<FieldSchema> getFields(
                                @ThriftField(value = 1, name = "db_name") final String dbName,
                                @ThriftField(value = 2, name = "table_name") final String tableName
                    ) throws MetaException, UnknownTableException, UnknownDBException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_schema",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = UnknownTableException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3)
                    })
    List<FieldSchema> getSchema(
                                @ThriftField(value = 1, name = "db_name") final String dbName,
                                @ThriftField(value = 2, name = "table_name") final String tableName
                    ) throws MetaException, UnknownTableException, UnknownDBException, org.apache.thrift.TException;

    @ThriftMethod(value = "create_table",
                    exception = {
                                    @ThriftException(type = AlreadyExistsException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3),
                                    @ThriftException(type = NoSuchObjectException.class, id = 4)
                    })
    void createTable(
                     @ThriftField(value = 1, name = "tbl") final Table tbl
                    ) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "create_table_with_environment_context",
                    exception = {
                                    @ThriftException(type = AlreadyExistsException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3),
                                    @ThriftException(type = NoSuchObjectException.class, id = 4)
                    })
    void createTableWithEnvironmentContext(
                                           @ThriftField(value = 1, name = "tbl") final Table tbl,
                                           @ThriftField(value = 2, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_table",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void dropTable(
                   @ThriftField(value = 1, name = "dbname") final String dbname,
                   @ThriftField(value = 2, name = "name") final String name,
                   @ThriftField(value = 3, name = "deleteData") final boolean deleteData
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_table_with_environment_context",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void dropTableWithEnvironmentContext(
                                         @ThriftField(value = 1, name = "dbname") final String dbname,
                                         @ThriftField(value = 2, name = "name") final String name,
                                         @ThriftField(value = 3, name = "deleteData") final boolean deleteData,
                                         @ThriftField(value = 4, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_tables",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getTables(
                           @ThriftField(value = 1, name = "db_name") final String dbName,
                           @ThriftField(value = 2, name = "pattern") final String pattern
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_all_tables",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getAllTables(
                              @ThriftField(value = 1, name = "db_name") final String dbName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_table",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Table getTable(
                   @ThriftField(value = 1, name = "dbname") final String dbname,
                   @ThriftField(value = 2, name = "tbl_name") final String tblName
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_table_objects_by_name",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = InvalidOperationException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3)
                    })
    List<Table> getTableObjectsByName(
                                      @ThriftField(value = 1, name = "dbname") final String dbname,
                                      @ThriftField(value = 2, name = "tbl_names") final List<String> tblNames
                    ) throws MetaException, InvalidOperationException, UnknownDBException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_table_names_by_filter",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = InvalidOperationException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3)
                    })
    List<String> getTableNamesByFilter(
                                       @ThriftField(value = 1, name = "dbname") final String dbname,
                                       @ThriftField(value = 2, name = "filter") final String filter,
                                       @ThriftField(value = 3, name = "max_tables") final short maxTables
                    ) throws MetaException, InvalidOperationException, UnknownDBException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_table",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterTable(
                    @ThriftField(value = 1, name = "dbname") final String dbname,
                    @ThriftField(value = 2, name = "tbl_name") final String tblName,
                    @ThriftField(value = 3, name = "new_tbl") final Table newTbl
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_table_with_environment_context",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterTableWithEnvironmentContext(
                                          @ThriftField(value = 1, name = "dbname") final String dbname,
                                          @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                          @ThriftField(value = 3, name = "new_tbl") final Table newTbl,
                                          @ThriftField(value = 4, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "add_partition",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition addPartition(
                           @ThriftField(value = 1, name = "new_part") final Partition newPart
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "add_partition_with_environment_context",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition addPartitionWithEnvironmentContext(
                                                 @ThriftField(value = 1, name = "new_part") final Partition newPart,
                                                 @ThriftField(value = 2, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "add_partitions",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    int addPartitions(
                      @ThriftField(value = 1, name = "new_parts") final List<Partition> newParts
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "append_partition",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition appendPartition(
                              @ThriftField(value = 1, name = "db_name") final String dbName,
                              @ThriftField(value = 2, name = "tbl_name") final String tblName,
                              @ThriftField(value = 3, name = "part_vals") final List<String> partVals
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "append_partition_with_environment_context",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition appendPartitionWithEnvironmentContext(
                                                    @ThriftField(value = 1, name = "db_name") final String dbName,
                                                    @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                                    @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                                    @ThriftField(value = 4, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "append_partition_by_name",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition appendPartitionByName(
                                    @ThriftField(value = 1, name = "db_name") final String dbName,
                                    @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                    @ThriftField(value = 3, name = "part_name") final String partName
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "append_partition_by_name_with_environment_context",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Partition appendPartitionByNameWithEnvironmentContext(
                                                          @ThriftField(value = 1, name = "db_name") final String dbName,
                                                          @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                                          @ThriftField(value = 3, name = "part_name") final String partName,
                                                          @ThriftField(value = 4, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_partition",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropPartition(
                          @ThriftField(value = 1, name = "db_name") final String dbName,
                          @ThriftField(value = 2, name = "tbl_name") final String tblName,
                          @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                          @ThriftField(value = 4, name = "deleteData") final boolean deleteData
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_partition_with_environment_context",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropPartitionWithEnvironmentContext(
                                                @ThriftField(value = 1, name = "db_name") final String dbName,
                                                @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                                @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                                @ThriftField(value = 4, name = "deleteData") final boolean deleteData,
                                                @ThriftField(value = 5, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_partition_by_name",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropPartitionByName(
                                @ThriftField(value = 1, name = "db_name") final String dbName,
                                @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                @ThriftField(value = 3, name = "part_name") final String partName,
                                @ThriftField(value = 4, name = "deleteData") final boolean deleteData
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_partition_by_name_with_environment_context",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropPartitionByNameWithEnvironmentContext(
                                                      @ThriftField(value = 1, name = "db_name") final String dbName,
                                                      @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                                      @ThriftField(value = 3, name = "part_name") final String partName,
                                                      @ThriftField(value = 4, name = "deleteData") final boolean deleteData,
                                                      @ThriftField(value = 5, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Partition getPartition(
                           @ThriftField(value = 1, name = "db_name") final String dbName,
                           @ThriftField(value = 2, name = "tbl_name") final String tblName,
                           @ThriftField(value = 3, name = "part_vals") final List<String> partVals
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_with_auth",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Partition getPartitionWithAuth(
                                   @ThriftField(value = 1, name = "db_name") final String dbName,
                                   @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                   @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                   @ThriftField(value = 4, name = "user_name") final String userName,
                                   @ThriftField(value = 5, name = "group_names") final List<String> groupNames
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_by_name",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Partition getPartitionByName(
                                 @ThriftField(value = 1, name = "db_name") final String dbName,
                                 @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                 @ThriftField(value = 3, name = "part_name") final String partName
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    List<Partition> getPartitions(
                                  @ThriftField(value = 1, name = "db_name") final String dbName,
                                  @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                  @ThriftField(value = 3, name = "max_parts") final short maxParts
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions_with_auth",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    List<Partition> getPartitionsWithAuth(
                                          @ThriftField(value = 1, name = "db_name") final String dbName,
                                          @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                          @ThriftField(value = 3, name = "max_parts") final short maxParts,
                                          @ThriftField(value = 4, name = "user_name") final String userName,
                                          @ThriftField(value = 5, name = "group_names") final List<String> groupNames
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_names",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getPartitionNames(
                                   @ThriftField(value = 1, name = "db_name") final String dbName,
                                   @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                   @ThriftField(value = 3, name = "max_parts") final short maxParts
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions_ps",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    List<Partition> getPartitionsPs(
                                    @ThriftField(value = 1, name = "db_name") final String dbName,
                                    @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                    @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                    @ThriftField(value = 4, name = "max_parts") final short maxParts
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions_ps_with_auth",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    List<Partition> getPartitionsPsWithAuth(
                                            @ThriftField(value = 1, name = "db_name") final String dbName,
                                            @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                            @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                            @ThriftField(value = 4, name = "max_parts") final short maxParts,
                                            @ThriftField(value = 5, name = "user_name") final String userName,
                                            @ThriftField(value = 6, name = "group_names") final List<String> groupNames
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_names_ps",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    List<String> getPartitionNamesPs(
                                     @ThriftField(value = 1, name = "db_name") final String dbName,
                                     @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                     @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                                     @ThriftField(value = 4, name = "max_parts") final short maxParts
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions_by_filter",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    List<Partition> getPartitionsByFilter(
                                          @ThriftField(value = 1, name = "db_name") final String dbName,
                                          @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                          @ThriftField(value = 3, name = "filter") final String filter,
                                          @ThriftField(value = 4, name = "max_parts") final short maxParts
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partitions_by_names",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    List<Partition> getPartitionsByNames(
                                         @ThriftField(value = 1, name = "db_name") final String dbName,
                                         @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                         @ThriftField(value = 3, name = "names") final List<String> names
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_partition",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterPartition(
                        @ThriftField(value = 1, name = "db_name") final String dbName,
                        @ThriftField(value = 2, name = "tbl_name") final String tblName,
                        @ThriftField(value = 3, name = "new_part") final Partition newPart
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_partitions",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterPartitions(
                         @ThriftField(value = 1, name = "db_name") final String dbName,
                         @ThriftField(value = 2, name = "tbl_name") final String tblName,
                         @ThriftField(value = 3, name = "new_parts") final List<Partition> newParts
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_partition_with_environment_context",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterPartitionWithEnvironmentContext(
                                              @ThriftField(value = 1, name = "db_name") final String dbName,
                                              @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                              @ThriftField(value = 3, name = "new_part") final Partition newPart,
                                              @ThriftField(value = 4, name = "environment_context") final EnvironmentContext environmentContext
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "rename_partition",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void renamePartition(
                         @ThriftField(value = 1, name = "db_name") final String dbName,
                         @ThriftField(value = 2, name = "tbl_name") final String tblName,
                         @ThriftField(value = 3, name = "part_vals") final List<String> partVals,
                         @ThriftField(value = 4, name = "new_part") final Partition newPart
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "partition_name_has_valid_characters",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean partitionNameHasValidCharacters(
                                            @ThriftField(value = 1, name = "part_vals") final List<String> partVals,
                                            @ThriftField(value = 2, name = "throw_exception") final boolean throwException
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_config_value",
                    exception = {
                                    @ThriftException(type = ConfigValSecurityException.class, id = 1)
                    })
    String getConfigValue(
                          @ThriftField(value = 1, name = "name") final String name,
                          @ThriftField(value = 2, name = "defaultValue") final String defaultValue
                    ) throws ConfigValSecurityException, org.apache.thrift.TException;

    @ThriftMethod(value = "partition_name_to_vals",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> partitionNameToVals(
                                     @ThriftField(value = 1, name = "part_name") final String partName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "partition_name_to_spec",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    Map<String, String> partitionNameToSpec(
                                            @ThriftField(value = 1, name = "part_name") final String partName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "markPartitionForEvent",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3),
                                    @ThriftException(type = UnknownTableException.class, id = 4),
                                    @ThriftException(type = UnknownPartitionException.class, id = 5),
                                    @ThriftException(type = InvalidPartitionException.class, id = 6)
                    })
    void markPartitionForEvent(
                               @ThriftField(value = 1, name = "db_name") final String dbName,
                               @ThriftField(value = 2, name = "tbl_name") final String tblName,
                               @ThriftField(value = 3, name = "part_vals") final Map<String, String> partVals,
                               @ThriftField(value = 4, name = "eventType") final PartitionEventType eventType
                    ) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException, InvalidPartitionException, org.apache.thrift.TException;

    @ThriftMethod(value = "isPartitionMarkedForEvent",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2),
                                    @ThriftException(type = UnknownDBException.class, id = 3),
                                    @ThriftException(type = UnknownTableException.class, id = 4),
                                    @ThriftException(type = UnknownPartitionException.class, id = 5),
                                    @ThriftException(type = InvalidPartitionException.class, id = 6)
                    })
    boolean isPartitionMarkedForEvent(
                                      @ThriftField(value = 1, name = "db_name") final String dbName,
                                      @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                      @ThriftField(value = 3, name = "part_vals") final Map<String, String> partVals,
                                      @ThriftField(value = 4, name = "eventType") final PartitionEventType eventType
                    ) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException, InvalidPartitionException, org.apache.thrift.TException;

    @ThriftMethod(value = "add_index",
                    exception = {
                                    @ThriftException(type = InvalidObjectException.class, id = 1),
                                    @ThriftException(type = AlreadyExistsException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3)
                    })
    Index addIndex(
                   @ThriftField(value = 1, name = "new_index") final Index newIndex,
                   @ThriftField(value = 2, name = "index_table") final Table indexTable
                    ) throws InvalidObjectException, AlreadyExistsException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "alter_index",
                    exception = {
                                    @ThriftException(type = InvalidOperationException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    void alterIndex(
                    @ThriftField(value = 1, name = "dbname") final String dbname,
                    @ThriftField(value = 2, name = "base_tbl_name") final String baseTblName,
                    @ThriftField(value = 3, name = "idx_name") final String idxName,
                    @ThriftField(value = 4, name = "new_idx") final Index newIdx
                    ) throws InvalidOperationException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_index_by_name",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    boolean dropIndexByName(
                            @ThriftField(value = 1, name = "db_name") final String dbName,
                            @ThriftField(value = 2, name = "tbl_name") final String tblName,
                            @ThriftField(value = 3, name = "index_name") final String indexName,
                            @ThriftField(value = 4, name = "deleteData") final boolean deleteData
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_index_by_name",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1),
                                    @ThriftException(type = NoSuchObjectException.class, id = 2)
                    })
    Index getIndexByName(
                         @ThriftField(value = 1, name = "db_name") final String dbName,
                         @ThriftField(value = 2, name = "tbl_name") final String tblName,
                         @ThriftField(value = 3, name = "index_name") final String indexName
                    ) throws MetaException, NoSuchObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_indexes",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2)
                    })
    List<Index> getIndexes(
                           @ThriftField(value = 1, name = "db_name") final String dbName,
                           @ThriftField(value = 2, name = "tbl_name") final String tblName,
                           @ThriftField(value = 3, name = "max_indexes") final short maxIndexes
                    ) throws NoSuchObjectException, MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_index_names",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getIndexNames(
                               @ThriftField(value = 1, name = "db_name") final String dbName,
                               @ThriftField(value = 2, name = "tbl_name") final String tblName,
                               @ThriftField(value = 3, name = "max_indexes") final short maxIndexes
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "update_table_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3),
                                    @ThriftException(type = InvalidInputException.class, id = 4)
                    })
    boolean updateTableColumnStatistics(
                                        @ThriftField(value = 1, name = "stats_obj") final ColumnStatistics statsObj
                    ) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, org.apache.thrift.TException;

    @ThriftMethod(value = "update_partition_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = InvalidObjectException.class, id = 2),
                                    @ThriftException(type = MetaException.class, id = 3),
                                    @ThriftException(type = InvalidInputException.class, id = 4)
                    })
    boolean updatePartitionColumnStatistics(
                                            @ThriftField(value = 1, name = "stats_obj") final ColumnStatistics statsObj
                    ) throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_table_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2),
                                    @ThriftException(type = InvalidInputException.class, id = 3),
                                    @ThriftException(type = InvalidObjectException.class, id = 4)
                    })
    ColumnStatistics getTableColumnStatistics(
                                              @ThriftField(value = 1, name = "db_name") final String dbName,
                                              @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                              @ThriftField(value = 3, name = "col_name") final String colName
                    ) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_partition_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2),
                                    @ThriftException(type = InvalidInputException.class, id = 3),
                                    @ThriftException(type = InvalidObjectException.class, id = 4)
                    })
    ColumnStatistics getPartitionColumnStatistics(
                                                  @ThriftField(value = 1, name = "db_name") final String dbName,
                                                  @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                                  @ThriftField(value = 3, name = "part_name") final String partName,
                                                  @ThriftField(value = 4, name = "col_name") final String colName
                    ) throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, org.apache.thrift.TException;

    @ThriftMethod(value = "delete_partition_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2),
                                    @ThriftException(type = InvalidObjectException.class, id = 3),
                                    @ThriftException(type = InvalidInputException.class, id = 4)
                    })
    boolean deletePartitionColumnStatistics(
                                            @ThriftField(value = 1, name = "db_name") final String dbName,
                                            @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                            @ThriftField(value = 3, name = "part_name") final String partName,
                                            @ThriftField(value = 4, name = "col_name") final String colName
                    ) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, org.apache.thrift.TException;

    @ThriftMethod(value = "delete_table_column_statistics",
                    exception = {
                                    @ThriftException(type = NoSuchObjectException.class, id = 1),
                                    @ThriftException(type = MetaException.class, id = 2),
                                    @ThriftException(type = InvalidObjectException.class, id = 3),
                                    @ThriftException(type = InvalidInputException.class, id = 4)
                    })
    boolean deleteTableColumnStatistics(
                                        @ThriftField(value = 1, name = "db_name") final String dbName,
                                        @ThriftField(value = 2, name = "tbl_name") final String tblName,
                                        @ThriftField(value = 3, name = "col_name") final String colName
                    ) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, org.apache.thrift.TException;

    @ThriftMethod(value = "create_role",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean createRole(
                       @ThriftField(value = 1, name = "role") final Role role
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "drop_role",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean dropRole(
                     @ThriftField(value = 1, name = "role_name") final String roleName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_role_names",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> getRoleNames() throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "grant_role",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean grantRole(
                      @ThriftField(value = 1, name = "role_name") final String roleName,
                      @ThriftField(value = 2, name = "principal_name") final String principalName,
                      @ThriftField(value = 3, name = "principal_type") final PrincipalType principalType,
                      @ThriftField(value = 4, name = "grantor") final String grantor,
                      @ThriftField(value = 5, name = "grantorType") final PrincipalType grantorType,
                      @ThriftField(value = 6, name = "grant_option") final boolean grantOption
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "revoke_role",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean revokeRole(
                       @ThriftField(value = 1, name = "role_name") final String roleName,
                       @ThriftField(value = 2, name = "principal_name") final String principalName,
                       @ThriftField(value = 3, name = "principal_type") final PrincipalType principalType
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "list_roles",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<Role> listRoles(
                         @ThriftField(value = 1, name = "principal_name") final String principalName,
                         @ThriftField(value = 2, name = "principal_type") final PrincipalType principalType
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_privilege_set",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    PrincipalPrivilegeSet getPrivilegeSet(
                                          @ThriftField(value = 1, name = "hiveObject") final HiveObjectRef hiveObject,
                                          @ThriftField(value = 2, name = "user_name") final String userName,
                                          @ThriftField(value = 3, name = "group_names") final List<String> groupNames
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "list_privileges",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<HiveObjectPrivilege> listPrivileges(
                                             @ThriftField(value = 1, name = "principal_name") final String principalName,
                                             @ThriftField(value = 2, name = "principal_type") final PrincipalType principalType,
                                             @ThriftField(value = 3, name = "hiveObject") final HiveObjectRef hiveObject
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "grant_privileges",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean grantPrivileges(
                            @ThriftField(value = 1, name = "privileges") final PrivilegeBag privileges
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "revoke_privileges",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    boolean revokePrivileges(
                             @ThriftField(value = 1, name = "privileges") final PrivilegeBag privileges
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "set_ugi",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    List<String> setUgi(
                        @ThriftField(value = 1, name = "user_name") final String userName,
                        @ThriftField(value = 2, name = "group_names") final List<String> groupNames
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "get_delegation_token",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    String getDelegationToken(
                              @ThriftField(value = 1, name = "token_owner") final String tokenOwner,
                              @ThriftField(value = 2, name = "renewer_kerberos_principal_name") final String renewerKerberosPrincipalName
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "renew_delegation_token",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    long renewDelegationToken(
                              @ThriftField(value = 1, name = "token_str_form") final String tokenStrForm
                    ) throws MetaException, org.apache.thrift.TException;

    @ThriftMethod(value = "cancel_delegation_token",
                    exception = {
                                    @ThriftException(type = MetaException.class, id = 1)
                    })
    void cancelDelegationToken(
                               @ThriftField(value = 1, name = "token_str_form") final String tokenStrForm
                    ) throws MetaException, org.apache.thrift.TException;
}
