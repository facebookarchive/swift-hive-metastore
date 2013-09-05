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

import static com.facebook.swift.service.guice.ThriftClientBinder.thriftClientBinder;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.airlift.configuration.ConfigurationModule.bindConfig;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

import org.apache.thrift.transport.TTransportException;

public class HiveMetastoreClientModule
    implements Module
{
    @Override
    public void configure(final Binder binder)
    {
        bindConfig(binder).to(HiveMetastoreClientConfig.class);
        thriftClientBinder(binder).bindThriftClient(HiveMetastore.class);

        binder.bind(HiveMetastoreFactory.class).to(GuiceHiveMetastoreFactory.class).in(Scopes.SINGLETON);
        binder.bind(HiveMetastoreClientProvider.class).in(Scopes.SINGLETON);
        ThrowingProviderBinder.create(binder).bind(HiveMetastoreProvider.class, HiveMetastore.class).to(HiveMetastoreClientProvider.class);
    }

    public static class HiveMetastoreClientProvider
        implements HiveMetastoreProvider<HiveMetastore>
    {
        private final HiveMetastoreFactory metastoreFactory;

        @Inject
        HiveMetastoreClientProvider(final HiveMetastoreFactory metastoreFactory)
        {
            this.metastoreFactory = checkNotNull(metastoreFactory, "metastoreFactory is null");
        }

        @Override
        public HiveMetastore get() throws InterruptedException, TTransportException
        {
            return metastoreFactory.getDefaultClient();
        }
    }
}
