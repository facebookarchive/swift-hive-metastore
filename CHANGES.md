* 2.0

- Rework the HiveMetastoreFactory; HiveMetastore objects are now lazily connected. Get rid of all
  methods that provide futures, only expose default client and a config specific method.
  This is a backwards incompatible change.
- Add retry logic to the default hive metastore. This exposes three new configuration options:
  hive-metastore.max-retries for the number of retries (default is 0, like the previous versions)
  hive-metastore.retry-timeout for the maximum time that the client will try to do an API call
  (default is 1 minute, like the old client code)
  hive-metastore.retry-sleep for the time that the client waits before trying to reconnect.
  (default is 10 seconds).

* 1.1

- add ThriftUnion based ColumnStatisticsData and the related methods. This requires swift 0.10 or newer.
- add Constants.java file which contains the constants from the IDL, remove hive_metastoreConstants.
- add additional c'tors to MetaException to allow explicit construction with Throwables.
- add APIs from Apache Hive: HiveMetaStoreClient, IMetaStoreClient, StatsSetupConst.
- add TableStats methods to MetaStoreUtils and Warehouse.
- add TableLink code and methods to MetaStoreUtils and TableType.


* 1.0

- First public release
