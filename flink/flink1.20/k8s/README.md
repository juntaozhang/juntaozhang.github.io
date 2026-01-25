# Flink on Kubernetes

## Create Kubernetes Role
```bash
cd flink-release-1.20-study/build-target
kubectl apply -f flink-rbac.yaml
```

## Run Flink Simple Application example
```bash
export cluster_id=wordcount
export cluster_port=8083
./bin/flink run-application \
    --target kubernetes-application \
    -Drest.port=$cluster_port \
    -Drest.bind-port=$cluster_port \
    -Dkubernetes.cluster-id=$cluster_id \
    -Dkubernetes.container.image=flink:1.20.3-scala_2.12 \
    -Dkubernetes.service-account=flink-service-account \
    local:///opt/flink/examples/streaming/WordCount.jar
```

## Run Flink Custom Example

### Start socket server

```bash
docker build -t my-nc -f Dockerfile_nc .
kubectl apply -f socket-server.yaml
```

### Build My Flink Job

- cd [flink-examples-streaming](https://github.com/juntaozhang/flink/tree/release-1.20-study/flink-examples/flink-examples-streaming/)
- build [WordCountExample](https://github.com/juntaozhang/flink/tree/release-1.20-study/flink-examples/flink-examples-streaming/src/main/java/org/apache/flink/streaming/examples/my/WordCountExample.java)
  - `mvn package -Dspotless.check.skip=true -Pjava17,java17-target` 
  - `docker build -t my-flink:1.20.3-scala_2.12-java17 -f Dockerfile_flink .` 
  - run job
    ```bash
    export cluster_id=mywordcount
    export cluster_port=8082
    ./bin/flink run-application                                              \
        --target kubernetes-application                                      \
        -Drest.port=$cluster_port                                            \
        -Drest.bind-port=$cluster_port                                       \
        -Dkubernetes.cluster-id=$cluster_id                                  \
        -Dkubernetes.container.image=my-flink:1.20.3-scala_2.12-java17       \
        -Dkubernetes.service-account=flink-service-account                   \
        -Dkubernetes.rest-service.exposed.type=LoadBalancer                  \
        -Dparallelism.default=2                                              \
        -Drestart-strategy=fixed-delay                                       \
        -Drestart-strategy.fixed-delay.attempts=10                           \
        -Drestart-strategy.fixed-delay.delay=10s                             \
        local:///opt/flink/examples/streaming/WordCountExample.jar socket-server 19998
    ```