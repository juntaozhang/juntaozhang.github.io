# hbase

### create 
```
create 'test',{NAME =>'c', COMPRESSION => 'SNAPPY', VERSIONS => 1, BLOCKCACHE => 'false', BLOOMFILTER => 'ROW', DATA_BLOCK_ENCODING => 'FAST_DIFF'}, {SPLITS_FILE => '/home/dmpots/output_partitions.lst'}


hadoop distcp -pb -update -delete hdfs://bd04-001/tmp/device_id_tags_mapping_profile_full/hfile/c/ hdfs://bd15-130.yzdns.com/tmp/device_id_tags_mapping_profile_full/hfile/c/
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles  bulk_path table_name

测试了一下明确几个问题:
1. 数据文件目录: 
	/tmp/device_id_tags_mapping_profile_full/hfile/c/dataFile
2. 加载数据命令示例:
	hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles   /tmp/device_id_tags_mapping_profile_full/hfile/ rp_mobeye_o2o:device_id_tags_mapping_profile_full_tmp
3. 数据会被移动到/hbase/data对应表下
```


hadoop distcp -pb -update hdfs://bd04-001/user/dataengine/hbase/rp_device_profile_info_test/ hdfs://bd15-101.yzdns.com/tmp/rp_device_profile_info_test/

disable 'rp_mobeye_o2o:rp_device_profile_info_test'
drop 'rp_mobeye_o2o:rp_device_profile_info_test'
create 'rp_mobeye_o2o:rp_device_profile_info_test',{NAME =>'cf', COMPRESSION => 'SNAPPY', VERSIONS => 1, BLOCKCACHE => 'false', BLOOMFILTER => 'ROW', DATA_BLOCK_ENCODING => 'FAST_DIFF'}, {SPLITS_FILE => '/home/dmpots/output_partitions.lst'}


自定义partiton df join 

df.join hash过的

rdd.fullOuterJoin 不能保存顺序,
CoGroupedRDD
```
for ((it, depNum) <- rddIterators) {
      map.insertAll(it.map(pair => (pair._1, new CoGroupValue(pair._2, depNum))))
    }
```

fulljoin解决需要自己 merge row,通过单元测试走通
查看顺序

java jar orc-tools-1.3.3-uber.jar meta part-0001>part-0001
java jar orc-tools-1.3.3-uber.jar data part-0001>part-0001

TODO
Create an RDD for non-bucketed reads.
orc ACID 一定要看 spark-hive_2.11-2.1.0.cloudera1-sources.jar
OrcFileFormat vs OrcInputFormat 什么场景下使用? 有什么区别,这种改动有性能问题吗?(SplitGenerator.schedule)
orc bloomfilter

Table Parameters: 
spark.sql.sources.provider	orc


主要是shuffle阶段依赖的文件太多导致:具体问题还待研究

DiskStore IllegalArgumentException: Size exceeds Integer.MAX_VALUE 没有解决
http://bd04-031:18089/history/application_1530348593010_5185/1/executors/
https://issues.apache.org/jira/browse/SPARK-6238   
>hdfs dfs -du -h $HDFS_HIVE/rp_dataengine.db/rp_device_profile_info|grep -v "32."|grep -v "33."|grep -v "34."|grep -v "31."


数据量倾斜
```
496.8 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01260
496.1 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01261
493.5 M  1.4 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01262
496.0 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01263
496.4 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01264
496.6 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01265
493.3 M  1.4 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01266
493.7 M  1.4 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01267
495.9 M  1.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01268
493.3 M  1.4 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01269

852.5 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01270
853.4 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01271
854.3 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01272
853.2 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01273
853.0 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01274
853.0 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01275
855.2 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01276
853.6 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01277
853.1 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01278
855.2 M  2.5 G    /user/hive/warehouse/rp_dataengine.db/rp_device_profile_info/part-01279
```

排序 jvm 吞吐量很低 待解决,
memoryOverhead 网络 IO ? 哪里吃的内存

orc inputformat split 问题 => mapred.max.split.size

https://zhuanlan.zhihu.com/p/28574213

spark2-shell debug

```
:paste -raw
package org.apache.spark.rdd

Ctrl+D结束
```




spark2-shell https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-tips-and-tricks-access-private-members-spark-shell.html


- [hbase建表时region预分区的方法](https://blog.csdn.net/chaolovejia/article/details/46375849)