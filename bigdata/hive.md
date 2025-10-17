# hive

## debug
>export HADOOP_CLIENT_OPTS=" -Dcom.sun.management.jmxremote.port=27012 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution "
>hive --debug --hiveconf hive.root.logger=DEBUG,console  

## hive merge small files
```
hive.merge.mapredfiles
默认值： 在作业结束的时候是否合并小文件
说明： false

hive.merge.mapfiles
说明：Map-Only Job是否合并小文件
默认值：true

set hive.mergejob.maponly=true
说明： 在只有Map任务的时候 合并输出结果
默认值： true
```

## hive RANK
```
SELECT * FROM
(SELECT *, RANK() over (partition by sessionID, order by timestamp desc) as rank FROM clicks) ranked_clicks
WHERE ranked_clicks.rank=1;
```


## sql优化

1.减少join
```
CREATE TABLE clicks (
timestamp date, sessionID string, url string, source_ip string
) STORED as ORC tblproperties ("orc.compress" = "SNAPPY");

SELECT clicks.* FROM clicks inner join
(select sessionID, max(timestamp) as max_ts from clicks group by sessionID) latest
ON clicks.sessionID = latest.sessionID and
clicks.timestamp = latest.max_ts

SELECT * FROM
(SELECT *, RANK() over (partition by sessionID, order by timestamp desc) as rank
FROM clicks) ranked_clicks
WHERE ranked_clicks.rank=1;
```


hive.sql

```


CREATE TABLE IF NOT EXISTS hello(
  device string,
  phone string)
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat' ;


insert into table hello values ("1", '1,3'), ("2", '3,4,5'),("3","3");
add jar hdfs://ShareSdkHadoop/dmgroup/dba/commmon/udf/udf-manager-0.0.1-SNAPSHOT.jar;
create temporary function MD5_Eencrypt_UDF as 'com.youzu.mob.java.udf.MD5EencryptUDF';


CREATE TABLE IF NOT EXISTS device_imei(
  device string,
  imei string)
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat' ;

insert into table device_imei values ("1", '862736025874336'), ("2", '865124036127670'),("3","352562073479258");

select p from hello lateral view explode(split( phone,',')) tmp as p group by p;

select p,MD5_Eencrypt_UDF(p,32) from (select b.phone from device_imei a join hello b on a.device=b.device ) t lateral view explode(split(t.phone,',')) tmp as p group by p;


INSERT OVERWRITE LOCAL DIRECTORY 'test' select p,MD5_Eencrypt_UDF(p,32) 
  from (
    select b.phone from device_imei a join hello b on a.device=b.device where b.phone <> "" and b.phone IS NOT NULL
  ) t lateral view explode (split(t.phone,',')) tmp as p group by p;

INSERT OVERWRITE LOCAL DIRECTORY 'test1' select p 
  from ( 
    select b.phone from device_imei a join hello b on a.device=b.device where b.phone <> "" and b.phone IS NOT NULL
  ) t LATERAL VIEW EXPLODE(split(t.phone,',')) tmp as p group by p;


```
