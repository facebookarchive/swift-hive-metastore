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

import io.airlift.configuration.Config;
import io.airlift.units.Duration;
import io.airlift.units.MinDuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.concurrent.TimeUnit;

public class HiveMetastoreClientConfig
{
    private String host = "localhost";
    private int port = 9083;
    private boolean framed = false;
    private int maxRetries = 0;
    private Duration retryTimeout = new Duration (1, TimeUnit.MINUTES);
    private Duration retrySleep = new Duration (5, TimeUnit.SECONDS);

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

    @Min(1)
    @Max(65535)
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

    @Min(0)
    public int getMaxRetries()
    {
        return maxRetries;
    }

    @Config("hive-metastore.max-retries")
    public HiveMetastoreClientConfig setMaxRetries(final int maxRetries)
    {
        this.maxRetries = maxRetries;
        return this;
    }

    @MinDuration("1ms")
    public Duration getRetryTimeout()
    {
        return retryTimeout;
    }

    @Config("hive-metastore.retry-timeout")
    public HiveMetastoreClientConfig setRetryTimeout(final Duration retryTimeout)
    {
        this.retryTimeout = retryTimeout;
        return this;
    }

    @MinDuration("1ms")
    public Duration getRetrySleep()
    {
        return retrySleep;
    }

    @Config("hive-metastore.retry-sleep")
    public HiveMetastoreClientConfig setRetrySleep(final Duration retrySleep)
    {
        this.retrySleep = retrySleep;
        return this;
    }

}
