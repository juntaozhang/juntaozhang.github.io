# spark-conf

- 内存配置
--driver-memory 4g --executor-memory 12g --executor-cores 4

- shuffle fetch最大传输请求
--conf spark.reducer.maxReqsInFlight=1   \
--conf spark.reducer.maxSizeInFlight=24m  \

- suffle partition数量
--conf spark.sql.shuffle.partitions=2000     \

- 启动推测
--conf spark.speculation=true     \
--conf spark.speculation.quantile=0.95     \

- spark首先使用用户jar
--conf spark.driver.userClassPathFirst=true	\
--conf spark.executor.userClassPathFirst=true	\


- 启动动态指定
--conf spark.dynamicAllocation.enabled=true \
--conf spark.dynamicAllocation.initialExecutors=10     \
--conf spark.dynamicAllocation.minExecutors=1     \
--conf spark.dynamicAllocation.maxExecutors=50     \

- 指定jdk
--conf spark.yarn.appMasterEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \
--conf spark.executorEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \

- metrics
--files /home/dataengine/metrics.properties
--conf spark.metrics.conf=metrics.properties  \
--conf spark.executor.extraClassPath=dataengine-commons-v0.7.3.2.jar:xxx.jar   \

- log级别
--files /home/dataengine/log4j.properties

- G1 gc日志
--conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:MaxNewSize=5g" \

-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution
-verbose:gc 
- class from which jar
-verbose:class


#--conf spark.yarn.executor.memoryOverhead=5120 \
#export SPARK_SUBMIT_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=4000
#--conf spark.network.timeout=300 \
#--conf spark.driver.maxResultSize=4096m \
#--conf spark.kryoserializer.buffer.max=256m \
#--conf spark.app.name=mobeye-o2o-streaming \
#--conf spark.sql.shuffle.partitions=100 \
#--conf spark.task.maxFailures=1 \
#--conf spark.executor.extraJavaOptions="-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+ParallelRefProcEnabled -XX:+CMSClassUnloadingEnabled -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+HeapDumpOnOutOfMemoryError -verbose:gc" \
#--conf spark.executor.extraJavaOptions="-XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark -XX:+AlwaysPreTouch -XX:InitiatingHeapOccupancyPercent=30 -XX:ParallelGCThreads=10 -XX:-OmitStackTraceInFastThrow -XX:+UseCompressedStrings -XX:+UseStringCache -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -verbose:gc" \


:require /opt/jdk1.8.0_45/mysql-connector-java.jar



- carbon jars
--jars /data/jars/carbondata_2.11-1.4.0-dist/carbondata-bloom-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-core-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-format-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-hadoop-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-lucene-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-processing-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-search-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark2-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-store-sdk-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-streaming-1.4.0.jar 






spark2-shell --driver-memory 5g --executor-memory 20g --executor-cores 4     \
    --conf spark.shuffle.service.enabled=true \
    --conf spark.memory.fraction=0.75 \
    --conf spark.dynamicAllocation.enabled=true \
    --conf spark.dynamicAllocation.initialExecutors=10     \
    --conf spark.dynamicAllocation.maxExecutors=30     \
    --conf spark.speculation=true     \
    --conf spark.speculation.quantile=0.95     \
    --conf spark.sql.shuffle.partitions=2000     \
    --conf spark.yarn.appMasterEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \
    --conf spark.yarn.executorEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \
    --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:MaxNewSize=5g" \
    --jars /data/jars/carbondata_2.11-1.4.0-dist/carbondata-bloom-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-core-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-format-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-hadoop-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-lucene-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-processing-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-search-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark2-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-store-sdk-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-streaming-1.4.0.jar 



spark2-shell --driver-memory 4g --executor-memory 12g --executor-cores 4     \
    --conf spark.shuffle.service.enabled=true \
    --conf spark.memory.fraction=0.75 \
    --conf spark.memory.storageFraction=0.1     \
    --conf spark.shuffle.spill.initialMemoryThreshold=134217728     \
    --conf spark.dynamicAllocation.enabled=true \
    --conf spark.dynamicAllocation.initialExecutors=10     \
    --conf spark.dynamicAllocation.maxExecutors=30     \
    --conf spark.speculation=true     \
    --conf spark.speculation.quantile=0.95     \
    --conf spark.sql.shuffle.partitions=2000     \
    --conf spark.yarn.appMasterEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \
    --conf spark.yarn.executorEnv.JAVA_HOME="/opt/jdk1.8.0_45"     \
    --files /home/dataengine/log4j.properties,/home/dataengine/metrics.properties  \
    --conf spark.metrics.conf=metrics.properties  \
    --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:G1NewSizePercent=20 -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Dlog4j.configuration=file:log4j.properties -Dcom.sun.management.jmxremote.port=27012 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

## carbon
spark2-submit --driver-memory 4g --executor-memory 8G --executor-cores 4 \
    --master yarn --deploy-mode cluster  \
    --conf spark.yarn.appMasterEnv.JAVA_HOME="/opt/jdk1.8.0_45" \
    --conf spark.yarn.executorEnv.JAVA_HOME="/opt/jdk1.8.0_45" \
    --conf spark.speculation=true \
    --conf spark.speculation.quantile=0.90 \
    --conf 'spark.driver.extraJavaOptions=-XX:PermSize=128M -XX:MaxPermSize=256M -Dlog4j.configuration=file:log4j.properties' \
    --conf spark.dynamicAllocation.minExecutors=2 \
    --conf spark.dynamicAllocation.maxExecutors=20 \
    --conf spark.yarn.executor.memoryOverhead=4069 \
    --conf spark.sql.shuffle.partitions=1000  \
    --files /data/jars/carbondata_2.11-1.4.0-dist/log4j.properties \
    --jars /data/jars/carbondata_2.11-1.4.0-dist/carbondata-bloom-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-core-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-format-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-hadoop-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-lucene-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-processing-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-search-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark2-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-spark-common-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-store-sdk-1.4.0.jar,/data/jars/carbondata_2.11-1.4.0-dist/carbondata-streaming-1.4.0.jar \
    --class com.mob.ga.ip.pegging.PoiIpBssidDeviceCarbonQueryAPI --queue dataexport \
    datamigration-0.0.1-jar-with-dependencies.jar

## metrics

/usr/bin/spark2-submit --executor-memory 10g    \
    --master yarn   \
    --executor-cores 4  \
    --name TagsChecker[20180827]-34372a84-217b-4866-b801-90d62984a5b0   \
    --deploy-mode cluster   \
    --driver-memory 4g  \
    --class com.mob.dataengine.utils.tags.TagsChecker   \
    --conf "spark.speculation.quantile=0.98"    \
    --conf "spark.executorEnv.JAVA_HOME=/opt/jdk1.8.0_45"   \
    --conf "spark.yarn.appMasterEnv.JAVA_HOME=/opt/jdk1.8.0_45" \
    --conf "spark.shuffle.service.enabled=true" \
    --conf "spark.dynamicAllocation.minExecutors=1" \
    --conf "spark.driver.extraJavaOptions=-XX:+UseG1GC"   \
    --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC"   \
    --conf "spark.dynamicAllocation.maxExecutors=20"    \
    --conf "spark.speculation=true" \
    --conf spark.metrics.conf=metrics.properties  \
    --conf spark.driver.extraClassPath=spark-metrics-v0.0.1.jar,simpleclient-0.5.0.jar,simpleclient_common-0.5.0.jar,simpleclient_dropwizard-0.5.0.jar,simpleclient_pushgateway-0.5.0.jar   \
    --conf spark.executor.extraClassPath=spark-metrics-v0.0.1.jar,simpleclient-0.5.0.jar,simpleclient_common-0.5.0.jar,simpleclient_dropwizard-0.5.0.jar,simpleclient_pushgateway-0.5.0.jar   \
    --jars /home/dataengine/metrics/spark-metrics-v0.0.1.jar,/home/dataengine/metrics/simpleclient-0.5.0.jar,/home/dataengine/metrics/simpleclient_common-0.5.0.jar,/home/dataengine/metrics/simpleclient_dropwizard-0.5.0.jar,/home/dataengine/metrics/simpleclient_pushgateway-0.5.0.jar  \
    --files /home/dataengine/releases/midengine/master/distribution/conf/log4j.properties,/home/dataengine/releases/midengine/master/distribution/conf/hive_database_table.properties,/home/dataengine/metrics/metrics.properties \
    /home/dataengine/releases/midengine/master/distribution/lib/dataengine-utils-v0.8.0-jar-with-dependencies.jar \
    --tableName rp_device_profile_info_20180904 --zk 'bd15-098,bd15-099,bd15-107'


/usr/bin/spark2-submit --executor-memory 10g    \
 --master yarn  \
 --name TagsChecker[20180904]-61611b07-a46d-416f-b495-06dc0f954fa9  \
 --executor-cores 4 \
 --deploy-mode cluster  \
 --class com.mob.dataengine.utils.tags.TagsChecker  \
 --driver-memory 4g \
 --conf "spark.yarn.appMasterEnv.JAVA_HOME=/opt/jdk1.8.0_45"    \
 --conf "spark.driver.cores=2"  \
 --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC"    \
 --conf "spark.dynamicAllocation.minExecutors=1"    \
 --conf "spark.dynamicAllocation.initialExecutors=1"    \
 --conf "spark.speculation.quantile=0.98"   \
 --conf "spark.dynamicAllocation.enabled=true"  \
 --conf "spark.dynamicAllocation.maxExecutors=60"   \
 --conf "spark.speculation=true"    \
 --conf "spark.executorEnv.JAVA_HOME=/opt/jdk1.8.0_45"  \
 --conf "spark.shuffle.service.enabled=true" \
 --conf "spark.executor.extraClassPath=spark-metrics-v0.0.1-jar-with-dependencies.jar"   \
 --conf "spark.driver.extraClassPath=spark-metrics-v0.0.1-jar-with-dependencies.jar"   \
 --conf spark.metrics.conf=metrics.properties  \
 --files /home/dataengine/releases/midengine/master/distribution/conf/log4j.properties,/home/dataengine/releases/midengine/master/distribution/conf/hive_database_table.properties,/home/dataengine/metrics/metrics.properties \
 --jars /home/dataengine/metrics/spark-metrics-v0.0.1-jar-with-dependencies.jar  \
 /home/dataengine/releases/midengine/master/distribution/lib/dataengine-utils-v0.8.0-jar-with-dependencies.jar \
 --tableName rp_device_profile_info_20180904 --zk 'bd15-161-218,bd15-161-220,bd15-161-219' 




## orc bug

spark2-submit --class com.mob.dataengine.lookalike.LookalikeDistanceCalculation \
    --master yarn --deploy-mode cluster \
    --queue dmpots --driver-memory 5G \
    --executor-memory 8G --executor-cores 6 \
    --conf spark.dynamicAllocation.minExecutors=20  \
    --conf spark.shuffle.service.enabled=true \
    --conf spark.speculation=true \
    --conf spark.speculation.quantile=0.9 \
    --conf spark.driver.userClassPathFirst=true \
    --conf spark.executor.userClassPathFirst=true \
    --conf spark.app.name=ecology_lookalike_normal-f95cbe240e4b4a3b881ec51d5529a955_0 \
    --conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=35 -XX:ConcGCThreads=12" \
    --jars /data/walle/releases/midengine/dataengine-assembly-0.5-SNAPSHOT-dist/lib/scopt_2.11-3.3.0.jar,/data/walle/releases/midengine/dataengine-assembly-0.5-SNAPSHOT-dist/lib/mailSender.jar,/data/walle/releases/midengine/dataengine-assembly-0.5-SNAPSHOT-dist/lib/udf-manager-0.0.1-dependencies.jar,/data/walle/releases/midengine/dataengine-assembly-0.5-SNAPSHOT-dist/lib/spark-hive_2.11-2.1.0.cloudera1.jar  \
    /data/walle/releases/midengine/dataengine-assembly-0.5-SNAPSHOT-dist/lib/lookalike-0.5-SNAPSHOT.jar \
    --data_url "http://10.5.1.45:20101/fs/download?path=tmp/lmz_lookalike_original_set.txt&module=dataengine" \
    --encrypt_type 0 \
    --device_type 4 \
    --o2o_id "f95cbe240e4b4a3b881ec51d5529a955" \
    --day "20180609" \
    --pac_tf_idf_day "20180420" \
    --ml_model “naiveByes"


/usr/bin/spark2-submit --executor-memory 8g    \
    --master yarn   \
    --executor-cores 4  \
    --name test-orc-bug   \
    --deploy-mode cluster   \
    --driver-memory 15g  \
    --class org.apache.spark.sql.hive.orc.OrcBugTest    \
    --conf "spark.speculation.quantile=0.98"    \
    --conf "spark.executorEnv.JAVA_HOME=/opt/jdk1.8.0_45"   \
    --conf "spark.yarn.appMasterEnv.JAVA_HOME=/opt/jdk1.8.0_45" \
    --conf spark.driver.userClassPathFirst=true   \
    --conf spark.executor.userClassPathFirst=true   \
    --conf "spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=25005 -Dcom.sun.management.jmxremote.port=27012 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -verbose:gc "   \
    --files /home/dataengine/releases/midengine/master/distribution/conf/log4j.properties \
    --jars /home/dataengine/releases/midengine/dev-metrics/spark-hive_2.11-2.1.0.cloudera1.jar  \
    /home/dataengine/releases/midengine/dev-metrics/dataengine-commons-v0.8.0.jar



spark2-shell --driver-memory 4g --executor-memory 12g --executor-cores 4 --conf spark.speculation=true     \
--conf spark.speculation.quantile=0.95     \

 spark-shell  --jars mysql-connector-java-5.1.29.jar --driver-library-path mysql-connector-java-5.1.29.jar


log4j
java code生成
log4j.logger.org.apache.spark.sql.catalyst.expressions.codegen=info


