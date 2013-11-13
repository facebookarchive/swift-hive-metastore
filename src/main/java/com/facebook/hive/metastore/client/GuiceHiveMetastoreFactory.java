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

import com.facebook.swift.service.ThriftClient;
import com.google.inject.Inject;

import org.apache.thrift.transport.TTransportException;

import static com.google.common.base.Preconditions.checkNotNull;

public class GuiceHiveMetastoreFactory
    implements HiveMetastoreFactory
{
    private final HiveMetastoreClientConfig hiveMetastoreClientConfig;
    private final ThriftClient<HiveMetastore> thriftClient;

    @Inject
    GuiceHiveMetastoreFactory(final HiveMetastoreClientConfig hiveMetastoreClientConfig,
                              final ThriftClient<HiveMetastore> thriftClient)
    {
        this.hiveMetastoreClientConfig = checkNotNull(hiveMetastoreClientConfig, "hiveMetastoreConfig is null");
        this.thriftClient = checkNotNull(thriftClient, "thiftClient is null");
    }

    @Override
    public HiveMetastore getDefaultClient() throws InterruptedException, TTransportException
    {
        return getClientForHost(hiveMetastoreClientConfig);
    }

    @Override
    public HiveMetastore getClientForHost(final HiveMetastoreClientConfig config) throws InterruptedException, TTransportException
    {
        return new RetryingHiveMetastore(config, thriftClient);
    }

    @Override
    public HiveMetastore get() throws InterruptedException, TTransportException
    {
        return getDefaultClient();
    }
}
