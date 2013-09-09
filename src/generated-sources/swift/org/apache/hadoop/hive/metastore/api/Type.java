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

import java.util.List;

@ThriftStruct("Type")
public class Type
{
    @ThriftConstructor
    public Type(
                @ThriftField(value = 1, name = "name") final String name,
                @ThriftField(value = 2, name = "type1") final String type1,
                @ThriftField(value = 3, name = "type2") final String type2,
                @ThriftField(value = 4, name = "fields") final List<FieldSchema> fields)
    {
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.fields = fields;
    }

    public Type()
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

    private String type1;

    @ThriftField(value = 2, name = "type1")
    public String getType1()
    {
        return type1;
    }

    public void setType1(final String type1)
    {
        this.type1 = type1;
    }

    private String type2;

    @ThriftField(value = 3, name = "type2")
    public String getType2()
    {
        return type2;
    }

    public void setType2(final String type2)
    {
        this.type2 = type2;
    }

    private List<FieldSchema> fields;

    @ThriftField(value = 4, name = "fields")
    public List<FieldSchema> getFields()
    {
        return fields;
    }

    public void setFields(final List<FieldSchema> fields)
    {
        this.fields = fields;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("name", name)
            .add("type1", type1)
            .add("type2", type2)
            .add("fields", fields)
            .toString();
    }
}
