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

@ThriftStruct("ColumnStatisticsObj")
public class ColumnStatisticsObj
{
    @ThriftConstructor
    public ColumnStatisticsObj(
                               @ThriftField(value = 1, name = "colName") final String colName,
                               @ThriftField(value = 2, name = "colType") final String colType,
                               @ThriftField(value = 3, name = "statsData") final ColumnStatisticsData statsData)
    {
        this.colName = colName;
        this.colType = colType;
        this.statsData = statsData;
    }

    public ColumnStatisticsObj()
    {
    }

    private String colName;

    @ThriftField(value = 1, name = "colName")
    public String getColName()
    {
        return colName;
    }

    public void setColName(final String colName)
    {
        this.colName = colName;
    }

    private String colType;

    @ThriftField(value = 2, name = "colType")
    public String getColType()
    {
        return colType;
    }

    public void setColType(final String colType)
    {
        this.colType = colType;
    }

    private ColumnStatisticsData statsData;

    @ThriftField(value = 3, name = "statsData")
    public ColumnStatisticsData getStatsData()
    {
        return statsData;
    }

    public void setStatsData(final ColumnStatisticsData statsData)
    {
        this.statsData = statsData;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("colName", colName)
            .add("colType", colType)
            .add("statsData", statsData)
            .toString();
    }
}
