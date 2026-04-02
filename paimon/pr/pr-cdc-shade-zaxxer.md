# com.zaxxer.hikari need to be shade like `flink-sql-connector-mysql-cdc`
let paimon-flink.jar be the first in classpath, which can reproduce this bug:
```text
2026-02-09 10:11:15,260 INFO  org.apache.flink.runtime.jobmaster.JobMaster                 [] - Starting execution of job 'MySQL-Paimon Table Sync: ods.orders' (023a66d968b14df7c78b96a4a2fc51d6) under job master id 00000000000000000000000000000000.
2026-02-09 10:11:15,264 INFO  org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Starting split enumerator for source Source: MySQL Source.
2026-02-09 10:11:15,371 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
java.lang.AbstractMethodError: Receiver class org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionPools does not define or inherit an implementation of the resolved method 'abstract org.apache.flink.cdc.connectors.shaded.com.zaxxer.hikari.HikariDataSource getOrCreateConnectionPool(org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPoolId, org.apache.flink.cdc.connectors.mysql.source.config.MySqlSourceConfig)' of interface org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPools.
	at org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionFactory.connect(JdbcConnectionFactory.java:55) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:888) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:883) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connect(JdbcConnection.java:411) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.openJdbcConnection(DebeziumUtils.java:74) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.createJdbcConnection(MySqlValidator.java:87) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:71) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:229) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.applyCall(RecreateOnResetOperatorCoordinator.java:343) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.start(RecreateOnResetOperatorCoordinator.java:72) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.operators.coordination.OperatorCoordinatorHolder.start(OperatorCoordinatorHolder.java:204) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:173) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startAllOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:85) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.scheduler.SchedulerBase.startScheduling(SchedulerBase.java:635) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.jobmaster.JobMaster.startScheduling(JobMaster.java:1235) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.jobmaster.JobMaster.startJobExecution(JobMaster.java:1152) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.jobmaster.JobMaster.onStart(JobMaster.java:460) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.rpc.RpcEndpoint.internalCallOnStart(RpcEndpoint.java:214) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor$StoppedState.lambda$start$0(PekkoRpcActor.java:627) ~[flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.flink.runtime.concurrent.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68) ~[flink-dist-1.20.3.jar:1.20.3]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor$StoppedState.start(PekkoRpcActor.java:626) ~[flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleControlMessage(PekkoRpcActor.java:197) ~[flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:33) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:29) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:127) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:126) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:29) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:175) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.actor.Actor.aroundReceive(Actor.scala:547) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.actor.Actor.aroundReceive$(Actor.scala:545) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.actor.AbstractActor.aroundReceive(AbstractActor.scala:229) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.actor.ActorCell.receiveMessage(ActorCell.scala:590) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.actor.ActorCell.invoke(ActorCell.scala:557) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.dispatch.Mailbox.processMailbox(Mailbox.scala:272) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.dispatch.Mailbox.run(Mailbox.scala:233) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at org.apache.pekko.dispatch.Mailbox.exec(Mailbox.scala:245) [flink-rpc-akka92f4d17c-3fab-4d5a-ac84-c80f9ab34bcf.jar:1.20.3]
	at java.base/java.util.concurrent.ForkJoinTask.doExec(Unknown Source) [?:?]
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(Unknown Source) [?:?]
	at java.base/java.util.concurrent.ForkJoinPool.scan(Unknown Source) [?:?]
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(Unknown Source) [?:?]
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(Unknown Source) [?:?]
```

```shell
docker build -t my-flink:1.20.3-scala_2.12-java17-paimon-fix1 -f Dockerfile_flink .
-- rename paimon-flink-1.20-1.4-SNAPSHOT.jar to a-paimon-flink-1.20-1.4-SNAPSHOT.jar, let it be the first in classpath 

export cluster_id=flink2
export cluster_port=8083
bin/kubernetes-session.sh \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=$cluster_port \
    -Drest.bind-port=$cluster_port \
    -Dclassloader.resolve-order=child-first \
    -Dkubernetes.container.image=my-flink:1.20.3-scala_2.12-java17-paimon-fix1 \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    -Dcontainerized.master.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dcontainerized.taskmanager.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
    -Dfs.s3a.endpoint=http://rustfs-svc:9000 \
    -Dfs.s3a.path.style.access=true \
    -Dfs.s3a.connection.ssl.enabled=false \
    -Dfs.s3a.access.key=test \
    -Dfs.s3a.secret.key=11111111 \
    -Dstate.checkpoints.dir=s3a://flink-bucket/$cluster_id/checkpoints \
    -Dstate.savepoints.dir=s3a://flink-bucket/$cluster_id/savepoints

bin/flink run \
    -Drest.address=flink2-rest \
    -Drest.port=8083 \
    -Dexecution.checkpointing.interval=10s \
    -Dexecution.checkpointing.mode=EXACTLY_ONCE \
    lib/paimon-flink-action-*.jar \
    mysql_sync_table \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table orders \
    --primary_keys id \
    --mysql_conf hostname=mysql \
    --mysql_conf username=root \
    --mysql_conf port=3307 \
    --mysql_conf password=root123 \
    --mysql_conf database-name='test' \
    --mysql_conf table-name='orders' \
    --table_conf bucket=1 \
    --table_conf merge-engine=deduplicate \
    --table_conf changelog-producer=input
```

https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/deployment/resource-providers/native_kubernetes/
```shell
k run mysql-client   --image=docker.io/bitnamilegacy/mysql:8.3.0-debian-12-r3   --rm -it   --restart=Never -- mysql -h mysql -u root -proot123 -P3307

export cluster_id=flink-mysql-sync-table
bin/flink run-application \
    --target kubernetes-application \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=8084 \
    -Drest.bind-port=8084 \
    -Dkubernetes.container.image=my-flink:1.20.1-scala_2.12-java17-paimon \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    -Dkubernetes.service-account=flink-service-account \
    -Dexecution.checkpointing.interval=10s \
    -Dexecution.checkpointing.mode=EXACTLY_ONCE \
    -Dstate.checkpoints.dir=s3a://flink-bucket/$cluster_id/checkpoints \
    -Dstate.savepoints.dir=s3a://flink-bucket/$cluster_id/savepoints \
    -Dfs.s3a.endpoint=http://rustfs-svc:9000 \
    -Dfs.s3a.path.style.access=true \
    -Dfs.s3a.connection.ssl.enabled=false \
    -Dfs.s3a.access.key=test \
    -Dfs.s3a.secret.key=11111111 \
    lib/paimon-flink-action.jar \
    mysql_sync_table \
    --warehouse s3a://warehouse/paimon \
    --database ods \
    --table orders \
    --primary_keys id \
    --mysql_conf hostname=mysql \
    --mysql_conf port=3307 \
    --mysql_conf username=root \
    --mysql_conf password=root123 \
    --mysql_conf database-name=test \
    --mysql_conf table-name=orders \
    --table_conf bucket=1 \
    --table_conf merge-engine=deduplicate \
    --table_conf changelog-producer=input
```


IntelliJ local test:
```log
Connected to the target VM, address: '127.0.0.1:57722', transport: 'socket'
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/Users/juntao/.m2/repository/org/slf4j/slf4j-reload4j/1.7.36/slf4j-reload4j-1.7.36.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/juntao/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.20.0/log4j-slf4j-impl-2.20.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Reload4jLoggerFactory]
09:31:04,621 INFO  org.apache.paimon.flink.action.ActionFactory                 [] - mysql_sync_table job args: --warehouse s3a://warehouse/paimon --database ods --table orders --primary_keys id --mysql_conf hostname=localhost --mysql_conf username=root --mysql_conf port=3307 --mysql_conf password=root123 --mysql_conf database-name=test --mysql_conf table-name=orders --mysql_conf server-time-zone=UTC --table_conf bucket=1 --table_conf merge-engine=deduplicate --table_conf changelog-producer=input
09:31:04,695 WARN  org.apache.paimon.utils.HadoopUtils                          [] - Could not find Hadoop configuration via any of the supported methods
09:31:05,190 WARN  org.apache.hadoop.metrics2.impl.MetricsConfig                [] - Cannot locate configuration: tried hadoop-metrics2-s3a-file-system.properties,hadoop-metrics2.properties
09:31:05,320 WARN  org.apache.hadoop.util.NativeCodeLoader                      [] - Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
09:31:06,117 INFO  com.amazonaws.http.AmazonHttpClient                          [] - Configuring Proxy. Proxy Host: 127.0.0.1 Proxy Port: 7897
09:31:06,689 INFO  org.apache.paimon.flink.FlinkCatalog                         [] - Creating Flink catalog: metastore=filesystem
09:31:08,590 INFO  org.apache.paimon.flink.action.cdc.mysql.MySqlActionUtils    [] - Connect to MySQL server using url: jdbc:mysql://localhost:3307
09:31:09,241 WARN  org.apache.paimon.utils.HadoopUtils                          [] - Could not find Hadoop configuration via any of the supported methods
09:31:10,390 INFO  org.apache.pekko.event.slf4j.Slf4jLogger                     [] - Slf4jLogger started
09:31:10,522 INFO  org.apache.pekko.event.slf4j.Slf4jLogger                     [] - Slf4jLogger started
09:31:10,564 WARN  org.apache.flink.runtime.util.HadoopUtils                    [] - Could not find Hadoop configuration via any of the supported methods (Flink configuration, environment variables).
09:31:10,566 WARN  org.apache.flink.runtime.util.HadoopUtils                    [] - Could not find Hadoop configuration via any of the supported methods (Flink configuration, environment variables).
09:31:10,600 WARN  org.apache.flink.runtime.security.token.DefaultDelegationTokenManager [] - No tokens obtained so skipping notifications
09:31:11,128 WARN  org.apache.flink.runtime.webmonitor.WebMonitorUtils          [] - Log file environment variable 'log.file' is not set.
09:31:11,128 WARN  org.apache.flink.runtime.webmonitor.WebMonitorUtils          [] - JobManager log files are unavailable in the web dashboard. Log file location not found in environment variable 'log.file' or configuration key 'web.log.path'.
09:31:11,359 WARN  org.apache.flink.runtime.security.token.DefaultDelegationTokenManager [] - No tokens obtained so skipping notifications
09:31:11,359 WARN  org.apache.flink.runtime.security.token.DefaultDelegationTokenManager [] - Tokens update task not started because either no tokens obtained or none of the tokens specified its renewal date
09:31:11,623 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
java.lang.AbstractMethodError: Receiver class org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionPools does not define or inherit an implementation of the resolved method 'abstract org.apache.flink.cdc.connectors.shaded.com.zaxxer.hikari.HikariDataSource getOrCreateConnectionPool(org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPoolId, org.apache.flink.cdc.connectors.mysql.source.config.MySqlSourceConfig)' of interface org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPools.
	at org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionFactory.connect(JdbcConnectionFactory.java:55) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:888) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:883) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connect(JdbcConnection.java:411) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.openJdbcConnection(DebeziumUtils.java:74) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.createJdbcConnection(MySqlValidator.java:87) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:71) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:232) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.applyCall(RecreateOnResetOperatorCoordinator.java:343) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.start(RecreateOnResetOperatorCoordinator.java:72) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.OperatorCoordinatorHolder.start(OperatorCoordinatorHolder.java:204) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:173) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultOperatorCoordinatorHandler.startAllOperatorCoordinators(DefaultOperatorCoordinatorHandler.java:85) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.SchedulerBase.startScheduling(SchedulerBase.java:635) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.jobmaster.JobMaster.startScheduling(JobMaster.java:1220) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.jobmaster.JobMaster.startJobExecution(JobMaster.java:1137) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.jobmaster.JobMaster.onStart(JobMaster.java:460) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.rpc.RpcEndpoint.internalCallOnStart(RpcEndpoint.java:214) ~[flink-rpc-core-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor$StoppedState.lambda$start$0(PekkoRpcActor.java:627) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.concurrent.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68) ~[flink-rpc-core-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor$StoppedState.start(PekkoRpcActor.java:626) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleControlMessage(PekkoRpcActor.java:197) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:33) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:127) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:126) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:175) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive(Actor.scala:547) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive$(Actor.scala:545) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.AbstractActor.aroundReceive(AbstractActor.scala:229) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.receiveMessage(ActorCell.scala:590) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.invoke(ActorCell.scala:557) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.processMailbox(Mailbox.scala:272) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.run(Mailbox.scala:233) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.exec(Mailbox.scala:245) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at java.util.concurrent.ForkJoinTask.doExec$$$capture(ForkJoinTask.java:387) [?:?]
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java) [?:?]
	at java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312) [?:?]
	at java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843) [?:?]
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808) [?:?]
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188) [?:?]
09:31:12,734 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
java.lang.AbstractMethodError: Receiver class org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionPools does not define or inherit an implementation of the resolved method 'abstract org.apache.flink.cdc.connectors.shaded.com.zaxxer.hikari.HikariDataSource getOrCreateConnectionPool(org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPoolId, org.apache.flink.cdc.connectors.mysql.source.config.MySqlSourceConfig)' of interface org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPools.
	at org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionFactory.connect(JdbcConnectionFactory.java:55) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:888) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:883) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connect(JdbcConnection.java:411) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.openJdbcConnection(DebeziumUtils.java:74) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.createJdbcConnection(MySqlValidator.java:87) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:71) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:232) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.resetAndStart(RecreateOnResetOperatorCoordinator.java:433) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.lambda$resetToCheckpoint$7(RecreateOnResetOperatorCoordinator.java:157) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:863) ~[?:?]
	at java.util.concurrent.CompletableFuture.uniWhenCompleteStage(CompletableFuture.java:887) ~[?:?]
	at java.util.concurrent.CompletableFuture.whenComplete(CompletableFuture.java:2357) ~[?:?]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.resetToCheckpoint(RecreateOnResetOperatorCoordinator.java:144) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.OperatorCoordinatorHolder.resetToCheckpoint(OperatorCoordinatorHolder.java:303) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreStateToCoordinators(CheckpointCoordinator.java:2140) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreLatestCheckpointedStateInternal(CheckpointCoordinator.java:1799) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreLatestCheckpointedStateToAll(CheckpointCoordinator.java:1725) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.SchedulerBase.restoreState(SchedulerBase.java:445) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultScheduler.restartTasks(DefaultScheduler.java:411) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultScheduler.lambda$null$2(DefaultScheduler.java:372) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.util.concurrent.CompletableFuture$UniRun.tryFire$$$capture(CompletableFuture.java:787) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniRun.tryFire(CompletableFuture.java) ~[?:?]
	at java.util.concurrent.CompletableFuture$Completion.run(CompletableFuture.java:482) ~[?:?]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.lambda$handleRunAsync$4(PekkoRpcActor.java:460) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.concurrent.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68) ~[flink-rpc-core-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleRunAsync(PekkoRpcActor.java:460) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleRpcMessage(PekkoRpcActor.java:225) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.FencedPekkoRpcActor.handleRpcMessage(FencedPekkoRpcActor.java:88) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleMessage(PekkoRpcActor.java:174) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:33) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:127) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:126) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:175) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive(Actor.scala:547) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive$(Actor.scala:545) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.AbstractActor.aroundReceive(AbstractActor.scala:229) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.receiveMessage(ActorCell.scala:590) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.invoke(ActorCell.scala:557) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.processMailbox(Mailbox.scala:272) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.run(Mailbox.scala:233) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.exec(Mailbox.scala:245) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at java.util.concurrent.ForkJoinTask.doExec$$$capture(ForkJoinTask.java:387) [?:?]
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java) [?:?]
	at java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312) [?:?]
	at java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843) [?:?]
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808) [?:?]
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188) [?:?]
09:31:14,207 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
java.lang.AbstractMethodError: Receiver class org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionPools does not define or inherit an implementation of the resolved method 'abstract org.apache.flink.cdc.connectors.shaded.com.zaxxer.hikari.HikariDataSource getOrCreateConnectionPool(org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPoolId, org.apache.flink.cdc.connectors.mysql.source.config.MySqlSourceConfig)' of interface org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPools.
	at org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionFactory.connect(JdbcConnectionFactory.java:55) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:888) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:883) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connect(JdbcConnection.java:411) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.openJdbcConnection(DebeziumUtils.java:74) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.createJdbcConnection(MySqlValidator.java:87) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:71) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:232) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.resetAndStart(RecreateOnResetOperatorCoordinator.java:433) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.lambda$resetToCheckpoint$7(RecreateOnResetOperatorCoordinator.java:157) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:863) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire$$$capture(CompletableFuture.java:841) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java) ~[?:?]
	at java.util.concurrent.CompletableFuture.unipush(CompletableFuture.java:593) ~[?:?]
	at java.util.concurrent.CompletableFuture.uniWhenCompleteStage(CompletableFuture.java:885) ~[?:?]
	at java.util.concurrent.CompletableFuture.whenComplete(CompletableFuture.java:2357) ~[?:?]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.resetToCheckpoint(RecreateOnResetOperatorCoordinator.java:144) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.OperatorCoordinatorHolder.resetToCheckpoint(OperatorCoordinatorHolder.java:303) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreStateToCoordinators(CheckpointCoordinator.java:2140) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreLatestCheckpointedStateInternal(CheckpointCoordinator.java:1799) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreLatestCheckpointedStateToAll(CheckpointCoordinator.java:1725) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.SchedulerBase.restoreState(SchedulerBase.java:445) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultScheduler.restartTasks(DefaultScheduler.java:411) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.scheduler.DefaultScheduler.lambda$null$2(DefaultScheduler.java:372) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.util.concurrent.CompletableFuture$UniRun.tryFire$$$capture(CompletableFuture.java:787) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniRun.tryFire(CompletableFuture.java) ~[?:?]
	at java.util.concurrent.CompletableFuture$Completion.run(CompletableFuture.java:482) ~[?:?]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.lambda$handleRunAsync$4(PekkoRpcActor.java:460) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.concurrent.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68) ~[flink-rpc-core-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleRunAsync(PekkoRpcActor.java:460) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleRpcMessage(PekkoRpcActor.java:225) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.FencedPekkoRpcActor.handleRpcMessage(FencedPekkoRpcActor.java:88) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.flink.runtime.rpc.pekko.PekkoRpcActor.handleMessage(PekkoRpcActor.java:174) ~[flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:33) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:127) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:126) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:29) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:175) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:176) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive(Actor.scala:547) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.Actor.aroundReceive$(Actor.scala:545) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.AbstractActor.aroundReceive(AbstractActor.scala:229) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.receiveMessage(ActorCell.scala:590) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.actor.ActorCell.invoke(ActorCell.scala:557) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.processMailbox(Mailbox.scala:272) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.run(Mailbox.scala:233) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at org.apache.pekko.dispatch.Mailbox.exec(Mailbox.scala:245) [flink-rpc-akka74df2065-f887-4f3a-84e0-0cd5bc0897ee.jar:1.20.1]
	at java.util.concurrent.ForkJoinTask.doExec$$$capture(ForkJoinTask.java:387) [?:?]
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java) [?:?]
	at java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312) [?:?]
	at java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843) [?:?]
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808) [?:?]
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188) [?:?]
09:31:15,953 ERROR org.apache.flink.runtime.source.coordinator.SourceCoordinator [] - Failed to create Source Enumerator for source Source: MySQL Source
java.lang.AbstractMethodError: Receiver class org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionPools does not define or inherit an implementation of the resolved method 'abstract org.apache.flink.cdc.connectors.shaded.com.zaxxer.hikari.HikariDataSource getOrCreateConnectionPool(org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPoolId, org.apache.flink.cdc.connectors.mysql.source.config.MySqlSourceConfig)' of interface org.apache.flink.cdc.connectors.mysql.source.connection.ConnectionPools.
	at org.apache.flink.cdc.connectors.mysql.source.connection.JdbcConnectionFactory.connect(JdbcConnectionFactory.java:55) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:888) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connection(JdbcConnection.java:883) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at io.debezium.jdbc.JdbcConnection.connect(JdbcConnection.java:411) ~[flink-sql-connector-postgres-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.debezium.DebeziumUtils.openJdbcConnection(DebeziumUtils.java:74) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.createJdbcConnection(MySqlValidator.java:87) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.MySqlValidator.validate(MySqlValidator.java:71) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.cdc.connectors.mysql.source.MySqlSource.createEnumerator(MySqlSource.java:200) ~[flink-sql-connector-mysql-cdc-3.5.0.jar:3.5.0]
	at org.apache.flink.runtime.source.coordinator.SourceCoordinator.start(SourceCoordinator.java:232) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator$DeferrableCoordinator.resetAndStart(RecreateOnResetOperatorCoordinator.java:433) ~[flink-runtime-1.20.1.jar:1.20.1]
	at org.apache.flink.runtime.operators.coordination.RecreateOnResetOperatorCoordinator.lambda$resetToCheckpoint$7(RecreateOnResetOperatorCoordinator.java:157) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:863) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire$$$capture(CompletableFuture.java:841) ~[?:?]
	at java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java) ~[?:?]
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:510) ~[?:?]
	at java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2179) ~[?:?]
	at org.apache.flink.runtime.operators.coordination.ComponentClosingUtils.lambda$closeAsyncWithTimeout$0(ComponentClosingUtils.java:77) ~[flink-runtime-1.20.1.jar:1.20.1]
	at java.lang.Thread.run(Thread.java:1583) [?:?]
Disconnected from the target VM, address: '127.0.0.1:57722', transport: 'socket'

Process finished with exit code 137 (interrupted by signal 9:SIGKILL)
```


## Troubleshooting
<details>
<summary>Caused by: java.lang.IllegalStateException: Delegation token provider with service name s3-hadoop has multiple implementations</summary>

```text
主要原因是下面配置不能同时存在：

-Dcontainerized.master.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \
-Dcontainerized.taskmanager.env.ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.17.2.jar \

RUN  mkdir /opt/flink/plugins/s3-fs-hadoop
COPY ./flink-s3-fs-hadoop-1.20.1.jar /opt/flink/plugins/s3-fs-hadoop/
```
</details>


<details>
<summary>flink-s3-fs-hadoop-1.17.2.jar 有 org.apache.hadoop.conf.Configuration 为什么会报 org.apache.hadoop.conf.Configuration 找不到？</summary>

```
插件类加载器的优先级：能访问 Flink 核心类，但核心类加载器无法访问插件内的类（单向可见）: 所以paimon action 中用到了hadoop 相关client的类报错，需要增加：
hadoop-client-api-3.3.2.jar、avro-1.11.3.jar、flink-s3-fs-hadoop-1.20.1.jar
```
</details>

