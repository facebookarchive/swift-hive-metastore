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

@ThriftStruct("PrincipalPrivilegeSet")
public class PrincipalPrivilegeSet
{
    @ThriftConstructor
    public PrincipalPrivilegeSet(
                                 @ThriftField(value = 1, name = "userPrivileges") final Map<String, List<PrivilegeGrantInfo>> userPrivileges,
                                 @ThriftField(value = 2, name = "groupPrivileges") final Map<String, List<PrivilegeGrantInfo>> groupPrivileges,
                                 @ThriftField(value = 3, name = "rolePrivileges") final Map<String, List<PrivilegeGrantInfo>> rolePrivileges)
    {
        this.userPrivileges = userPrivileges;
        this.groupPrivileges = groupPrivileges;
        this.rolePrivileges = rolePrivileges;
    }

    public PrincipalPrivilegeSet()
    {
    }

    private Map<String, List<PrivilegeGrantInfo>> userPrivileges;

    @ThriftField(value = 1, name = "userPrivileges")
    public Map<String, List<PrivilegeGrantInfo>> getUserPrivileges()
    {
        return userPrivileges;
    }

    public void setUserPrivileges(final Map<String, List<PrivilegeGrantInfo>> userPrivileges)
    {
        this.userPrivileges = userPrivileges;
    }

    private Map<String, List<PrivilegeGrantInfo>> groupPrivileges;

    @ThriftField(value = 2, name = "groupPrivileges")
    public Map<String, List<PrivilegeGrantInfo>> getGroupPrivileges()
    {
        return groupPrivileges;
    }

    public void setGroupPrivileges(final Map<String, List<PrivilegeGrantInfo>> groupPrivileges)
    {
        this.groupPrivileges = groupPrivileges;
    }

    private Map<String, List<PrivilegeGrantInfo>> rolePrivileges;

    @ThriftField(value = 3, name = "rolePrivileges")
    public Map<String, List<PrivilegeGrantInfo>> getRolePrivileges()
    {
        return rolePrivileges;
    }

    public void setRolePrivileges(final Map<String, List<PrivilegeGrantInfo>> rolePrivileges)
    {
        this.rolePrivileges = rolePrivileges;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("userPrivileges", userPrivileges)
            .add("groupPrivileges", groupPrivileges)
            .add("rolePrivileges", rolePrivileges)
            .toString();
    }
}
