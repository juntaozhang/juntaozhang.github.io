# join

>sql("select count(1) from rp_sdk_mobeye.rp_mobeye_tfidf_pca_tags_mapping a join test.lmz_lookalike_huanbei_testset b on a.device=b.device").explain  

```
== Physical Plan ==
*HashAggregate(keys=[], functions=[count(1)])
+- Exchange SinglePartition
   +- *HashAggregate(keys=[], functions=[partial_count(1)])
      +- *Project
         +- *SortMergeJoin [device#1393], [device#1424], Inner
            :- *Sort [device#1393 ASC NULLS FIRST], false, 0
            :  +- Exchange hashpartitioning(device#1393, 200)
            :     +- *Filter isnotnull(device#1393)
            :        +- HiveTableScan [device#1393], MetastoreRelation rp_sdk_mobeye, rp_mobeye_tfidf_pca_tags_mapping
            +- *Sort [device#1424 ASC NULLS FIRST], false, 0
               +- Exchange hashpartitioning(device#1424, 200)
                  +- *Filter isnotnull(device#1424)
                     +- HiveTableScan [device#1424], MetastoreRelation test, lmz_lookalike_huanbei_testset

```

>val big = sql("select * from rp_sdk_mobeye.rp_mobeye_tfidf_pca_tags_mapping")  
>val small = sql("select * from test.lmz_lookalike_huanbei_testset")  
>big.join(broadcast(small),big("device") === small("device")).explain  

```
== Physical Plan ==
*BroadcastHashJoin [device#704], [device#875], Inner, BuildRight
:- *Filter isnotnull(device#704)
:  +- HiveTableScan [device#704, imei#705, mac#706, phone#707, tfidflist#708, country#709, province#710, city#711, gender#712, agebin#713, segment#714, edu#715, kids#716, income#717, cell_factory#718, model#719, model_level#720, carrier#721, network#722, screensize#723, sysver#724, occupation#725, house#726, repayment#727, ... 8 more fields], MetastoreRelation rp_sdk_mobeye, rp_mobeye_tfidf_pca_tags_mapping
+- BroadcastExchange HashedRelationBroadcastMode(List(input[0, string, false]))
   +- *Filter isnotnull(device#875)
      +- InMemoryTableScan [device#875, tfidflist#876], [isnotnull(device#875)]
            +- InMemoryRelation [device#875, tfidflist#876], true, 10000, StorageLevel(disk, memory, deserialized, 1 replicas), `deviceIdTab`
                  +- HiveTableScan [device#509, tfidflist#510], MetastoreRelation test, lmz_lookalike_huanbei_testset
```