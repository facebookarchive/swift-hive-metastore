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

import static io.airlift.configuration.testing.ConfigAssertions.assertFullMapping;
import static io.airlift.configuration.testing.ConfigAssertions.assertRecordedDefaults;
import static io.airlift.configuration.testing.ConfigAssertions.recordDefaults;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.Map;

public class TestHiveMetastoreClientConfig
{
    @Test
    public void testDefaults()
    {
        assertRecordedDefaults(recordDefaults(HiveMetastoreClientConfig.class)
            .setHost("localhost")
            .setPort(9083)
            .setFramed(false));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
            .put("hive-metastore.host", "some.host")
            .put("hive-metastore.port", "12345")
            .put("hive-metastore.framed", "true")
            .build();

        HiveMetastoreClientConfig expected = new HiveMetastoreClientConfig()
            .setHost("some.host")
            .setPort(12345)
            .setFramed(true);

        assertFullMapping(properties, expected);
    }
}
