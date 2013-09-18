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

@ThriftStruct("PrivilegeGrantInfo")
public class PrivilegeGrantInfo
{
    @ThriftConstructor
    public PrivilegeGrantInfo(
                              @ThriftField(value = 1, name = "privilege") final String privilege,
                              @ThriftField(value = 2, name = "createTime") final int createTime,
                              @ThriftField(value = 3, name = "grantor") final String grantor,
                              @ThriftField(value = 4, name = "grantorType") final PrincipalType grantorType,
                              @ThriftField(value = 5, name = "grantOption") final boolean grantOption)
    {
        this.privilege = privilege;
        this.createTime = createTime;
        this.grantor = grantor;
        this.grantorType = grantorType;
        this.grantOption = grantOption;
    }

    public PrivilegeGrantInfo()
    {
    }

    private String privilege;

    @ThriftField(value = 1, name = "privilege")
    public String getPrivilege()
    {
        return privilege;
    }

    public void setPrivilege(final String privilege)
    {
        this.privilege = privilege;
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

    private String grantor;

    @ThriftField(value = 3, name = "grantor")
    public String getGrantor()
    {
        return grantor;
    }

    public void setGrantor(final String grantor)
    {
        this.grantor = grantor;
    }

    private PrincipalType grantorType;

    @ThriftField(value = 4, name = "grantorType")
    public PrincipalType getGrantorType()
    {
        return grantorType;
    }

    public void setGrantorType(final PrincipalType grantorType)
    {
        this.grantorType = grantorType;
    }

    private boolean grantOption;

    @ThriftField(value = 5, name = "grantOption")
    public boolean isGrantOption()
    {
        return grantOption;
    }

    public void setGrantOption(final boolean grantOption)
    {
        this.grantOption = grantOption;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("privilege", privilege)
            .add("createTime", createTime)
            .add("grantor", grantor)
            .add("grantorType", grantorType)
            .add("grantOption", grantOption)
            .toString();
    }
}
