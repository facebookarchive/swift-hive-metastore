#!/usr/local/bin/thrift -java

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#
# Facebook extensions to the Hive Metastore.
# do not use these APIs when talking to an open source hive metastore.
#

include "hive_metastore.thrift"

namespace java org.apache.hadoop.hive.metastore.api

/**
 * Thrown when the table link specification is invalid:
 *   - table link to the same database,
 *   - table link not pointing to a managed or external table. 
 */
exception InvalidTableLinkDescriptionException {
  1: string message
}

service FacebookHiveMetastore extends hive_metastore.ThriftHiveMetastore {
##
## SWIFT-HIVE-METASTORE
##

#
# the method of this call differs from the original, open-source Hive.
#
  list<hive_metastore.Partition> exchange_partition(1:map<string, string> partitionSpecs,
                                     2:string source_db,
                                     3:string source_table_name,
                                     4:string dest_db,
                                     5:string dest_table_name,
                                     6:bool overwrite)
       throws(1:hive_metastore.MetaException o1,
              2:hive_metastore.NoSuchObjectException o2,
              3:hive_metastore.InvalidObjectException o3,
              4:hive_metastore.InvalidInputException o4,
              5:hive_metastore.AlreadyExistsException o5)

#
# Additional APIs supported by this client.
#

  void create_table_link(1:string dbName,
                         2:string targetDbName,
                         3:string targetTableName,
                         4:string owner,
                         5:bool isStatic,
                         6:map<string, string> linkProperties)
       throws (1:hive_metastore.AlreadyExistsException o1,
               2:hive_metastore.InvalidObjectException o2,
               3:hive_metastore.MetaException o3,
               4:hive_metastore.NoSuchObjectException o4,
               5:InvalidTableLinkDescriptionException o5)

  void drop_table_link(1:string dbName,
                       2:string targetDbName,
                       3:string targetTableName)
       throws (1:hive_metastore.NoSuchObjectException o1,
               2:hive_metastore.MetaException o2)

  bool exists_table(1:string dbname,
                    2:string tbl_name)
       throws (1:hive_metastore.MetaException o1)

  hive_metastore.Table get_table_link(1:string dbName,
                       2:string targetDbName,
                       3:string targetTableName)
       throws (1:hive_metastore.MetaException o1,
               2:hive_metastore.NoSuchObjectException o2)

  void alter_table_link(1:string dbName,
                        2:string targetDbName,
  	                    3:string targetTableName ,
                        4:hive_metastore.Table new_tbl)
       throws (1:hive_metastore.InvalidOperationException o1,
               2:hive_metastore.MetaException o2)

  void alter_table_link_properties(1:string dbName,
                                   2:string targetDbName,
                                   3:string targetTableName,
                                   4:map<string, string> updatedProperties)
       throws (1:hive_metastore.InvalidOperationException o1,
               2:hive_metastore.MetaException o2,
               3:hive_metastore.NoSuchObjectException o3)

  hive_metastore.Partition add_table_link_partition(1:string dbName,
                                     2:string targetDbName,
                                     3:string targetTableName,
                                     4:string partitionName)
       throws (1:hive_metastore.InvalidObjectException o1,
               2:hive_metastore.AlreadyExistsException o2,
               3:hive_metastore.NoSuchObjectException o3,
               4:hive_metastore.MetaException o4)

  bool drop_table_link_partition(1:string dbName,
                                 2:string targetDbName,
                                 3:string targetTableName,
                                 4:string partitionName)
       throws (1:hive_metastore.NoSuchObjectException o1,
               2:hive_metastore.MetaException o2)
  
  // Gets a new partition object with the given parameters. Similar to append_partition, 
  // but does not add the partition to metastore
  // It only works for tables; does not work for other objects like views.
  // The table is fetched from the metastore using the db name and the table name. 
  // However, the actual partition is not fetched from the metastore.
  // It does not matter whether the partition exists or not.
  // The partition values are used to construct a new partition.
  hive_metastore.Partition get_partition_template(1:string db_name,
                                   2:string tbl_name,
                                   3:list<string> part_vals)
       throws (1:hive_metastore.InvalidObjectException o1,
               2:hive_metastore.MetaException o2)

  // Return the total number of partitions for the given table and db name
  i32 get_total_partitions(1:string db_name,
                           2:string tbl_name)
       throws (1:hive_metastore.MetaException o1)

##
## SWIFT-HIVE-METASTORE
##
}


