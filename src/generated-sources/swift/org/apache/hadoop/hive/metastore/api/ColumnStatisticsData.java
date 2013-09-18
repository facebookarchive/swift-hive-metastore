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
import com.facebook.swift.codec.ThriftUnion;
import com.facebook.swift.codec.ThriftUnionId;

import static com.google.common.base.Objects.toStringHelper;

@ThriftUnion("ColumnStatisticsData")
public class ColumnStatisticsData
{
    private Object value;
    private int id = -1;
    private String name;

    public ColumnStatisticsData()
    {
    }

    @ThriftConstructor
    public ColumnStatisticsData(final BooleanColumnStatsData booleanStats)
    {
        this.value = booleanStats;
        this.id = 1;
        this.name = "booleanStats";
    }

    @ThriftConstructor
    public ColumnStatisticsData(final LongColumnStatsData longStats)
    {
        this.value = longStats;
        this.id = 2;
        this.name = "longStats";
    }

    @ThriftConstructor
    public ColumnStatisticsData(final DoubleColumnStatsData doubleStats)
    {
        this.value = doubleStats;
        this.id = 3;
        this.name = "doubleStats";
    }

    @ThriftConstructor
    public ColumnStatisticsData(final StringColumnStatsData stringStats)
    {
        this.value = stringStats;
        this.id = 4;
        this.name = "stringStats";
    }

    @ThriftConstructor
    public ColumnStatisticsData(final BinaryColumnStatsData binaryStats)
    {
        this.value = binaryStats;
        this.id = 5;
        this.name = "binaryStats";
    }

    public void setBooleanStats(final BooleanColumnStatsData booleanStats)
    {
        this.value = booleanStats;
        this.id = 1;
        this.name = "booleanStats";
    }

    public void setLongStats(final LongColumnStatsData longStats)
    {
        this.value = longStats;
        this.id = 2;
        this.name = "longStats";
    }

    public void setDoubleStats(final DoubleColumnStatsData doubleStats)
    {
        this.value = doubleStats;
        this.id = 3;
        this.name = "doubleStats";
    }

    public void setStringStats(final StringColumnStatsData stringStats)
    {
        this.value = stringStats;
        this.id = 4;
        this.name = "stringStats";
    }

    public void setBinaryStats(final BinaryColumnStatsData binaryStats)
    {
        this.value = binaryStats;
        this.id = 5;
        this.name = "binaryStats";
    }

    @ThriftField(value = 1, name = "booleanStats")
    public BooleanColumnStatsData getBooleanStats()
    {
        if (this.id != 1) {
            throw new IllegalStateException("Not a booleanStats element!");
        }
        return (BooleanColumnStatsData) value;
    }

    public boolean isSetBooleanStats()
    {
        return this.id == 1;
    }

    @ThriftField(value = 2, name = "longStats")
    public LongColumnStatsData getLongStats()
    {
        if (this.id != 2) {
            throw new IllegalStateException("Not a longStats element!");
        }
        return (LongColumnStatsData) value;
    }

    public boolean isSetLongStats()
    {
        return this.id == 2;
    }

    @ThriftField(value = 3, name = "doubleStats")
    public DoubleColumnStatsData getDoubleStats()
    {
        if (this.id != 3) {
            throw new IllegalStateException("Not a doubleStats element!");
        }
        return (DoubleColumnStatsData) value;
    }

    public boolean isSetDoubleStats()
    {
        return this.id == 3;
    }

    @ThriftField(value = 4, name = "stringStats")
    public StringColumnStatsData getStringStats()
    {
        if (this.id != 4) {
            throw new IllegalStateException("Not a stringStats element!");
        }
        return (StringColumnStatsData) value;
    }

    public boolean isSetStringStats()
    {
        return this.id == 4;
    }

    @ThriftField(value = 5, name = "binaryStats")
    public BinaryColumnStatsData getBinaryStats()
    {
        if (this.id != 5) {
            throw new IllegalStateException("Not a binaryStats element!");
        }
        return (BinaryColumnStatsData) value;
    }

    public boolean isSetBinaryStats()
    {
        return this.id == 5;
    }

    @ThriftUnionId
    public int getThriftId()
    {
        return this.id;
    }

    public String getThriftName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("value", value)
            .add("id", id)
            .add("name", name)
            .add("type", value == null ? "<null>" : value.getClass().getSimpleName())
            .toString();
    }
}
