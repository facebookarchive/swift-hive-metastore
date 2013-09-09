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
package com.facebook.hive.metastore;

import com.facebook.hive.metastore.api.ThriftHiveMetastore;

import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
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

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public abstract class AbstractLegacyMetastore implements ThriftHiveMetastore
{
    @Override
    public void close()
    {
    }

    @Override
    public void create_database(Database database) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Database get_database(String name) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drop_database(String name, boolean deleteData, boolean cascade) throws NoSuchObjectException, InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_databases(String pattern) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_all_databases() throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_database(String dbname, Database db) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type get_type(String name) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean create_type(Type type) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_type(String type) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Type> get_type_all(String name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldSchema> get_fields(String db_name, String table_name) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldSchema> get_schema(String db_name, String table_name) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create_table(Table tbl) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create_table_with_environment_context(Table tbl, EnvironmentContext environment_context) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create_table_link(String dbName, String targetDbName, String targetTableName, String owner, boolean isStatic, Map<String, String> linkProperties) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException,
        InvalidTableLinkDescriptionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drop_table(String dbname, String name, boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drop_table_link(String dbName, String targetDbName, String targetTableName) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drop_table_with_environment_context(String dbname, String name, boolean deleteData, EnvironmentContext environment_context) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_tables(String db_name, String pattern) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_all_tables(String db_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table get_table(String dbname, String tbl_name) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists_table(String dbname, String tbl_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table get_table_link(String dbName, String targetDbName, String targetTableName) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Table> get_table_objects_by_name(String dbname, List<String> tbl_names) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_table_names_by_filter(String dbname, String filter, short max_tables) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_table(String dbname, String tbl_name, Table new_tbl) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_table_with_environment_context(String dbname, String tbl_name, Table new_tbl, EnvironmentContext environment_context) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_table_link(String dbName, String targetDbName, String targetTableName, Table new_tbl) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_table_link_properties(String dbName, String targetDbName, String targetTableName, Map<String, String> updatedProperties) throws InvalidOperationException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition add_partition(Partition new_part) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition add_partition_with_environment_context(Partition new_part, EnvironmentContext environment_context) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition add_table_link_partition(String dbName, String targetDbName, String targetTableName, String partitionName) throws InvalidObjectException, AlreadyExistsException, NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int add_partitions(List<Partition> new_parts) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition append_partition(String db_name, String tbl_name, List<String> part_vals) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition append_partition_with_environment_context(String db_name, String tbl_name, List<String> part_vals, EnvironmentContext environment_context) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition append_partition_by_name(String db_name, String tbl_name, String part_name) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition append_partition_by_name_with_environment_context(String db_name, String tbl_name, String part_name, EnvironmentContext environment_context) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_partition(String db_name, String tbl_name, List<String> part_vals, boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_partition_with_environment_context(String db_name, String tbl_name, List<String> part_vals, boolean deleteData, EnvironmentContext environment_context) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_partition_by_name(String db_name, String tbl_name, String part_name, boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_partition_by_name_with_environment_context(String db_name, String tbl_name, String part_name, boolean deleteData, EnvironmentContext environment_context) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_table_link_partition(String dbName, String targetDbName, String targetTableName, String partitionName) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition get_partition_template(String db_name, String tbl_name, List<String> part_vals) throws InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition get_partition(String db_name, String tbl_name, List<String> part_vals) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> exchange_partition(Map<String, String> partitionSpecs, String source_db, String source_table_name, String dest_db, String dest_table_name, boolean overwrite) throws MetaException, NoSuchObjectException, InvalidObjectException,
        InvalidInputException, AlreadyExistsException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition get_partition_with_auth(String db_name, String tbl_name, List<String> part_vals, String user_name, List<String> group_names) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition get_partition_by_name(String db_name, String tbl_name, String part_name) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions(String db_name, String tbl_name, short max_parts) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions_with_auth(String db_name, String tbl_name, short max_parts, String user_name, List<String> group_names) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_partition_names(String db_name, String tbl_name, short max_parts) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int get_total_partitions(String db_name, String tbl_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions_ps(String db_name, String tbl_name, List<String> part_vals, short max_parts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions_ps_with_auth(String db_name, String tbl_name, List<String> part_vals, short max_parts, String user_name, List<String> group_names) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_partition_names_ps(String db_name, String tbl_name, List<String> part_vals, short max_parts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions_by_filter(String db_name, String tbl_name, String filter, short max_parts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> get_partitions_by_names(String db_name, String tbl_name, List<String> names) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_partition(String db_name, String tbl_name, Partition new_part) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_partitions(String db_name, String tbl_name, List<Partition> new_parts) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_partition_with_environment_context(String db_name, String tbl_name, Partition new_part, EnvironmentContext environment_context) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename_partition(String db_name, String tbl_name, List<String> part_vals, Partition new_part) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean partition_name_has_valid_characters(List<String> part_vals, boolean throw_exception) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String get_config_value(String name, String defaultValue) throws ConfigValSecurityException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> partition_name_to_vals(String part_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> partition_name_to_spec(String part_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markPartitionForEvent(String db_name, String tbl_name, Map<String, String> part_vals, PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException,
        InvalidPartitionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartitionMarkedForEvent(String db_name, String tbl_name, Map<String, String> part_vals, PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException,
        InvalidPartitionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index add_index(Index new_index, Table index_table) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alter_index(String dbname, String base_tbl_name, String idx_name, Index new_idx) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_index_by_name(String db_name, String tbl_name, String index_name, boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index get_index_by_name(String db_name, String tbl_name, String index_name) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Index> get_indexes(String db_name, String tbl_name, short max_indexes) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_index_names(String db_name, String tbl_name, short max_indexes) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete_partition_column_statistics(String db_name, String tbl_name, String part_name, String col_name) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete_table_column_statistics(String db_name, String tbl_name, String col_name) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean create_role(Role role) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drop_role(String role_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> get_role_names() throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grant_role(String role_name, String principal_name, PrincipalType principal_type, String grantor, PrincipalType grantorType, boolean grant_option) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean revoke_role(String role_name, String principal_name, PrincipalType principal_type) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Role> list_roles(String principal_name, PrincipalType principal_type) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrincipalPrivilegeSet get_privilege_set(HiveObjectRef hiveObject, String user_name, List<String> group_names) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HiveObjectPrivilege> list_privileges(String principal_name, PrincipalType principal_type, HiveObjectRef hiveObject) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grant_privileges(PrivilegeBag privileges) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean revoke_privileges(PrivilegeBag privileges) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> set_ugi(String user_name, List<String> group_names) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String get_delegation_token(String token_owner, String renewer_kerberos_principal_name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long renew_delegation_token(String token_str_form) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancel_delegation_token(String token_str_form) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }
}
