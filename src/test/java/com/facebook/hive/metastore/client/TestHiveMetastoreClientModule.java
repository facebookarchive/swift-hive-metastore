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
package com.facebook.hive.metastore.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.facebook.hive.metastore.client.HiveMetastoreClientModule.HiveMetastoreClientProvider;
import com.facebook.hive.metastore.client.testing.DummyHiveMetastoreServerModule;
import com.facebook.hive.metastore.client.testing.NetUtils;
import com.facebook.swift.codec.guice.ThriftCodecModule;
import com.facebook.swift.service.guice.ThriftClientModule;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import io.airlift.bootstrap.Bootstrap;

import java.util.Map;

public class TestHiveMetastoreClientModule
{
    @Inject
    public HiveMetastoreClientProvider metastoreProvider = null;

    @Test
    public void testSimple() throws Exception
    {
        final String port = Integer.toString(NetUtils.findUnusedPort());

        final Map<String, String> properties = ImmutableMap.of("hive-metastore.port", port,
                                                               "thrift.port", port);

        final Injector inj = new Bootstrap(new DummyHiveMetastoreServerModule(),
                                           new HiveMetastoreClientModule(),
                                           new ThriftClientModule(),
                                           new ThriftCodecModule())
            .setRequiredConfigurationProperties(properties)
            .strictConfig()
            .initialize();

        inj.injectMembers(this);

        final HiveMetastore metastore = metastoreProvider.get();

        assertNotNull(metastore);

        final Table table = metastore.getTable("hello", "world");
        assertNotNull(table);
        assertEquals("hello", table.getDbName());
        assertEquals("world", table.getTableName());

    }

    @Test
    public void testLateConnectIsOk() throws Exception
    {
        final String port = Integer.toString(NetUtils.findUnusedPort());

        final Map<String, String> properties = ImmutableMap.of("hive-metastore.port", port);

        final Injector inj = new Bootstrap(new HiveMetastoreClientModule(),
                                           new ThriftClientModule(),
                                           new ThriftCodecModule())
            .setRequiredConfigurationProperties(properties)
            .strictConfig()
            .initialize();

        inj.injectMembers(this);

        HiveMetastore metastore = null;

        try {
            metastore = metastoreProvider.get();
            fail();
        }
        catch (TTransportException e) {

        }

        assertNull(metastore);

        final Map<String, String> serverProps = ImmutableMap.of("thrift.port", port);

        new Bootstrap(new DummyHiveMetastoreServerModule(),
                      new HiveMetastoreClientModule(),
                      new ThriftClientModule(),
                      new ThriftCodecModule())
            .setRequiredConfigurationProperties(serverProps)
            .strictConfig()
            .initialize();

        metastore = metastoreProvider.get();

        final Table table = metastore.getTable("hello", "world");
        assertNotNull(table);
        assertEquals("hello", table.getDbName());
        assertEquals("world", table.getTableName());
    }
}
