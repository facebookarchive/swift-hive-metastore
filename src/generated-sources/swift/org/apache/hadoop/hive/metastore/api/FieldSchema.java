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

@ThriftStruct("FieldSchema")
public class FieldSchema
{
    @ThriftConstructor
    public FieldSchema(
                       @ThriftField(value = 1, name = "name") final String name,
                       @ThriftField(value = 2, name = "type") final String type,
                       @ThriftField(value = 3, name = "comment") final String comment)
    {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public FieldSchema()
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

    private String type;

    @ThriftField(value = 2, name = "type")
    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    private String comment;

    @ThriftField(value = 3, name = "comment")
    public String getComment()
    {
        return comment;
    }

    public void setComment(final String comment)
    {
        this.comment = comment;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("name", name)
            .add("type", type)
            .add("comment", comment)
            .toString();
    }
}
