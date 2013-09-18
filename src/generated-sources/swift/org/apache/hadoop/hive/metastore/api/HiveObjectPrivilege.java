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

@ThriftStruct("HiveObjectPrivilege")
public class HiveObjectPrivilege
{
    @ThriftConstructor
    public HiveObjectPrivilege(
                               @ThriftField(value = 1, name = "hiveObject") final HiveObjectRef hiveObject,
                               @ThriftField(value = 2, name = "principalName") final String principalName,
                               @ThriftField(value = 3, name = "principalType") final PrincipalType principalType,
                               @ThriftField(value = 4, name = "grantInfo") final PrivilegeGrantInfo grantInfo)
    {
        this.hiveObject = hiveObject;
        this.principalName = principalName;
        this.principalType = principalType;
        this.grantInfo = grantInfo;
    }

    public HiveObjectPrivilege()
    {
    }

    private HiveObjectRef hiveObject;

    @ThriftField(value = 1, name = "hiveObject")
    public HiveObjectRef getHiveObject()
    {
        return hiveObject;
    }

    public void setHiveObject(final HiveObjectRef hiveObject)
    {
        this.hiveObject = hiveObject;
    }

    private String principalName;

    @ThriftField(value = 2, name = "principalName")
    public String getPrincipalName()
    {
        return principalName;
    }

    public void setPrincipalName(final String principalName)
    {
        this.principalName = principalName;
    }

    private PrincipalType principalType;

    @ThriftField(value = 3, name = "principalType")
    public PrincipalType getPrincipalType()
    {
        return principalType;
    }

    public void setPrincipalType(final PrincipalType principalType)
    {
        this.principalType = principalType;
    }

    private PrivilegeGrantInfo grantInfo;

    @ThriftField(value = 4, name = "grantInfo")
    public PrivilegeGrantInfo getGrantInfo()
    {
        return grantInfo;
    }

    public void setGrantInfo(final PrivilegeGrantInfo grantInfo)
    {
        this.grantInfo = grantInfo;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("hiveObject", hiveObject)
            .add("principalName", principalName)
            .add("principalType", principalType)
            .add("grantInfo", grantInfo)
            .toString();
    }
}
