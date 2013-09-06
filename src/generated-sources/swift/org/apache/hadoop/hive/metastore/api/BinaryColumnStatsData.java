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

@ThriftStruct("BinaryColumnStatsData")
public class BinaryColumnStatsData
{
    @ThriftConstructor
    public BinaryColumnStatsData(
                                 @ThriftField(value = 1, name = "maxColLen") final long maxColLen,
                                 @ThriftField(value = 2, name = "avgColLen") final double avgColLen,
                                 @ThriftField(value = 3, name = "numNulls") final long numNulls)
    {
        this.maxColLen = maxColLen;
        this.avgColLen = avgColLen;
        this.numNulls = numNulls;
    }

    public BinaryColumnStatsData()
    {
    }

    private long maxColLen;

    @ThriftField(value = 1, name = "maxColLen")
    public long getMaxColLen()
    {
        return maxColLen;
    }

    public void setMaxColLen(final long maxColLen)
    {
        this.maxColLen = maxColLen;
    }

    private double avgColLen;

    @ThriftField(value = 2, name = "avgColLen")
    public double getAvgColLen()
    {
        return avgColLen;
    }

    public void setAvgColLen(final double avgColLen)
    {
        this.avgColLen = avgColLen;
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
            .add("maxColLen", maxColLen)
            .add("avgColLen", avgColLen)
            .add("numNulls", numNulls)
            .toString();
    }
}
