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
package org.apache.hadoop.hive.metastore.api;

import static com.google.common.base.Objects.toStringHelper;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct("Order")
public class Order
{
    @ThriftConstructor
    public Order(
                 @ThriftField(value = 1, name = "col") final String col,
                 @ThriftField(value = 2, name = "order") final int order)
    {
        this.col = col;
        this.order = order;
    }

    public Order()
    {
    }

    private String col;

    @ThriftField(value = 1, name = "col")
    public String getCol()
    {
        return col;
    }

    public void setCol(final String col)
    {
        this.col = col;
    }

    private int order;

    @ThriftField(value = 2, name = "order")
    public int getOrder()
    {
        return order;
    }

    public void setOrder(final int order)
    {
        this.order = order;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
            .add("col", col)
            .add("order", order)
            .toString();
    }
}
