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

@ThriftStruct("Index")
public class Index
{
    @ThriftConstructor
    public Index(
                 @ThriftField(value = 1, name = "indexName") final String indexName,
                 @ThriftField(value = 2, name = "indexHandlerClass") final String indexHandlerClass,
                 @ThriftField(value = 3, name = "dbName") final String dbName,
                 @ThriftField(value = 4, name = "origTableName") final String origTableName,
                 @ThriftField(value = 5, name = "createTime") final int createTime,
                 @ThriftField(value = 6, name = "lastAccessTime") final int lastAccessTime,
                 @ThriftField(value = 7, name = "indexTableName") final String indexTableName,
                 @ThriftField(value = 8, name = "sd") final StorageDescriptor sd,
                 @ThriftField(value = 9, name = "parameters") final Map<String, String> parameters,
                 @ThriftField(value = 10, name = "deferredRebuild") final boolean deferredRebuild)
    {
        this.indexName = indexName;
        this.indexHandlerClass = indexHandlerClass;
        this.dbName = dbName;
        this.origTableName = origTableName;
        this.createTime = createTime;
        this.lastAccessTime = lastAccessTime;
        this.indexTableName = indexTableName;
        this.sd = sd;
        this.parameters = parameters;
        this.deferredRebuild = deferredRebuild;
    }

    public Index()
    {
    }

    private String indexName;

    @ThriftField(value = 1, name = "indexName")
    public String getIndexName()
    {
        return indexName;
    }

    public void setIndexName(final String indexName)
    {
        this.indexName = indexName;
    }

    private String indexHandlerClass;

    @ThriftField(value = 2, name = "indexHandlerClass")
    public String getIndexHandlerClass()
    {
        return indexHandlerClass;
    }

    public void setIndexHandlerClass(final String indexHandlerClass)
    {
        this.indexHandlerClass = indexHandlerClass;
    }

    private String dbName;

    @ThriftField(value = 3, name = "dbName")
    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(final String dbName)
    {
        this.dbName = dbName;
    }

    private String origTableName;

    @ThriftField(value = 4, name = "origTableName")
    public String getOrigTableName()
    {
        return origTableName;
    }

    public void setOrigTableName(final String origTableName)
    {
        this.origTableName = origTableName;
    }

    private int createTime;

    @ThriftField(value = 5, name = "createTime")
    public int getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(final int createTime)
    {
        this.createTime = createTime;
    }

    private int lastAccessTime;

    @ThriftField(value = 6, name = "lastAccessTime")
    public int getLastAccessTime()
    {
        return lastAccessTime;
    }

    public void setLastAccessTime(final int lastAccessTime)
    {
        this.lastAccessTime = lastAccessTime;
    }

    private String indexTableName;

    @ThriftField(value = 7, name = "indexTableName")
    public String getIndexTableName()
    {
        return indexTableName;
    }

    public void setIndexTableName(final String indexTableName)
    {
        this.indexTableName = indexTableName;
    }

    private StorageDescriptor sd;

    @ThriftField(value = 8, name = "sd")
    public StorageDescriptor getSd()
    {
        return sd;
    }

    public void setSd(final StorageDescriptor sd)
    {
        this.sd = sd;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 9, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    private boolean deferredRebuild;

    @ThriftField(value = 10, name = "deferredRebuild")
    public boolean isDeferredRebuild()
    {
        return deferredRebuild;
    }

    public void setDeferredRebuild(final boolean deferredRebuild)
    {
        this.deferredRebuild = deferredRebuild;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("indexName", indexName)
            .add("indexHandlerClass", indexHandlerClass)
            .add("dbName", dbName)
            .add("origTableName", origTableName)
            .add("createTime", createTime)
            .add("lastAccessTime", lastAccessTime)
            .add("indexTableName", indexTableName)
            .add("sd", sd)
            .add("parameters", parameters)
            .add("deferredRebuild", deferredRebuild)
            .toString();
    }
}
