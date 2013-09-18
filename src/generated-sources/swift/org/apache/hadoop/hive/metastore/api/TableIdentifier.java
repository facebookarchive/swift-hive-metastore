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

import static com.google.common.base.Objects.toStringHelper;

@ThriftStruct("TableIdentifier")
public class TableIdentifier
{
    @ThriftConstructor
    public TableIdentifier(
                           @ThriftField(value = 1, name = "dbName") final String dbName,
                           @ThriftField(value = 2, name = "tableName") final String tableName,
                           @ThriftField(value = 3, name = "tableType") final String tableType)
    {
        this.dbName = dbName;
        this.tableName = tableName;
        this.tableType = tableType;
    }

    public TableIdentifier()
    {
    }

    private String dbName;

    @ThriftField(value = 1, name = "dbName")
    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(final String dbName)
    {
        this.dbName = dbName;
    }

    private String tableName;

    @ThriftField(value = 2, name = "tableName")
    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(final String tableName)
    {
        this.tableName = tableName;
    }

    private String tableType;

    @ThriftField(value = 3, name = "tableType")
    public String getTableType()
    {
        return tableType;
    }

    public void setTableType(final String tableType)
    {
        this.tableType = tableType;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("dbName", dbName)
            .add("tableName", tableName)
            .add("tableType", tableType)
            .toString();
    }
}
