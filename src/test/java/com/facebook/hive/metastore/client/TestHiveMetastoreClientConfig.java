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

import com.google.common.collect.ImmutableMap;

import io.airlift.units.Duration;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.airlift.configuration.testing.ConfigAssertions.assertFullMapping;
import static io.airlift.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static io.airlift.configuration.testing.ConfigAssertions.recordDefaults;

public class TestHiveMetastoreClientConfig
{
    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(recordDefaults(HiveMetastoreClientConfig.class)
            .setHost("localhost")
            .setPort(9083)
            .setFramed(false)
            .setMaxRetries(0)
            .setRetrySleep(new Duration (10, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(1, TimeUnit.MINUTES)));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
            .put("hive-metastore.host", "some.host")
            .put("hive-metastore.port", "12345")
            .put("hive-metastore.framed", "true")
            .put("hive-metastore.max-retries", "5")
            .put("hive-metastore.retry-sleep", "30s")
            .put("hive-metastore.retry-timeout", "2m")
            .build();

        HiveMetastoreClientConfig expected = new HiveMetastoreClientConfig()
            .setHost("some.host")
            .setPort(12345)
            .setFramed(true)
            .setMaxRetries(5)
            .setRetrySleep(new Duration(30, TimeUnit.SECONDS))
            .setRetryTimeout(new Duration(2, TimeUnit.MINUTES));

        assertFullMapping(properties, expected);
    }
}
