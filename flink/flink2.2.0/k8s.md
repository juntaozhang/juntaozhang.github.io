
## Run flink example
```bash
export cluster_id=wordcount
export cluster_port=8084
./bin/flink run \
    --target kubernetes-application \
    -Drest.port=$cluster_port \
    -Drest.bind-port=$cluster_port \
    -Dkubernetes.cluster-id=$cluster_id \
        -Dkubernetes.container.image=flink:2.2.0-scala_2.12-java11 \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer \
    local:///opt/flink/examples/streaming/WordCount.jar
```

```bash
export cluster_id=flink2
export cluster_port=8084
./bin/kubernetes-session.sh \
    -Dkubernetes.cluster-id=$cluster_id \
    -Drest.port=8084 \
    -Drest.bind-port=8084 \
    -Dkubernetes.container.image=flink:2.2.0-scala_2.12-java11 \
    -Dkubernetes.service-account=flink-service-account \
    -Dkubernetes.rest-service.exposed.type=LoadBalancer
```