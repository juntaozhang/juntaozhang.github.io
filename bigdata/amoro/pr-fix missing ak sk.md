[AMORO-4063] Fix missing S3 AK/SK in Apache Paimon format

request payload:
```text
{
  "name": "test",
  "type": "filesystem",
  "optimizerGroup": "local",
  "tableFormatList": [
    "PAIMON"
  ],
  "storageConfig": {
    "hadoop.core.site": "",
    "hadoop.hdfs.site": "",
    "storage.type": "S3",
    "storage.s3.endpoint": "http://localhost:32000"
  },
  "authConfig": {
    "auth.kerberos.keytab": "",
    "auth.kerberos.krb5": "",
    "auth.type": "AK/SK",
    "auth.ak_sk.access_key": "test",
    "auth.ak_sk.secret_key": "11111111"
  },
  "properties": {
    "warehouse": "s3://warehouse/paimon",
    "s3.path.style.access": "true"
  },
  "tableProperties": {}
}
```

response:
```text
{
  "message": "org.apache.paimon.fs.UnsupportedSchemeException: Could not find a file io implementation for scheme 's3' in the classpath.  Hadoop FileSystem also cannot access this path 's3://warehouse/paimon'.",
  "code": 500,
  "requestId": ""
}
```

detail log:
```text
Caused by: org.apache.paimon.fs.UnsupportedSchemeException: Could not find a file io implementation for scheme 's3' in the classpath.  Hadoop FileSystem also cannot access this path 's3://warehouse/paimon'.
at org.apache.paimon.fs.FileIO.get(FileIO.java:572) ~[paimon-bundle-1.2.0.jar:1.2.0]
at org.apache.paimon.catalog.CatalogFactory.createUnwrappedCatalog(CatalogFactory.java:97) ~[paimon-bundle-1.2.0.jar:1.2.0]
... 71 more
Suppressed: java.io.IOException: One or more required options are missing.

Missing required options are:

s3.access-key
s3.secret-key
```