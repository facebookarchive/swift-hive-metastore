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

@ThriftStruct("PrivilegeBag")
public class PrivilegeBag
{
    @ThriftConstructor
    public PrivilegeBag(
                        @ThriftField(value = 1, name = "privileges") final List<HiveObjectPrivilege> privileges)
    {
        this.privileges = privileges;
    }

    public PrivilegeBag()
    {
    }

    private List<HiveObjectPrivilege> privileges;

    @ThriftField(value = 1, name = "privileges")
    public List<HiveObjectPrivilege> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(final List<HiveObjectPrivilege> privileges)
    {
        this.privileges = privileges;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("privileges", privileges)
            .toString();
    }
}
