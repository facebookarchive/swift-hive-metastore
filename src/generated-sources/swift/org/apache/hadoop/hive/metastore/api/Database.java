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

@ThriftStruct("Database")
public class Database
{
    @ThriftConstructor
    public Database(
                    @ThriftField(value = 1, name = "name") final String name,
                    @ThriftField(value = 2, name = "description") final String description,
                    @ThriftField(value = 3, name = "locationUri") final String locationUri,
                    @ThriftField(value = 4, name = "parameters") final Map<String, String> parameters,
                    @ThriftField(value = 5, name = "privileges") final PrincipalPrivilegeSet privileges)
    {
        this.name = name;
        this.description = description;
        this.locationUri = locationUri;
        this.parameters = parameters;
        this.privileges = privileges;
    }

    public Database()
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

    private String description;

    @ThriftField(value = 2, name = "description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    private String locationUri;

    @ThriftField(value = 3, name = "locationUri")
    public String getLocationUri()
    {
        return locationUri;
    }

    public void setLocationUri(final String locationUri)
    {
        this.locationUri = locationUri;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 4, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    private PrincipalPrivilegeSet privileges;

    @ThriftField(value = 5, name = "privileges")
    public PrincipalPrivilegeSet getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(final PrincipalPrivilegeSet privileges)
    {
        this.privileges = privileges;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("name", name)
            .add("description", description)
            .add("locationUri", locationUri)
            .add("parameters", parameters)
            .add("privileges", privileges)
            .toString();
    }
}
