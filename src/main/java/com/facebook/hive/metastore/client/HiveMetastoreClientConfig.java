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

import com.google.common.net.HostAndPort;

import io.airlift.configuration.Config;

import javax.validation.constraints.NotNull;

public class HiveMetastoreClientConfig
{
    private String host = "localhost";
    private int port = 9083;
    private boolean framed = false;

    @NotNull
    public String getHost()
    {
        return host;
    }

    @Config("hive-metastore.host")
    public HiveMetastoreClientConfig setHost(final String host)
    {
        this.host = host;
        return this;
    }

    @NotNull
    public int getPort()
    {
        return port;
    }

    @Config("hive-metastore.port")
    public HiveMetastoreClientConfig setPort(final int port)
    {
        this.port = port;
        return this;
    }

    public boolean isFramed()
    {
        return framed;
    }

    @Config("hive-metastore.framed")
    public HiveMetastoreClientConfig setFramed(final boolean framed)
    {
        this.framed = framed;
        return this;
    }

    public HostAndPort getHostAndPort()
    {
        return HostAndPort.fromParts(host, port);
    }

}
