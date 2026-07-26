#!/bin/bash

set -e

echo "🚀 部署 etcd 到 Kubernetes..."

# 检查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl 未找到，请先安装 kubectl"
    exit 1
fi

# 检查 Kubernetes 集群连接
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ 无法连接到 Kubernetes 集群，请检查 kubeconfig"
    exit 1
fi

echo "✅ Kubernetes 集群连接正常"

# 部署 etcd
echo "📦 部署 etcd..."
kubectl apply -f etcd-deployment.yaml

echo "⏳ 等待 etcd pod 就绪..."
kubectl wait --for=condition=ready pod -l app=etcd --timeout=300s

echo "✅ etcd 部署完成!"

# 显示服务信息
echo ""
echo "📋 服务信息:"
kubectl get pods -l app=etcd
kubectl get services -l app=etcd

# 获取 NodePort
NODEPORT=$(kubectl get service etcd-nodeport -o jsonpath='{.spec.ports[0].nodePort}')
echo ""
echo "🔗 连接信息:"
echo "  - 集群内部: etcd-service:2379"
echo "  - NodePort: localhost:$NODEPORT"
echo "  - 环境变量: export ETCD_ENDPOINTS=localhost:$NODEPORT"

echo ""
echo "🧪 测试连接:"
echo "kubectl port-forward service/etcd-service 2379:2379"