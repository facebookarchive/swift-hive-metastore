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

public final class Constants
{
    private Constants()
    {
    }

    public static final String BUCKET_COUNT = "bucket_count";

    public static final String BUCKET_FIELD_NAME = "bucket_field_name";

    public static final String DDL_TIME = "transient_lastDdlTime";

    public static final String FIELD_TO_DIMENSION = "field_to_dimension";

    public static final String FILE_INPUT_FORMAT = "file.inputformat";

    public static final String FILE_OUTPUT_FORMAT = "file.outputformat";

    public static final String HIVE_FILTER_FIELD_LAST_ACCESS = "hive_filter_field_last_access__";

    public static final String HIVE_FILTER_FIELD_OWNER = "hive_filter_field_owner__";

    public static final String HIVE_FILTER_FIELD_PARAMS = "hive_filter_field_params__";

    public static final String IS_ARCHIVED = "is_archived";

    public static final String META_TABLE_COLUMNS = "columns";

    public static final String META_TABLE_COLUMN_TYPES = "columns.types";

    public static final String META_TABLE_DB = "db";

    public static final String META_TABLE_LOCATION = "location";

    public static final String META_TABLE_NAME = "name";

    public static final String META_TABLE_PARTITION_COLUMNS = "partition_columns";

    public static final String META_TABLE_SERDE = "serde";

    public static final String META_TABLE_STORAGE = "storage_handler";

    public static final String ORIGINAL_LOCATION = "original_location";
}
