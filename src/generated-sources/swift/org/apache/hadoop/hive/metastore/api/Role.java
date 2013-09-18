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

@ThriftStruct("Role")
public class Role
{
    @ThriftConstructor
    public Role(
                @ThriftField(value = 1, name = "roleName") final String roleName,
                @ThriftField(value = 2, name = "createTime") final int createTime,
                @ThriftField(value = 3, name = "ownerName") final String ownerName)
    {
        this.roleName = roleName;
        this.createTime = createTime;
        this.ownerName = ownerName;
    }

    public Role()
    {
    }

    private String roleName;

    @ThriftField(value = 1, name = "roleName")
    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(final String roleName)
    {
        this.roleName = roleName;
    }

    private int createTime;

    @ThriftField(value = 2, name = "createTime")
    public int getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(final int createTime)
    {
        this.createTime = createTime;
    }

    private String ownerName;

    @ThriftField(value = 3, name = "ownerName")
    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(final String ownerName)
    {
        this.ownerName = ownerName;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("roleName", roleName)
            .add("createTime", createTime)
            .add("ownerName", ownerName)
            .toString();
    }
}
