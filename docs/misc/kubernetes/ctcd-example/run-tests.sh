#!/bin/bash

set -e

echo "🧪 etcd 测试套件"
echo "================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 帮助函数
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 检查 etcd 服务是否运行
check_etcd_service() {
    log_info "检查 etcd 服务状态..."

    if kubectl get pods -l app=etcd | grep -q "Running"; then
        log_success "etcd 服务正在运行"
        return 0
    else
        log_error "etcd 服务未运行，请先执行: kubectl apply -f etcd-deployment.yaml"
        return 1
    fi
}

# 运行基本测试
run_basic_test() {
    log_info "运行基本功能测试..."

    kubectl delete job etcd-test-job --ignore-not-found=true
    kubectl apply -f etcd-basic-test.yaml

    log_info "等待测试完成..."
    kubectl wait --for=condition=complete job/etcd-test-job --timeout=120s

    if [ $? -eq 0 ]; then
        log_success "基本测试完成"
        kubectl logs job/etcd-test-job
    else
        log_error "基本测试失败"
        kubectl describe job etcd-test-job
        return 1
    fi
}

# 运行 Watch 测试
run_watch_test() {
    log_info "运行 Watch 功能测试..."

    kubectl delete job etcd-watch-test --ignore-not-found=true
    kubectl apply -f etcd-watch-test.yaml

    log_info "等待 Watch 测试完成..."
    kubectl wait --for=condition=complete job/etcd-watch-test --timeout=60s

    if [ $? -eq 0 ]; then
        log_success "Watch 测试完成"
        echo "生产者日志:"
        kubectl logs job/etcd-watch-test -c watch-producer
        echo -e "\n消费者日志:"
        kubectl logs job/etcd-watch-test -c watch-consumer
    else
        log_warning "Watch 测试可能超时，查看日志..."
        kubectl describe job etcd-watch-test
    fi
}

# 运行性能基准测试
run_benchmark() {
    log_info "运行性能基准测试..."

    kubectl delete job etcd-benchmark --ignore-not-found=true
    kubectl apply -f etcd-benchmark.yaml

    log_info "等待基准测试完成..."
    kubectl wait --for=condition=complete job/etcd-benchmark --timeout=300s

    if [ $? -eq 0 ]; then
        log_success "性能基准测试完成"
        kubectl logs job/etcd-benchmark
    else
        log_error "性能基准测试失败"
        kubectl describe job etcd-benchmark
        return 1
    fi
}

# 启动监控
start_monitoring() {
    log_info "启动 etcd 监控..."

    kubectl apply -f etcd-monitor.yaml

    log_success "监控已启动，查看监控日志:"
    echo "kubectl logs -f deployment/etcd-monitor"
}

# 停止监控
stop_monitoring() {
    log_info "停止 etcd 监控..."
    kubectl delete -f etcd-monitor.yaml --ignore-not-found=true
    log_success "监控已停止"
}

# 清理测试资源
cleanup_tests() {
    log_info "清理测试资源..."

    kubectl delete -f etcd-basic-test.yaml --ignore-not-found=true
    kubectl delete -f etcd-watch-test.yaml --ignore-not-found=true
    kubectl delete -f etcd-benchmark.yaml --ignore-not-found=true
    kubectl delete pod etcd-test-client --ignore-not-found=true

    log_success "测试资源已清理"
}

# 显示使用帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  basic      - 运行基本功能测试"
    echo "  watch      - 运行 Watch 功能测试"
    echo "  benchmark  - 运行性能基准测试"
    echo "  monitor    - 启动监控"
    echo "  stop-monitor - 停止监控"
    echo "  all        - 运行所有测试"
    echo "  cleanup    - 清理测试资源"
    echo "  help       - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 all           # 运行所有测试"
    echo "  $0 basic         # 只运行基本测试"
    echo "  $0 monitor       # 启动监控"
    echo "  $0 cleanup       # 清理资源"
}

# 主函数
main() {
    case "${1:-all}" in
        "basic")
            check_etcd_service && run_basic_test
            ;;
        "watch")
            check_etcd_service && run_watch_test
            ;;
        "benchmark")
            check_etcd_service && run_benchmark
            ;;
        "monitor")
            check_etcd_service && start_monitoring
            ;;
        "stop-monitor")
            stop_monitoring
            ;;
        "all")
            if check_etcd_service; then
                run_basic_test
                run_watch_test
                run_benchmark
                log_success "所有测试完成!"
            fi
            ;;
        "cleanup")
            cleanup_tests
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"