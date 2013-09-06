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
package com.facebook.hive.metastore.client.testing;

import static com.facebook.swift.service.guice.ThriftServiceExporter.thriftServerBinder;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.facebook.swift.service.guice.ThriftServerModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableIdentifier;

public class DummyHiveMetastoreServerModule implements Module
{
    @Override
    public void configure(final Binder binder)
    {
        binder.install (new ThriftServerModule());
        binder.bind(DummyHiveMetastore.class).in(Scopes.SINGLETON);
        thriftServerBinder(binder).exportThriftService(DummyHiveMetastore.class);
    }

    @ThriftService("dummy")
    public static final class DummyHiveMetastore
    {
        @ThriftMethod(value = "get_table")
        public Table getTable(@ThriftField(value = 1, name = "dbname") String dbName,
                              @ThriftField(value = 2, name = "tbl_name") String tblName)
        {
            return new Table(tblName, dbName, "", 0, 0, 0, null, ImmutableList.<FieldSchema>of(), ImmutableMap.<String, String>of(), "", "", "", null, null, ImmutableList.<TableIdentifier>of());
        }

    }
}
