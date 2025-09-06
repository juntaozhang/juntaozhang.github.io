# MinIO
## build minio in k8s
```shell
helm install minio minio/minio \
--set mode=standalone \
--set rootUser=minio,rootPassword=minio12345 \
--set service.type=LoadBalancer \
--set service.port=9000
```

## build client in local
```shell
curl --progress-bar -L https://dl.min.io/aistor/mc/release/darwin-arm64/mc \
    --create-dirs \
    -o ~/minio-binaries/mc
    
alias mc='~/minio-binaries/mc'

mc alias set localminio http://127.0.0.1:9000 minio minio12345
```


## 在 K8s 集群内部直接用 mc
```shell
k run mc \
    --image=bitnami/minio-client:latest --restart=Never \
    --command -- bash -lc 'sleep infinity'
```
>k exec -it mc -- bash
```
mc alias set minio http://minio.default.svc.cluster.local:9000 minio minio12345
mc ls minio
```

```shell

```
