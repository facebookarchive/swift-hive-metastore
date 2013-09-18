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

@ThriftStruct("ColumnStatisticsDesc")
public class ColumnStatisticsDesc
{
    @ThriftConstructor
    public ColumnStatisticsDesc(
                                @ThriftField(value = 1, name = "isTblLevel") final boolean isTblLevel,
                                @ThriftField(value = 2, name = "dbName") final String dbName,
                                @ThriftField(value = 3, name = "tableName") final String tableName,
                                @ThriftField(value = 4, name = "partName") final String partName,
                                @ThriftField(value = 5, name = "lastAnalyzed") final long lastAnalyzed)
    {
        this.isTblLevel = isTblLevel;
        this.dbName = dbName;
        this.tableName = tableName;
        this.partName = partName;
        this.lastAnalyzed = lastAnalyzed;
    }

    public ColumnStatisticsDesc()
    {
    }

    private boolean isTblLevel;

    @ThriftField(value = 1, name = "isTblLevel")
    public boolean isIsTblLevel()
    {
        return isTblLevel;
    }

    public void setIsTblLevel(final boolean isTblLevel)
    {
        this.isTblLevel = isTblLevel;
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

    private String partName;

    @ThriftField(value = 4, name = "partName")
    public String getPartName()
    {
        return partName;
    }

    public void setPartName(final String partName)
    {
        this.partName = partName;
    }

    private long lastAnalyzed;

    @ThriftField(value = 5, name = "lastAnalyzed")
    public long getLastAnalyzed()
    {
        return lastAnalyzed;
    }

    public void setLastAnalyzed(final long lastAnalyzed)
    {
        this.lastAnalyzed = lastAnalyzed;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("isTblLevel", isTblLevel)
            .add("dbName", dbName)
            .add("tableName", tableName)
            .add("partName", partName)
            .add("lastAnalyzed", lastAnalyzed)
            .toString();
    }
}
