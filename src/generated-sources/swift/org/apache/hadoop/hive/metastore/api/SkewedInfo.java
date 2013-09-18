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

@ThriftStruct("SkewedInfo")
public class SkewedInfo
{
    @ThriftConstructor
    public SkewedInfo(
                      @ThriftField(value = 1, name = "skewedColNames") final List<String> skewedColNames,
                      @ThriftField(value = 2, name = "skewedColValues") final List<List<String>> skewedColValues,
                      @ThriftField(value = 3, name = "skewedColValueLocationMaps") final Map<List<String>, String> skewedColValueLocationMaps)
    {
        this.skewedColNames = skewedColNames;
        this.skewedColValues = skewedColValues;
        this.skewedColValueLocationMaps = skewedColValueLocationMaps;
    }

    public SkewedInfo()
    {
    }

    private List<String> skewedColNames;

    @ThriftField(value = 1, name = "skewedColNames")
    public List<String> getSkewedColNames()
    {
        return skewedColNames;
    }

    public void setSkewedColNames(final List<String> skewedColNames)
    {
        this.skewedColNames = skewedColNames;
    }

    private List<List<String>> skewedColValues;

    @ThriftField(value = 2, name = "skewedColValues")
    public List<List<String>> getSkewedColValues()
    {
        return skewedColValues;
    }

    public void setSkewedColValues(final List<List<String>> skewedColValues)
    {
        this.skewedColValues = skewedColValues;
    }

    private Map<List<String>, String> skewedColValueLocationMaps;

    @ThriftField(value = 3, name = "skewedColValueLocationMaps")
    public Map<List<String>, String> getSkewedColValueLocationMaps()
    {
        return skewedColValueLocationMaps;
    }

    public void setSkewedColValueLocationMaps(final Map<List<String>, String> skewedColValueLocationMaps)
    {
        this.skewedColValueLocationMaps = skewedColValueLocationMaps;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("skewedColNames", skewedColNames)
            .add("skewedColValues", skewedColValues)
            .add("skewedColValueLocationMaps", skewedColValueLocationMaps)
            .toString();
    }
}
