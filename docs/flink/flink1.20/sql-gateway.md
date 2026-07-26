# SqlClient

SqlClient 支持两种运行模式：**embedded 模式**和**gateway 模式**，它们在架构设计、部署方式和使用场景上有显著区别。


| 特性 | embedded 模式 | gateway 模式 |
|------|---------------|--------------|
| 进程模型 | 单进程 | 客户端-服务器 |
| 资源消耗 | 高（包含完整执行器） | 低（仅客户端） |
| 部署复杂度 | 简单（一键启动） | 复杂（需先启动网关） |
| 网络连接 | 本地连接 | 远程连接 |
| 多客户端支持 | 不支持 | 支持 |
| 适用场景 | 开发测试 | 生产环境 |

## embedded
- SQL CLI 客户端与执行器在同一个进程中紧密耦合
- 在客户端内部创建并启动一个内嵌的 SQL Gateway (`EmbeddedGateway` 类)
- 使用命令:
  ```bash
  ./sql-client.sh embedded -Dkubernetes.cluster-id=flink1
  ```

## gateway
- SQL CLI 客户端通过 REST API 连接到独立部署的 SQL Gateway 服务
- 客户端-服务器架构，需要独立部署网关服务
- 使用命令：
  ```bash
  # 先启动网关服务
  ./sql-gateway.sh start
  
  # 然后启动客户端连接到网关
  ./sql-client.sh gateway -e <gateway-address:port>
  ```
