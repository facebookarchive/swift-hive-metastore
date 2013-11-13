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
package com.facebook.hive.metastore.client;

import com.facebook.hive.metastore.client.testing.DummyHiveMetastoreServerModule;
import com.facebook.hive.metastore.client.testing.NetUtils;
import com.facebook.swift.codec.guice.ThriftCodecModule;
import com.facebook.swift.service.ThriftClientConfig;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;

import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;

import org.apache.hadoop.hive.metastore.api.Table;
import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestHiveMetastoreClient
{
    @Inject
    private LifeCycleManager lifecycleManager = null;

    private void startService(int port) throws Exception
    {
        final Map<String, String> properties = ImmutableMap.of("thrift.port", Integer.toString(port));

        final Injector inj = new Bootstrap(new DummyHiveMetastoreServerModule(),
                                           new ThriftCodecModule())
            .setRequiredConfigurationProperties(properties)
            .doNotInitializeLogging()
            .strictConfig()
            .initialize();

        inj.injectMembers(this);
    }

    @After
    public void tearDown() throws Exception
    {
        assertNotNull(lifecycleManager);
        lifecycleManager.stop();
    }

    @Test
    public void testSimple() throws Exception
    {
        final int port = NetUtils.findUnusedPort();

        startService(port);

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig().setPort(port);
        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final ThriftClientConfig clientConfig = new ThriftClientConfig();
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                final Table table = metastore.getTable("hello", "world");
                assertNotNull(table);
                assertEquals("hello", table.getDbName());
                assertEquals("world", table.getTableName());
            }
        }
    }

    @Test
    public void testLateConnectIsOk() throws Exception
    {
        final int port = NetUtils.findUnusedPort();

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig().setPort(port);
        final ThriftClientConfig clientConfig = new ThriftClientConfig();
        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                assertFalse(metastore.isConnected());
            }

            startService(port);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                final Table table = metastore.getTable("hello", "world");
                assertNotNull(table);
                assertEquals("hello", table.getDbName());
                assertEquals("world", table.getTableName());
            }
        }
    }

}

