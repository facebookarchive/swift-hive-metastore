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

import static com.google.common.base.Objects.toStringHelper;

@ThriftStruct("ColumnStatistics")
public class ColumnStatistics
{
    @ThriftConstructor
    public ColumnStatistics(
                            @ThriftField(value = 1, name = "statsDesc") final ColumnStatisticsDesc statsDesc,
                            @ThriftField(value = 2, name = "statsObj") final List<ColumnStatisticsObj> statsObj)
    {
        this.statsDesc = statsDesc;
        this.statsObj = statsObj;
    }

    public ColumnStatistics()
    {
    }

    private ColumnStatisticsDesc statsDesc;

    @ThriftField(value = 1, name = "statsDesc")
    public ColumnStatisticsDesc getStatsDesc()
    {
        return statsDesc;
    }

    public void setStatsDesc(final ColumnStatisticsDesc statsDesc)
    {
        this.statsDesc = statsDesc;
    }

    private List<ColumnStatisticsObj> statsObj;

    @ThriftField(value = 2, name = "statsObj")
    public List<ColumnStatisticsObj> getStatsObj()
    {
        return statsObj;
    }

    public void setStatsObj(final List<ColumnStatisticsObj> statsObj)
    {
        this.statsObj = statsObj;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("statsDesc", statsDesc)
            .add("statsObj", statsObj)
            .toString();
    }
}
