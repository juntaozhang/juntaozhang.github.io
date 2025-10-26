# Kubernetes etcd 部署和测试

本目录包含了在 Kubernetes 环境中部署和测试 etcd 的所有配置文件和脚本。

## 📁 文件结构

```
k8s/
├── README.md                # 本文档
├── etcd-deployment.yaml     # etcd 主要部署文件
├── test-etcd.yaml          # 基本功能测试
├── etcd-watch-test.yaml    # Watch 功能测试
├── etcd-benchmark.yaml     # 性能基准测试
├── etcd-monitor.yaml       # 监控部署
└── run-tests.sh           # 测试运行脚本
```

## 🚀 快速开始

### 1. 部署 etcd

```bash
# 部署 etcd 服务
chmod +x run-deploy.sh
./run-deploy.sh

# 检查部署状态
kubectl get pods -l app=etcd
kubectl get services -l app=etcd
```


build client
```bash
wget https://storage.googleapis.com/etcd/v3.6.0/etcd-v3.6.0-linux-amd64.tar.gz
tar -xvf etcd-v3.6.0-linux-amd64.tar.gz
docker build -t etcd-client -f dockerfile-client ./
kubectl apply -f etcd-test-client.yaml
```


### 2. 运行测试

```bash
chmod +x run-tests.sh

# 运行所有测试
./run-tests.sh all

# 或运行特定测试
./run-tests.sh basic     # 基本功能测试
./run-tests.sh watch     # Watch 功能测试
./run-tests.sh benchmark # 性能测试
```

## 📋 部署文件详解

### etcd-deployment.yaml

主要的 etcd 部署文件，包含：

- **Deployment**: etcd 单实例部署
  - 镜像: `quay.io/coreos/etcd:v3.5.10`
  - 端口: 2379 (客户端), 2380 (对等节点)
  - 持久化: EmptyDir 卷
  - 健康检查: 就绪性和存活性探针

- **Service (ClusterIP)**: 集群内部访问
  - 名称: `etcd-service`
  - 端口: 2379, 2380

- **Service (NodePort)**: 外部访问
  - 名称: `etcd-nodeport`
  - NodePort: 32379

#### 环境变量配置

| 变量 | 值 | 说明 |
|------|----|----- |
| ETCD_NAME | etcd-server | etcd 实例名称 |
| ETCD_DATA_DIR | /etcd-data | 数据目录 |
| ETCD_LISTEN_CLIENT_URLS | http://0.0.0.0:2379 | 客户端监听地址 |
| ETCD_ADVERTISE_CLIENT_URLS | http://0.0.0.0:2379 | 客户端广播地址 |
| ETCD_INITIAL_CLUSTER_STATE | new | 集群初始状态 |

## 🧪 测试用例

### 1. 基本功能测试 (test-etcd.yaml)

测试内容：
- ✅ 健康检查
- ✅ 基本键值操作 (PUT/GET)
- ✅ 前缀操作
- ✅ 事务操作
- ✅ 租约机制

运行方式：
```bash
./run-tests.sh basic
```

### 2. Watch 功能测试 (etcd-watch-test.yaml)

测试场景：
- 🔄 实时监听键值变化
- 📝 模拟配置更新
- 🗑️ 键删除监听
- 📊 批量操作监听

特性：
- 双容器设计：生产者和消费者
- 生产者负责写入数据
- 消费者负责监听变化

运行方式：
```bash
./run-tests.sh watch
```

### 3. 性能基准测试 (etcd-benchmark.yaml)

测试指标：
- ⚡ 写入性能 (1000 次 PUT 操作)
- 📖 读取性能 (1000 次 GET 操作)
- 🔍 前缀查询性能
- 🗑️ 批量删除性能

运行方式：
```bash
./run-tests.sh benchmark
```

示例输出：
```
=== etcd 性能基准测试 ===
0
1. 写入性能测试 (1000 次写入):
写入 1000 个键耗时: 140s
2. 读取性能测试 (1000 次读取):
读取 1000 个键耗时: 148s
3. 前缀查询性能测试:
前缀查询 2000 个键耗时: 1s
4. 批量删除性能测试:
批量删除耗时: 0s
=== 性能测试总结 ===
写入速度: 7 ops/s
读取速度: 6 ops/s
前缀查询: 1s for 2000 keys
批量删除: 0s
```

## 📊 监控和调试

### etcd 监控部署 (etcd-monitor.yaml)

功能：
- 🏥 持续健康检查
- 📈 集群状态监控
- 👥 成员信息显示
- 📊 性能指标收集
- 🔑 键值统计

启动监控：
```bash
./run-tests.sh monitor

# 查看监控日志
kubectl logs -f deployment/etcd-monitor
```

停止监控：
```bash
./run-tests.sh stop-monitor
```

### 手动调试命令

```bash
# 进入 etcd 容器
kubectl exec -it $(kubectl get pod -l app=etcd -o jsonpath='{.items[0].metadata.name}') -- sh

# 健康检查
kubectl exec $(kubectl get pod -l app=etcd -o jsonpath='{.items[0].metadata.name}') -- etcdctl endpoint health

# 查看集群状态
kubectl exec $(kubectl get pod -l app=etcd -o jsonpath='{.items[0].metadata.name}') -- etcdctl endpoint status --write-out=table

# 查看所有键
kubectl exec $(kubectl get pod -l app=etcd -o jsonpath='{.items[0].metadata.name}') -- etcdctl get "" --prefix --keys-only
```

## 🔧 运行脚本使用指南

### run-tests.sh 脚本

这是一个综合的测试管理脚本，支持以下操作：

```bash
# 显示帮助
./run-tests.sh help

# 运行所有测试
./run-tests.sh all

# 运行特定测试
./run-tests.sh basic      # 基本功能测试
./run-tests.sh watch      # Watch 功能测试
./run-tests.sh benchmark  # 性能基准测试

# 监控操作
./run-tests.sh monitor        # 启动监控
./run-tests.sh stop-monitor   # 停止监控

# 清理资源
./run-tests.sh cleanup

kubectl delete -f etcd-deployment.yaml --ignore-not-found=true
```
