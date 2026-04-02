# RustFS
## 安装
```shell
# git clone https://github.com/rustfs/rustfs/blob/main/helm/README.md
```

`helm install rustfs --create-namespace ./`

## RustFS 存储桶创建
https://docs.rustfs.com.cn/management/bucket/creation.html

## RustFS admin
http://localhost:32001/rustfs/console/access-keys


## MinIO Client
```shell
curl --progress-bar -L https://dl.min.io/aistor/mc/release/darwin-amd64/mc \
    --create-dirs \
    -o /usr/local/bin/mc

mc alias set rustfs http://localhost:32000 test 11111111
mc ls rustfs
```

```shell
kubectl run mc --image=busybox:latest --command -- sleep 3600
kubectl exec -it mc -- bash
curl --progress-bar -L https://dl.min.io/aistor/mc/release/linux-amd64/mc \
    --create-dirs \
    -o /usr/bin/mc
chmod +x /usr/bin/mc
mc alias set rustfs http://rustfs-svc:9000 test 11111111
mc ls rustfs
```

## Java Client
[S3RustFSExample.java](src/main/java/cn/juntaozhang/s3/S3RustFSExample.java)

