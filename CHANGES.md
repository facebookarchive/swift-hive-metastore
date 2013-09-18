* 1.1

- add ThriftUnion based ColumnStatisticsData and the related methods. This requires swift 0.10 or newer.
- add Constants.java file which contains the constants from the IDL, remove hive_metastoreConstants.
- add additional c'tors to MetaException to allow explicit construction with Throwables.
- add APIs from Apache Hive: HiveMetaStoreClient, IMetaStoreClient, StatsSetupConst.
- add TableStats methods to MetaStoreUtils and Warehouse.
- add TableLink code and methods to MetaStoreUtils and TableType.


* 1.0

- First public release
