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
package org.apache.hadoop.hive.metastore.api;

import static com.google.common.base.Objects.toStringHelper;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;
import java.util.Map;

@ThriftStruct("Partition")
public class Partition
{
    @ThriftConstructor
    public Partition(
                     @ThriftField(value = 1, name = "values") final List<String> values,
                     @ThriftField(value = 2, name = "dbName") final String dbName,
                     @ThriftField(value = 3, name = "tableName") final String tableName,
                     @ThriftField(value = 4, name = "createTime") final int createTime,
                     @ThriftField(value = 5, name = "lastAccessTime") final int lastAccessTime,
                     @ThriftField(value = 6, name = "sd") final StorageDescriptor sd,
                     @ThriftField(value = 7, name = "parameters") final Map<String, String> parameters,
                     @ThriftField(value = 8, name = "privileges") final PrincipalPrivilegeSet privileges,
                     @ThriftField(value = 9, name = "linkTarget") final PartitionIdentifier linkTarget,
                     @ThriftField(value = 10, name = "linkPartitions") final List<PartitionIdentifier> linkPartitions)
    {
        this.values = values;
        this.dbName = dbName;
        this.tableName = tableName;
        this.createTime = createTime;
        this.lastAccessTime = lastAccessTime;
        this.sd = sd;
        this.parameters = parameters;
        this.privileges = privileges;
        this.linkTarget = linkTarget;
        this.linkPartitions = linkPartitions;
    }

    public Partition()
    {
    }

    private List<String> values;

    @ThriftField(value = 1, name = "values")
    public List<String> getValues()
    {
        return values;
    }

    public void setValues(final List<String> values)
    {
        this.values = values;
    }

    private String dbName;

    @ThriftField(value = 2, name = "dbName")
    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(final String dbName)
    {
        this.dbName = dbName;
    }

    private String tableName;

    @ThriftField(value = 3, name = "tableName")
    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(final String tableName)
    {
        this.tableName = tableName;
    }

    private int createTime;

    @ThriftField(value = 4, name = "createTime")
    public int getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(final int createTime)
    {
        this.createTime = createTime;
    }

    private int lastAccessTime;

    @ThriftField(value = 5, name = "lastAccessTime")
    public int getLastAccessTime()
    {
        return lastAccessTime;
    }

    public void setLastAccessTime(final int lastAccessTime)
    {
        this.lastAccessTime = lastAccessTime;
    }

    private StorageDescriptor sd;

    @ThriftField(value = 6, name = "sd")
    public StorageDescriptor getSd()
    {
        return sd;
    }

    public void setSd(final StorageDescriptor sd)
    {
        this.sd = sd;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 7, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    private PrincipalPrivilegeSet privileges;

    @ThriftField(value = 8, name = "privileges")
    public PrincipalPrivilegeSet getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(final PrincipalPrivilegeSet privileges)
    {
        this.privileges = privileges;
    }

    private PartitionIdentifier linkTarget;

    @ThriftField(value = 9, name = "linkTarget")
    public PartitionIdentifier getLinkTarget()
    {
        return linkTarget;
    }

    public void setLinkTarget(final PartitionIdentifier linkTarget)
    {
        this.linkTarget = linkTarget;
    }

    private List<PartitionIdentifier> linkPartitions;

    @ThriftField(value = 10, name = "linkPartitions")
    public List<PartitionIdentifier> getLinkPartitions()
    {
        return linkPartitions;
    }

    public void setLinkPartitions(final List<PartitionIdentifier> linkPartitions)
    {
        this.linkPartitions = linkPartitions;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("values", values)
            .add("dbName", dbName)
            .add("tableName", tableName)
            .add("createTime", createTime)
            .add("lastAccessTime", lastAccessTime)
            .add("sd", sd)
            .add("parameters", parameters)
            .add("privileges", privileges)
            .add("linkTarget", linkTarget)
            .add("linkPartitions", linkPartitions)
            .toString();
    }
}
