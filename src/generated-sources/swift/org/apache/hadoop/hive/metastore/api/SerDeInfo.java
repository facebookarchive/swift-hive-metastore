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

@ThriftStruct("SerDeInfo")
public class SerDeInfo
{
    @ThriftConstructor
    public SerDeInfo(
                     @ThriftField(value = 1, name = "name") final String name,
                     @ThriftField(value = 2, name = "serializationLib") final String serializationLib,
                     @ThriftField(value = 3, name = "parameters") final Map<String, String> parameters)
    {
        this.name = name;
        this.serializationLib = serializationLib;
        this.parameters = parameters;
    }

    public SerDeInfo()
    {
    }

    private String name;

    @ThriftField(value = 1, name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    private String serializationLib;

    @ThriftField(value = 2, name = "serializationLib")
    public String getSerializationLib()
    {
        return serializationLib;
    }

    public void setSerializationLib(final String serializationLib)
    {
        this.serializationLib = serializationLib;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 3, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("name", name)
            .add("serializationLib", serializationLib)
            .add("parameters", parameters)
            .toString();
    }
}
