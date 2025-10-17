
```bash
kubectl apply -f flink-rbac.yaml
```

## Run flink example
```bash
export cluster_id=wordcount
export cluster_port=8083
./bin/flink run-application \
    --target kubernetes-application \
    -Drest.port=$cluster_port \
    -Drest.bind-port=$cluster_port \
    -Dkubernetes.cluster-id=$cluster_id \
    -Dkubernetes.container.image=flink:1.17.2-scala_2.12 \
    -Dkubernetes.service-account=flink-service-account \
    local:///opt/flink/examples/streaming/WordCount.jar
```