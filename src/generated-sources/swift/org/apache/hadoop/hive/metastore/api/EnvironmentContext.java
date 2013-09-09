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

import static com.google.common.base.Objects.toStringHelper;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.Map;

@ThriftStruct("EnvironmentContext")
public class EnvironmentContext
{
    @ThriftConstructor
    public EnvironmentContext(
                              @ThriftField(value = 1, name = "properties") final Map<String, String> properties)
    {
        this.properties = properties;
    }

    public EnvironmentContext()
    {
    }

    private Map<String, String> properties;

    @ThriftField(value = 1, name = "properties")
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(final Map<String, String> properties)
    {
        this.properties = properties;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("properties", properties)
            .toString();
    }
}
