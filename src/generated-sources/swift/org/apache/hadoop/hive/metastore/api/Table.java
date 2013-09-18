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

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.toStringHelper;

@ThriftStruct("Table")
public class Table
{
    @ThriftConstructor
    public Table(
                 @ThriftField(value = 1, name = "tableName") final String tableName,
                 @ThriftField(value = 2, name = "dbName") final String dbName,
                 @ThriftField(value = 3, name = "owner") final String owner,
                 @ThriftField(value = 4, name = "createTime") final int createTime,
                 @ThriftField(value = 5, name = "lastAccessTime") final int lastAccessTime,
                 @ThriftField(value = 6, name = "retention") final int retention,
                 @ThriftField(value = 7, name = "sd") final StorageDescriptor sd,
                 @ThriftField(value = 8, name = "partitionKeys") final List<FieldSchema> partitionKeys,
                 @ThriftField(value = 9, name = "parameters") final Map<String, String> parameters,
                 @ThriftField(value = 10, name = "viewOriginalText") final String viewOriginalText,
                 @ThriftField(value = 11, name = "viewExpandedText") final String viewExpandedText,
                 @ThriftField(value = 12, name = "tableType") final String tableType,
                 @ThriftField(value = 13, name = "privileges") final PrincipalPrivilegeSet privileges,
                 @ThriftField(value = 14, name = "linkTarget") final TableIdentifier linkTarget,
                 @ThriftField(value = 15, name = "linkTables") final List<TableIdentifier> linkTables)
    {
        this.tableName = tableName;
        this.dbName = dbName;
        this.owner = owner;
        this.createTime = createTime;
        this.lastAccessTime = lastAccessTime;
        this.retention = retention;
        this.sd = sd;
        this.partitionKeys = partitionKeys;
        this.parameters = parameters;
        this.viewOriginalText = viewOriginalText;
        this.viewExpandedText = viewExpandedText;
        this.tableType = tableType;
        this.privileges = privileges;
        this.linkTarget = linkTarget;
        this.linkTables = linkTables;
    }

    public Table()
    {
    }

    private String tableName;

    @ThriftField(value = 1, name = "tableName")
    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(final String tableName)
    {
        this.tableName = tableName;
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

    private String owner;

    @ThriftField(value = 3, name = "owner")
    public String getOwner()
    {
        return owner;
    }

    public void setOwner(final String owner)
    {
        this.owner = owner;
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

    private int retention;

    @ThriftField(value = 6, name = "retention")
    public int getRetention()
    {
        return retention;
    }

    public void setRetention(final int retention)
    {
        this.retention = retention;
    }

    private StorageDescriptor sd;

    @ThriftField(value = 7, name = "sd")
    public StorageDescriptor getSd()
    {
        return sd;
    }

    public void setSd(final StorageDescriptor sd)
    {
        this.sd = sd;
    }

    private List<FieldSchema> partitionKeys;

    @ThriftField(value = 8, name = "partitionKeys")
    public List<FieldSchema> getPartitionKeys()
    {
        return partitionKeys;
    }

    public void setPartitionKeys(final List<FieldSchema> partitionKeys)
    {
        this.partitionKeys = partitionKeys;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 9, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    private String viewOriginalText;

    @ThriftField(value = 10, name = "viewOriginalText")
    public String getViewOriginalText()
    {
        return viewOriginalText;
    }

    public void setViewOriginalText(final String viewOriginalText)
    {
        this.viewOriginalText = viewOriginalText;
    }

    private String viewExpandedText;

    @ThriftField(value = 11, name = "viewExpandedText")
    public String getViewExpandedText()
    {
        return viewExpandedText;
    }

    public void setViewExpandedText(final String viewExpandedText)
    {
        this.viewExpandedText = viewExpandedText;
    }

    private String tableType;

    @ThriftField(value = 12, name = "tableType")
    public String getTableType()
    {
        return tableType;
    }

    public void setTableType(final String tableType)
    {
        this.tableType = tableType;
    }

    private PrincipalPrivilegeSet privileges;

    @ThriftField(value = 13, name = "privileges")
    public PrincipalPrivilegeSet getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(final PrincipalPrivilegeSet privileges)
    {
        this.privileges = privileges;
    }

    private TableIdentifier linkTarget;

    @ThriftField(value = 14, name = "linkTarget")
    public TableIdentifier getLinkTarget()
    {
        return linkTarget;
    }

    public void setLinkTarget(final TableIdentifier linkTarget)
    {
        this.linkTarget = linkTarget;
    }

    private List<TableIdentifier> linkTables;

    @ThriftField(value = 15, name = "linkTables")
    public List<TableIdentifier> getLinkTables()
    {
        return linkTables;
    }

    public void setLinkTables(final List<TableIdentifier> linkTables)
    {
        this.linkTables = linkTables;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("tableName", tableName)
            .add("dbName", dbName)
            .add("owner", owner)
            .add("createTime", createTime)
            .add("lastAccessTime", lastAccessTime)
            .add("retention", retention)
            .add("sd", sd)
            .add("partitionKeys", partitionKeys)
            .add("parameters", parameters)
            .add("viewOriginalText", viewOriginalText)
            .add("viewExpandedText", viewExpandedText)
            .add("tableType", tableType)
            .add("privileges", privileges)
            .add("linkTarget", linkTarget)
            .add("linkTables", linkTables)
            .toString();
    }
}
