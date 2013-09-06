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

import static com.google.common.base.Preconditions.checkNotNull;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.nifty.client.UnframedClientConnector;
import com.facebook.swift.service.ThriftClient;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;

import org.apache.thrift.transport.TTransportException;

import java.util.concurrent.ExecutionException;

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
    public ListenableFuture<HiveMetastore> getDefaultClientAsFuture()
    {
        return getClientForHostAsFuture(hiveMetastoreClientConfig.getHostAndPort(), hiveMetastoreClientConfig.isFramed());
    }

    @Override
    public ListenableFuture<HiveMetastore> getClientForHostAsFuture(final HostAndPort hostAndPort, final boolean framed)
    {
        final NiftyClientConnector<? extends NiftyClientChannel> clientConnector = framed
            ? new FramedClientConnector(hostAndPort)
            : new UnframedClientConnector(hostAndPort);

        return thriftClient.open(clientConnector);
    }

    @Override
    public HiveMetastore getDefaultClient() throws InterruptedException, TTransportException
    {
        return getClientForHost(hiveMetastoreClientConfig.getHostAndPort(), hiveMetastoreClientConfig.isFramed());
    }

    @Override
    public HiveMetastore getClientForHost(final HostAndPort hostAndPort, final boolean framed) throws InterruptedException, TTransportException
    {
        try {
            return getClientForHostAsFuture(hostAndPort, framed).get();
        }
        catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            Throwables.propagateIfInstanceOf(t, TTransportException.class);
            throw Throwables.propagate(t);
        }
    }
}
