/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.
 *
 * This file is licensed to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.hive.metastore;

import com.facebook.hive.metastore.client.HiveMetastore;

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

import java.util.List;
import java.util.Map;

public abstract class AbstractMetastore implements HiveMetastore
{
    @Override
    public void close()
    {
    }

    @Override
    public Table getTable(final String dbname, final String tblName) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsTable(final String dbname, final String tblName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDatabase(final Database database) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Database getDatabase(final String name) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dropDatabase(final String name, final boolean deleteData, final boolean cascade) throws NoSuchObjectException, InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getDatabases(final String pattern) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllDatabases() throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterDatabase(final String dbname, final Database db) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType(final String name) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createType(final Type type) throws AlreadyExistsException, InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropType(final String type) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Type> getTypeAll(final String name) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldSchema> getFields(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldSchema> getSchema(final String dbName, final String tableName) throws MetaException, UnknownTableException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTable(final Table tbl) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTableWithEnvironmentContext(final Table tbl, final EnvironmentContext environmentContext) throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTableLink(final String dbName, final String targetDbName, final String targetTableName, final String owner, final boolean isStatic, final Map<String, String> linkProperties) throws AlreadyExistsException, InvalidObjectException,
        MetaException, NoSuchObjectException,
        InvalidTableLinkDescriptionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dropTable(final String dbname, final String name, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dropTableLink(final String dbName, final String targetDbName, final String targetTableName) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dropTableWithEnvironmentContext(final String dbname, final String name, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getTables(final String dbName, final String pattern) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllTables(final String dbName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table getTableLink(final String dbName, final String targetDbName, final String targetTableName) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Table> getTableObjectsByName(final String dbname, final List<String> tblNames) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getTableNamesByFilter(final String dbname, final String filter, final short maxTables) throws MetaException, InvalidOperationException, UnknownDBException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterTable(final String dbname, final String tblName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterTableWithEnvironmentContext(final String dbname, final String tblName, final Table newTbl, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterTableLink(final String dbName, final String targetDbName, final String targetTableName, final Table newTbl) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterTableLinkProperties(final String dbName, final String targetDbName, final String targetTableName, final Map<String, String> updatedProperties) throws InvalidOperationException, MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition addPartition(final Partition newPart) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition addPartitionWithEnvironmentContext(final Partition newPart, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition addTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws InvalidObjectException, AlreadyExistsException, NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addPartitions(final List<Partition> newParts) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition appendPartition(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition appendPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition appendPartitionByName(final String dbName, final String tblName, final String partName) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition appendPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final EnvironmentContext environmentContext) throws InvalidObjectException, AlreadyExistsException, MetaException,
        TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropPartition(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropPartitionWithEnvironmentContext(final String dbName, final String tblName, final List<String> partVals, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropPartitionByName(final String dbName, final String tblName, final String partName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropPartitionByNameWithEnvironmentContext(final String dbName, final String tblName, final String partName, final boolean deleteData, final EnvironmentContext environmentContext) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropTableLinkPartition(final String dbName, final String targetDbName, final String targetTableName, final String partitionName) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition getPartitionTemplate(final String dbName, final String tblName, final List<String> partVals) throws InvalidObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition getPartition(final String dbName, final String tblName, final List<String> partVals) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> exchangePartition(final Map<String, String> partitionSpecs, final String sourceDb, final String sourceTableName, final String destDb, final String destTableName, final boolean overwrite) throws MetaException,
        NoSuchObjectException, InvalidObjectException,
        InvalidInputException, AlreadyExistsException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition getPartitionWithAuth(final String dbName, final String tblName, final List<String> partVals, final String userName, final List<String> groupNames) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Partition getPartitionByName(final String dbName, final String tblName, final String partName) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitions(final String dbName, final String tblName, final short maxParts) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitionsWithAuth(final String dbName, final String tblName, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getPartitionNames(final String dbName, final String tblName, final short maxParts) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTotalPartitions(final String dbName, final String tblName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitionsPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitionsPsWithAuth(final String dbName, final String tblName, final List<String> partVals, final short maxParts, final String userName, final List<String> groupNames) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getPartitionNamesPs(final String dbName, final String tblName, final List<String> partVals, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitionsByFilter(final String dbName, final String tblName, final String filter, final short maxParts) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Partition> getPartitionsByNames(final String dbName, final String tblName, final List<String> names) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterPartition(final String dbName, final String tblName, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
    }

    @Override
    public void alterPartitions(final String dbName, final String tblName, final List<Partition> newParts) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public void alterPartitionWithEnvironmentContext(final String dbName, final String tblName, final Partition newPart, final EnvironmentContext environmentContext) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renamePartition(final String dbName, final String tblName, final List<String> partVals, final Partition newPart) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean partitionNameHasValidCharacters(final List<String> partVals, final boolean throwException) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String getConfigValue(final String name, final String defaultValue) throws ConfigValSecurityException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> partitionNameToVals(final String partName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> partitionNameToSpec(final String partName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markPartitionForEvent(final String dbName, final String tblName, final Map<String, String> partVals, final PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        UnknownPartitionException,
        InvalidPartitionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartitionMarkedForEvent(final String dbName, final String tblName, final Map<String, String> partVals, final PartitionEventType eventType) throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException,
        UnknownPartitionException,
        InvalidPartitionException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index addIndex(final Index newIndex, final Table indexTable) throws InvalidObjectException, AlreadyExistsException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void alterIndex(final String dbname, final String baseTblName, final String idxName, final Index newIdx) throws InvalidOperationException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropIndexByName(final String dbName, final String tblName, final String indexName, final boolean deleteData) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index getIndexByName(final String dbName, final String tblName, final String indexName) throws MetaException, NoSuchObjectException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Index> getIndexes(final String dbName, final String tblName, final short maxIndexes) throws NoSuchObjectException, MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getIndexNames(final String dbName, final String tblName, final short maxIndexes) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deletePartitionColumnStatistics(final String dbName, final String tblName, final String partName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteTableColumnStatistics(final String dbName, final String tblName, final String colName) throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createRole(final Role role) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropRole(final String roleName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRoleNames() throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grantRole(final String roleName, final String principalName, final PrincipalType principalType, final String grantor, final PrincipalType grantorType, final boolean grantOption) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean revokeRole(final String roleName, final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Role> listRoles(final String principalName, final PrincipalType principalType) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrincipalPrivilegeSet getPrivilegeSet(final HiveObjectRef hiveObject, final String userName, final List<String> groupNames) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HiveObjectPrivilege> listPrivileges(final String principalName, final PrincipalType principalType, final HiveObjectRef hiveObject) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grantPrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean revokePrivileges(final PrivilegeBag privileges) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> setUgi(final String userName, final List<String> groupNames) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String getDelegationToken(final String tokenOwner, final String renewerKerberosPrincipalName) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long renewDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelDelegationToken(final String tokenStrForm) throws MetaException, TException
    {
        throw new UnsupportedOperationException();
    }
}
