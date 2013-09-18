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

@ThriftStruct("BooleanColumnStatsData")
public class BooleanColumnStatsData
{
    @ThriftConstructor
    public BooleanColumnStatsData(
                                  @ThriftField(value = 1, name = "numTrues") final long numTrues,
                                  @ThriftField(value = 2, name = "numFalses") final long numFalses,
                                  @ThriftField(value = 3, name = "numNulls") final long numNulls)
    {
        this.numTrues = numTrues;
        this.numFalses = numFalses;
        this.numNulls = numNulls;
    }

    public BooleanColumnStatsData()
    {
    }

    private long numTrues;

    @ThriftField(value = 1, name = "numTrues")
    public long getNumTrues()
    {
        return numTrues;
    }

    public void setNumTrues(final long numTrues)
    {
        this.numTrues = numTrues;
    }

    private long numFalses;

    @ThriftField(value = 2, name = "numFalses")
    public long getNumFalses()
    {
        return numFalses;
    }

    public void setNumFalses(final long numFalses)
    {
        this.numFalses = numFalses;
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

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("numTrues", numTrues)
            .add("numFalses", numFalses)
            .add("numNulls", numNulls)
            .toString();
    }
}
