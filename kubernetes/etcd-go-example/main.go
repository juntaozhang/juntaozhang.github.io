package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	clientv3 "go.etcd.io/etcd/client/v3"
)

func main() {
	// 获取 etcd 端点配置
	etcdEndpoints := getEtcdEndpoints()

	// 创建 etcd 客户端
	client, err := clientv3.New(clientv3.Config{
		Endpoints:   etcdEndpoints,
		DialTimeout: 5 * time.Second,
	})
	if err != nil {
		log.Fatal("Failed to create etcd client:", err)
	}
	defer client.Close()

	fmt.Printf("连接到 etcd 成功! 端点: %v\n", etcdEndpoints)

	// 启动 Watch 示例
	go watchExample(client)

	// 启动一个简单的写入示例来触发 watch
	go writeExample(client)

	// 保持程序运行
	select {}
}

// getEtcdEndpoints 根据环境获取 etcd 端点
func getEtcdEndpoints() []string {
	// 检查环境变量
	if endpoints := os.Getenv("ETCD_ENDPOINTS"); endpoints != "" {
		return []string{endpoints}
	}

	// 检查是否在 Kubernetes 集群内
	if os.Getenv("KUBERNETES_SERVICE_HOST") != "" {
		// 在 Kubernetes 集群内，使用服务名
		return []string{"http://etcd-service:2379"}
	}

	// 本地开发环境，尝试 NodePort
	if isPortOpen("localhost:32379") {
		fmt.Println("检测到 Kubernetes NodePort，使用 localhost:32379")
		return []string{"localhost:32379"}
	}

	// 默认本地端口
	fmt.Println("使用默认本地端口 localhost:2379")
	return []string{"localhost:2379"}
}

// isPortOpen 简单检查端口是否开放
func isPortOpen(address string) bool {
	client, err := clientv3.New(clientv3.Config{
		Endpoints:   []string{address},
		DialTimeout: 1 * time.Second,
	})
	if err != nil {
		return false
	}
	defer client.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 1*time.Second)
	defer cancel()

	_, err = client.Status(ctx, address)
	return err == nil
}

// watchExample 演示如何 watch 一个 key 的变化
func watchExample(client *clientv3.Client) {
	fmt.Println("开始监听 /example/key 的变化...")

	// 创建 watch channel
	watchChan := client.Watch(context.Background(), "/example/key")

	// 监听变化
	for watchResp := range watchChan {
		for _, event := range watchResp.Events {
			switch event.Type {
			case clientv3.EventTypePut:
				fmt.Printf("🔄 键被更新: %s = %s\n", event.Kv.Key, event.Kv.Value)
			case clientv3.EventTypeDelete:
				fmt.Printf("🗑️  键被删除: %s\n", event.Kv.Key)
			}
		}
	}
}

// watchPrefixExample 演示如何 watch 一个前缀的所有变化
func watchPrefixExample(client *clientv3.Client) {
	fmt.Println("开始监听 /example/ 前缀的所有变化...")

	// 使用 WithPrefix 选项监听前缀
	watchChan := client.Watch(context.Background(), "/example/", clientv3.WithPrefix())

	for watchResp := range watchChan {
		for _, event := range watchResp.Events {
			switch event.Type {
			case clientv3.EventTypePut:
				fmt.Printf("🔄 前缀匹配 - 键被更新: %s = %s\n", event.Kv.Key, event.Kv.Value)
			case clientv3.EventTypeDelete:
				fmt.Printf("🗑️  前缀匹配 - 键被删除: %s\n", event.Kv.Key)
			}
		}
	}
}

// writeExample 演示写入数据来触发 watch
func writeExample(client *clientv3.Client) {
	time.Sleep(2 * time.Second) // 等待 watch 启动

	ctx := context.Background()

	// 写入一些示例数据
	examples := []struct {
		key   string
		value string
	}{
		{"/example/key", "初始值"},
		{"/example/key", "更新值1"},
		{"/example/key", "更新值2"},
		{"/example/config/db", "localhost:5432"},
		{"/example/config/redis", "localhost:6379"},
	}

	for i, example := range examples {
		time.Sleep(3 * time.Second)

		fmt.Printf("📝 写入第 %d 个值: %s = %s\n", i+1, example.key, example.value)

		_, err := client.Put(ctx, example.key, example.value)
		if err != nil {
			log.Printf("写入失败: %v", err)
		}
	}

	// 等待一会儿后删除一个键
	time.Sleep(3 * time.Second)
	fmt.Println("🗑️  删除 /example/key")
	_, err := client.Delete(ctx, "/example/key")
	if err != nil {
		log.Printf("删除失败: %v", err)
	}
}
