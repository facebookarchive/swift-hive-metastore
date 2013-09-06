/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.
 *
 * This file is licensed to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.metastore.api;

import static com.google.common.base.Objects.toStringHelper;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;

@ThriftStruct("HiveObjectRef")
public class HiveObjectRef
{
    @ThriftConstructor
    public HiveObjectRef(
                         @ThriftField(value = 1, name = "objectType") final HiveObjectType objectType,
                         @ThriftField(value = 2, name = "dbName") final String dbName,
                         @ThriftField(value = 3, name = "objectName") final String objectName,
                         @ThriftField(value = 4, name = "partValues") final List<String> partValues,
                         @ThriftField(value = 5, name = "columnName") final String columnName)
    {
        this.objectType = objectType;
        this.dbName = dbName;
        this.objectName = objectName;
        this.partValues = partValues;
        this.columnName = columnName;
    }

    public HiveObjectRef()
    {
    }

    private HiveObjectType objectType;

    @ThriftField(value = 1, name = "objectType")
    public HiveObjectType getObjectType()
    {
        return objectType;
    }

    public void setObjectType(final HiveObjectType objectType)
    {
        this.objectType = objectType;
    }

    private String dbName;

    @ThriftField(value = 2, name = "dbName")
    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(final String dbName)
    {
        this.dbName = dbName;
    }

    private String objectName;

    @ThriftField(value = 3, name = "objectName")
    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(final String objectName)
    {
        this.objectName = objectName;
    }

    private List<String> partValues;

    @ThriftField(value = 4, name = "partValues")
    public List<String> getPartValues()
    {
        return partValues;
    }

    public void setPartValues(final List<String> partValues)
    {
        this.partValues = partValues;
    }

    private String columnName;

    @ThriftField(value = 5, name = "columnName")
    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(final String columnName)
    {
        this.columnName = columnName;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("objectType", objectType)
            .add("dbName", dbName)
            .add("objectName", objectName)
            .add("partValues", partValues)
            .add("columnName", columnName)
            .toString();
    }
}
