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

@ThriftStruct("DoubleColumnStatsData")
public class DoubleColumnStatsData
{
    @ThriftConstructor
    public DoubleColumnStatsData(
                                 @ThriftField(value = 1, name = "lowValue") final double lowValue,
                                 @ThriftField(value = 2, name = "highValue") final double highValue,
                                 @ThriftField(value = 3, name = "numNulls") final long numNulls,
                                 @ThriftField(value = 4, name = "numDVs") final long numDVs)
    {
        this.lowValue = lowValue;
        this.highValue = highValue;
        this.numNulls = numNulls;
        this.numDVs = numDVs;
    }

    public DoubleColumnStatsData()
    {
    }

    private double lowValue;

    @ThriftField(value = 1, name = "lowValue")
    public double getLowValue()
    {
        return lowValue;
    }

    public void setLowValue(final double lowValue)
    {
        this.lowValue = lowValue;
    }

    private double highValue;

    @ThriftField(value = 2, name = "highValue")
    public double getHighValue()
    {
        return highValue;
    }

    public void setHighValue(final double highValue)
    {
        this.highValue = highValue;
    }

    private long numNulls;

    @ThriftField(value = 3, name = "numNulls")
    public long getNumNulls()
    {
        return numNulls;
    }

    public void setNumNulls(final long numNulls)
    {
        this.numNulls = numNulls;
    }

    private long numDVs;

    @ThriftField(value = 4, name = "numDVs")
    public long getNumDVs()
    {
        return numDVs;
    }

    public void setNumDVs(final long numDVs)
    {
        this.numDVs = numDVs;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("lowValue", lowValue)
            .add("highValue", highValue)
            .add("numNulls", numNulls)
            .add("numDVs", numDVs)
            .toString();
    }
}
