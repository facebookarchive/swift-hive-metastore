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
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.google.inject.Injector;

import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.log.Logging;
import io.airlift.log.Logging.Level;
import io.airlift.units.Duration;

import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestRetryingHiveMetastore
{
    @Inject
    private LifeCycleManager lifecycleManager = null;

    private final ScheduledExecutorService runner = Executors.newScheduledThreadPool(5);

    @Before
    public void setUp()
    {
        Logging.initialize()
            .setLevel("com.facebook", Level.DEBUG);
    }

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
        if (lifecycleManager != null) {
            lifecycleManager.stop();
        }

        runner.shutdownNow();
    }

    @Test
    public void testNonExistent() throws Exception
    {
        final int port = NetUtils.findUnusedPort();

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig()
            .setPort(port)
            .setMaxRetries(5)
            .setRetrySleep(new Duration(1, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(30, TimeUnit.SECONDS));

        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final ThriftClientConfig clientConfig = new ThriftClientConfig();
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                assertFalse(metastore.isConnected());
                metastore.getTable("hello", "world");
                fail();
            }
            catch (TTransportException te) {
                assertEquals(TTransportException.UNKNOWN, te.getType());
            }
        }
    }

    @Test
    public void testExisting() throws Exception
    {
        final int port = NetUtils.findUnusedPort();

        startService(port);

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig()
            .setPort(port)
            .setMaxRetries(5)
            .setRetrySleep(new Duration(1, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(30, TimeUnit.SECONDS));

        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final ThriftClientConfig clientConfig = new ThriftClientConfig();
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                assertFalse(metastore.isConnected());

                final Table table = metastore.getTable("hello", "world");
                assertNotNull(table);
                assertEquals("hello", table.getDbName());
                assertEquals("world", table.getTableName());

                assertTrue(metastore.isConnected());
            }
        }
    }

    @Test
    public void testLate() throws Exception
    {
        final int port = NetUtils.findUnusedPort();


        runner.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    startService(port);
                }
                catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }, 10, TimeUnit.SECONDS);

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig()
            .setPort(port)
            .setMaxRetries(5)
            .setRetrySleep(new Duration(5, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(30, TimeUnit.SECONDS));

        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final ThriftClientConfig clientConfig = new ThriftClientConfig();
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getDefaultClient()) {
                assertFalse(metastore.isConnected());

                final Table table = metastore.getTable("hello", "world");
                assertNotNull(table);
                assertEquals("hello", table.getDbName());
                assertEquals("world", table.getTableName());

                assertTrue(metastore.isConnected());
            }
        }
    }

    @Test
    public void testLetsShuffleOne() throws Exception
    {
        final int port = NetUtils.findUnusedPort();

        final ImmutableSet.Builder<HostAndPort> builder = ImmutableSet.builder();
        builder.add(HostAndPort.fromParts("localhost", port));

        for (int i = 0; i < 3; i++) {
            builder.add(HostAndPort.fromParts("localhost", NetUtils.findUnusedPort()));
        }

        runner.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    startService(port);
                }
                catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }, 10, TimeUnit.SECONDS);

        final HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig()
            .setMaxRetries(10)
            .setRetrySleep(new Duration(3, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(45, TimeUnit.SECONDS));

        try (final ThriftClientManager clientManager = new ThriftClientManager()) {
            final ThriftClientConfig clientConfig = new ThriftClientConfig();
            final HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

            try (final HiveMetastore metastore = factory.getClientForHost(builder.build())) {
                assertFalse(metastore.isConnected());

                final Table table = metastore.getTable("hello", "world");
                assertNotNull(table);
                assertEquals("hello", table.getDbName());
                assertEquals("world", table.getTableName());

                assertTrue(metastore.isConnected());
            }
        }
    }

}

