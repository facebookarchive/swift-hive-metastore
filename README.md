swift-hive-metastore
====================

A swift based client for the Hive Metastore. Swift is an annotation based implementation for the Thrift protocol, available
from https://github.com/facebook/swift.

Goal of this project is to replace

```xml
<dependency>
    <groupId>org.apache.hive</groupId>
    <artifactId>hive-metastore</artifactId>
</dependency>
```

as a client library for users of the Hive metastore.

Only supports Thrift based metastores, there is no support for a local
metastore. This is intentional to enforce usage of the Thrift API for
clients.

The library is supposed to be an API drop-in, it also supports a number of
helper APIs from the hive-metastore dependency:

* `org.apache.hadoop.hive.metastore.MetaStoreUtils`
* `org.apache.hadoop.hive.metastore.api.hive_metastoreConstants`
* `org.apache.hadoop.hive.metastore.TableType`
* `org.apache.hadoop.hive.metastore.MetaStoreFS`
* `org.apache.hadoop.hive.metastore.ProtectMode`
* `org.apache.hadoop.hive.metastore.Warehouse`

All client pieces that are different from the Apache Hive Metastore are located in the `com.facebook.hive.metastore` package.


Usage
-----

```java
HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig();
try (ThriftClientManager clientManager = new ThriftClientManager()) {
    ThriftClientConfig clientConfig = new ThriftClientConfig();
    HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

    try (HiveMetastore metastore = factory.getDefaultClient()) {
        Table table = metastore.getTable("hello", "world");

    }
}
```

The Metastore API uses new API names (Java style). For drop-in compatibility, use

```java
HiveMetastoreClientConfig metastoreConfig = new HiveMetastoreClientConfig();
try (ThriftClientManager clientManager = new ThriftClientManager()) {
    ThriftClientConfig clientConfig = new ThriftClientConfig();
    HiveMetastoreFactory factory = new SimpleHiveMetastoreFactory(clientManager, clientConfig, metastoreConfig);

    try (HiveMetastore metastore = factory.getDefaultClient()) {
        ThriftHiveMetastore.Client client = ThriftHiveMetastore.Client.forHiveMetastore(metastore);
        Table table = client.get_table("hello", "world");

    }
}
```

Using Guice
-----------

A Guice module is available that allows integration of the client using dependecy injection with Googl Guice. 
This requires the `ThriftClientModule` and the `ThriftCodecModule` from swift.

```java
Injector inj = Guice.createInjector(Stage.PRODUCTION, 
                                    new HiveMetastoreClientModule(),
                                    new ThriftClientModule(),
                                    new ThriftCodecModule());

HiveMetastoreProvider provider = inj.getInstance(HiveMetastoreProvider.class);
HiveMetastore metastore = metastoreProvider.get();

Table table = metastore.getTable("hello", "world");
...
```

Known problems
--------------

Swift does not support unions yet. So ColumnStatisticsData,
ColumnStatisticsObj and the associated API calls are not
available. This will be fixed in the next release.

