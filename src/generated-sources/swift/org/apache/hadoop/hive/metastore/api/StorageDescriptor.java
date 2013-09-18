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
package org.apache.hadoop.hive.metastore.api;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.toStringHelper;

@ThriftStruct("StorageDescriptor")
public class StorageDescriptor
{
    @ThriftConstructor
    public StorageDescriptor(
                             @ThriftField(value = 1, name = "cols") final List<FieldSchema> cols,
                             @ThriftField(value = 2, name = "location") final String location,
                             @ThriftField(value = 3, name = "inputFormat") final String inputFormat,
                             @ThriftField(value = 4, name = "outputFormat") final String outputFormat,
                             @ThriftField(value = 5, name = "compressed") final boolean compressed,
                             @ThriftField(value = 6, name = "numBuckets") final int numBuckets,
                             @ThriftField(value = 7, name = "serdeInfo") final SerDeInfo serdeInfo,
                             @ThriftField(value = 8, name = "bucketCols") final List<String> bucketCols,
                             @ThriftField(value = 9, name = "sortCols") final List<Order> sortCols,
                             @ThriftField(value = 10, name = "parameters") final Map<String, String> parameters,
                             @ThriftField(value = 11, name = "skewedInfo") final SkewedInfo skewedInfo,
                             @ThriftField(value = 12, name = "storedAsSubDirectories") final boolean storedAsSubDirectories,
                             @ThriftField(value = 13, name = "statsFresh") final boolean statsFresh)
    {
        this.cols = cols;
        this.location = location;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.compressed = compressed;
        this.numBuckets = numBuckets;
        this.serdeInfo = serdeInfo;
        this.bucketCols = bucketCols;
        this.sortCols = sortCols;
        this.parameters = parameters;
        this.skewedInfo = skewedInfo;
        this.storedAsSubDirectories = storedAsSubDirectories;
        this.statsFresh = statsFresh;
    }

    public StorageDescriptor()
    {
    }

    private List<FieldSchema> cols;

    @ThriftField(value = 1, name = "cols")
    public List<FieldSchema> getCols()
    {
        return cols;
    }

    public void setCols(final List<FieldSchema> cols)
    {
        this.cols = cols;
    }

    private String location;

    @ThriftField(value = 2, name = "location")
    public String getLocation()
    {
        return location;
    }

    public void setLocation(final String location)
    {
        this.location = location;
    }

    private String inputFormat;

    @ThriftField(value = 3, name = "inputFormat")
    public String getInputFormat()
    {
        return inputFormat;
    }

    public void setInputFormat(final String inputFormat)
    {
        this.inputFormat = inputFormat;
    }

    private String outputFormat;

    @ThriftField(value = 4, name = "outputFormat")
    public String getOutputFormat()
    {
        return outputFormat;
    }

    public void setOutputFormat(final String outputFormat)
    {
        this.outputFormat = outputFormat;
    }

    private boolean compressed;

    @ThriftField(value = 5, name = "compressed")
    public boolean isCompressed()
    {
        return compressed;
    }

    public void setCompressed(final boolean compressed)
    {
        this.compressed = compressed;
    }

    private int numBuckets;

    @ThriftField(value = 6, name = "numBuckets")
    public int getNumBuckets()
    {
        return numBuckets;
    }

    public void setNumBuckets(final int numBuckets)
    {
        this.numBuckets = numBuckets;
    }

    private SerDeInfo serdeInfo;

    @ThriftField(value = 7, name = "serdeInfo")
    public SerDeInfo getSerdeInfo()
    {
        return serdeInfo;
    }

    public void setSerdeInfo(final SerDeInfo serdeInfo)
    {
        this.serdeInfo = serdeInfo;
    }

    private List<String> bucketCols;

    @ThriftField(value = 8, name = "bucketCols")
    public List<String> getBucketCols()
    {
        return bucketCols;
    }

    public void setBucketCols(final List<String> bucketCols)
    {
        this.bucketCols = bucketCols;
    }

    private List<Order> sortCols;

    @ThriftField(value = 9, name = "sortCols")
    public List<Order> getSortCols()
    {
        return sortCols;
    }

    public void setSortCols(final List<Order> sortCols)
    {
        this.sortCols = sortCols;
    }

    private Map<String, String> parameters;

    @ThriftField(value = 10, name = "parameters")
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    private SkewedInfo skewedInfo;

    @ThriftField(value = 11, name = "skewedInfo")
    public SkewedInfo getSkewedInfo()
    {
        return skewedInfo;
    }

    public void setSkewedInfo(final SkewedInfo skewedInfo)
    {
        this.skewedInfo = skewedInfo;
    }

    private boolean storedAsSubDirectories;

    @ThriftField(value = 12, name = "storedAsSubDirectories")
    public boolean isStoredAsSubDirectories()
    {
        return storedAsSubDirectories;
    }

    public void setStoredAsSubDirectories(final boolean storedAsSubDirectories)
    {
        this.storedAsSubDirectories = storedAsSubDirectories;
    }

    private boolean statsFresh;

    @ThriftField(value = 13, name = "statsFresh")
    public boolean isStatsFresh()
    {
        return statsFresh;
    }

    public void setStatsFresh(final boolean statsFresh)
    {
        this.statsFresh = statsFresh;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("cols", cols)
            .add("location", location)
            .add("inputFormat", inputFormat)
            .add("outputFormat", outputFormat)
            .add("compressed", compressed)
            .add("numBuckets", numBuckets)
            .add("serdeInfo", serdeInfo)
            .add("bucketCols", bucketCols)
            .add("sortCols", sortCols)
            .add("parameters", parameters)
            .add("skewedInfo", skewedInfo)
            .add("storedAsSubDirectories", storedAsSubDirectories)
            .add("statsFresh", statsFresh)
            .toString();
    }
}
