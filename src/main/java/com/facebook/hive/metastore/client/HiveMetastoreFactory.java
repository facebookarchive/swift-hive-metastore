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

import com.google.common.net.HostAndPort;

import java.util.Set;

public interface HiveMetastoreFactory
    extends HiveMetastoreProvider<HiveMetastore>
{
    /**
     * Returns a client connected to the store using the default configuration settings.
     */
    HiveMetastore getDefaultClient();

    /**
     * Return a client connected to the store described in the supplied {@link HostAndPort} element. All
     * additional settings are taken from the default configuration settings.
     */
    HiveMetastore getClientForHost(HostAndPort hostAndPort);

    /**
     * Return a client connected to the store described in the {@link HiveMetastoreClientConfig} object.
     */
    HiveMetastore getClientForHost(HiveMetastoreClientConfig config);

    /**
     * Return a client connected to one of the stores described in the supplied set of {@link HostAndPort} elements.
     * All additional settings are taken from the default configuration settings. When retrying, another store is used.
     */
    HiveMetastore getClientForHost(Set<HostAndPort> hostAndPorts);

    /**
     * Return a client connected to one of the stores described in the supplied set of {@link HostAndPort} elements.
     * All additional settings are taken from the configuration supplied. When retrying, another store is used.
     */
    HiveMetastore getClientForHost(Set<HostAndPort> hostAndPorts, HiveMetastoreClientConfig config);
}
