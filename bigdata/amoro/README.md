## install Amoro
https://amoro.apache.org/docs/latest/deployment-on-kubernetes/

charts/amoro/Chart.yaml
```text
appVersion: "master-snapshot"
```

charts/amoro/values.yaml
```text
  database:
    type: derby
    driver: org.apache.derby.jdbc.EmbeddedDriver
    url: jdbc:derby:/tmp/amoro/derby;create=true
#    type: postgres # postgres or mysql
#    jdbc-driver-class: org.postgresql.Driver
#    url: jdbc:postgresql://postgresql:5432/amoro
#    username: postgres
#    password: postgres123
#    auto-create-tables: true

tag: master-snapshot-flink1.20

tag: master-snapshot-spark3.5
```

## paimon format conf

| 配置项 | 值 |
|--------|-----|
| metastore| filesystem |
| s3.endpoint | http://localhost:32000 |
| s3.path.style.access | true |
| warehouse | s3://warehouse/paimon |
| s3.access-key | test |
| s3.secret-key | 11111111 |



# Troubleshooting

<details>
<summary>Could not find a valid Docker environment. Please see logs and check configuration</summary>

- Upgrade testcontainers to 1.21.4
- [testcontainers not working for Docker Engine v29](https://forums.docker.com/t/testcontainer-stopped-working-after-updating-docker-desktop-to-v4-56-0/150823)

```log
docker version:4.57.0 
Engine:29.1.3
testcontainers:1.17.2

SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/Users/juntao/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.20.0/log4j-slf4j-impl-2.20.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/juntao/.m2/repository/org/slf4j/slf4j-log4j12/1.7.5/slf4j-log4j12-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
19:00:58.571 ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy: Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
19:00:58.577 ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy:     UnixSocketClientProviderStrategy: failed with exception BadRequestException (Status 400: {"ID":"","Containers":0,"ContainersRunning":0,"ContainersPaused":0,"ContainersStopped":0,"Images":0,"Driver":"","DriverStatus":null,"Plugins":{"Volume":null,"Network":null,"Authorization":null,"Log":null},"MemoryLimit":false,"SwapLimit":false,"CpuCfsPeriod":false,"CpuCfsQuota":false,"CPUShares":false,"CPUSet":false,"PidsLimit":false,"IPv4Forwarding":false,"Debug":false,"NFd":0,"OomKillDisable":false,"NGoroutines":0,"SystemTime":"","LoggingDriver":"","CgroupDriver":"","NEventsListener":0,"KernelVersion":"","OperatingSystem":"","OSVersion":"","OSType":"","Architecture":"","IndexServerAddress":"","RegistryConfig":null,"NCPU":0,"MemTotal":0,"GenericResources":null,"DockerRootDir":"","HttpProxy":"","HttpsProxy":"","NoProxy":"","Name":"","Labels":["com.docker.desktop.address=unix:///Users/juntao/Library/Containers/com.docker.docker/Data/docker-cli.sock"],"ExperimentalBuild":false,"ServerVersion":"","Runtimes":null,"DefaultRuntime":"","Swarm":{"NodeID":"","NodeAddr":"","LocalNodeState":"","ControlAvailable":false,"Error":"","RemoteManagers":null},"LiveRestoreEnabled":false,"Isolation":"","InitBinary":"","ContainerdCommit":{"ID":""},"RuncCommit":{"ID":""},"InitCommit":{"ID":""},"SecurityOptions":null,"CDISpecDirs":null,"Warnings":null})
19:00:58.578 ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy: As no valid configuration was found, execution cannot continue

Test ignored.

java.lang.IllegalStateException: Could not find a valid Docker environment. Please see logs and check configuration

	at org.testcontainers.dockerclient.DockerClientProviderStrategy.lambda$getFirstValidStrategy$6(DockerClientProviderStrategy.java:242)
	at java.base/java.util.Optional.orElseThrow(Optional.java:408)
	at org.testcontainers.dockerclient.DockerClientProviderStrategy.getFirstValidStrategy(DockerClientProviderStrategy.java:234)
	at org.testcontainers.DockerClientFactory.getOrInitializeStrategy(DockerClientFactory.java:135)
	at org.testcontainers.DockerClientFactory.client(DockerClientFactory.java:176)
	at org.testcontainers.DockerClientFactory$1.getDockerClient(DockerClientFactory.java:90)
	at com.github.dockerjava.api.DockerClientDelegate.authConfig(DockerClientDelegate.java:108)
	at org.testcontainers.containers.GenericContainer.start(GenericContainer.java:325)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.start(TestcontainersExtension.java:242)
	at org.testcontainers.junit.jupiter.TestcontainersExtension$StoreAdapter.access$200(TestcontainersExtension.java:229)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$null$1(TestcontainersExtension.java:59)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore.lambda$getOrComputeIfAbsent$4(ExtensionValuesStore.java:86)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore$MemoizingSupplier.computeValue(ExtensionValuesStore.java:223)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore$MemoizingSupplier.get(ExtensionValuesStore.java:211)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore$StoredValue.evaluate(ExtensionValuesStore.java:191)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore$StoredValue.access$100(ExtensionValuesStore.java:171)
	at org.junit.jupiter.engine.execution.ExtensionValuesStore.getOrComputeIfAbsent(ExtensionValuesStore.java:89)
	at org.junit.jupiter.engine.execution.NamespaceAwareStore.getOrComputeIfAbsent(NamespaceAwareStore.java:53)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.lambda$beforeAll$2(TestcontainersExtension.java:59)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
	at org.testcontainers.junit.jupiter.TestcontainersExtension.beforeAll(TestcontainersExtension.java:59)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$invokeBeforeAllCallbacks$12(ClassBasedTestDescriptor.java:395)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeBeforeAllCallbacks(ClassBasedTestDescriptor.java:395)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.before(ClassBasedTestDescriptor.java:211)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.before(ClassBasedTestDescriptor.java:84)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:148)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:147)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:127)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:90)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:55)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:102)
	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:54)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:114)
	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:86)
	at org.junit.platform.launcher.core.DefaultLauncherSession$DelegatingLauncher.execute(DefaultLauncherSession.java:86)
	at org.junit.platform.launcher.core.SessionPerRequestLauncher.execute(SessionPerRequestLauncher.java:53)
	at com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:66)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
	at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:237)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:58)


Process finished with exit code 255
```
</details>

<details>
<summary>Paimon S3 access error: missing s3 access and secret key</summary>

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
https://github.com/apache/amoro/issues/4063
</details>

