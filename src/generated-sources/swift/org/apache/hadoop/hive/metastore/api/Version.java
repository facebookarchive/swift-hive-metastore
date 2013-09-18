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

@ThriftStruct("Version")
public class Version
{
    @ThriftConstructor
    public Version(
                   @ThriftField(value = 1, name = "version") final String version,
                   @ThriftField(value = 2, name = "comments") final String comments)
    {
        this.version = version;
        this.comments = comments;
    }

    public Version()
    {
    }

    private String version;

    @ThriftField(value = 1, name = "version")
    public String getVersion()
    {
        return version;
    }

    public void setVersion(final String version)
    {
        this.version = version;
    }

    private String comments;

    @ThriftField(value = 2, name = "comments")
    public String getComments()
    {
        return comments;
    }

    public void setComments(final String comments)
    {
        this.comments = comments;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("version", version)
            .add("comments", comments)
            .toString();
    }
}
