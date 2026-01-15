# JobManager Election in Kubernetes
1. **多Pod启动阶段**：
   多个JobManager Pod同时启动，各自初始化并准备参与Leader选举。

2. **Leader竞争阶段**：
    - 所有Pod同时尝试向Kubernetes ConfigMap写入Leader锁信息
    - 利用Kubernetes ConfigMap的原子性更新特性，确保只有一个Pod能够成功获取锁
    - 成功获取锁的Pod成为**Active Leader**，其余Pod进入**Standby模式**

3. **Standby状态维护**：
    - Standby Pod持续监听ConfigMap中Leader锁的变化
    - Leader Pod定期更新锁的租约，维持Leader身份

4. **Leader故障检测**：
    - 当Leader Pod发生故障（崩溃或网络分区）时，其持有的锁租约过期
    - Kubernetes 自动检测到故障并清理 ConfigMap 中的 Leader 信息

5. **故障转移阶段**：
    - 所有Standby Pod立即感知到Leader锁的释放
    - 重新发起ConfigMap锁的竞争，最快获取锁的 Pod 成为新的 Leader

6. **作业恢复阶段**：
    - 新 Leader 从外部存储（如HDFS/S3）加载最近的 checkpoint 或 savepoint
    - 恢复作业执行状态和元数据
    - 通知所有 TaskManager 连接新的 Leader 并继续执行作业


## Architecture
```mermaid
---
config:
  flowchart:
    defaultRenderer: "dagre-wrapper"
---
flowchart TD
    subgraph "JobManager Pod 1 (Leader)"
        JM1["JobManager Pod 1<br/>KubernetesApplicationClusterEntrypoint"]
        JM1_Disp["Dispatcher<br/>Leader"]
        JM1_RM["ResourceManager<br/>Leader"]
        JM1_JM["JobMaster<br/>Leader"]
        JM1_LE["LeaderElection<br/>Driver"]
        JM1 --> JM1_Disp
        JM1 --> JM1_RM
        JM1 --> JM1_JM
        JM1 --> JM1_LE
    end
        subgraph "TaskManager Pods (多个副本)"
            TM1["TaskManager Pod 1<br/>TaskExecutor"]
            TM2["TaskManager Pod 2<br/>TaskExecutor"]
        TM3["TaskManager Pod 3<br/>TaskExecutor"]
    end

    subgraph "JobManager Pod 2 (Standby)"
        JM2["JobManager Pod 2<br/>KubernetesApplicationClusterEntrypoint"]
        JM2_Disp["Dispatcher<br/>Standby"]  
        JM2_RM["ResourceManager<br/>Standby"]
        JM2_JM["JobMaster<br/>Standby"]
        JM2_LR["LeaderRetrieval<br/>Service"]
        JM2 --> JM2_Disp
        JM2 --> JM2_RM
        JM2 --> JM2_JM
        JM2 --> JM2_LR
    end

    subgraph "JobManager Pod 3 (Standby)"
        JM3["JobManager Pod 3<br/>KubernetesApplicationClusterEntrypoint"]
        JM3_Disp["Dispatcher<br/>Standby"]
        JM3_RM["ResourceManager<br/>Standby"]
        JM3_JM["JobMaster<br/>Standby"]
        JM3_LR["LeaderRetrieval<br/>Service"]
        JM3 --> JM3_Disp
        JM3 --> JM3_RM
        JM3 --> JM3_JM
        JM3 --> JM3_LR
    end

    subgraph "Kubernetes ConfigMap (Leader Election & HA)"
        CM["ConfigMap:<br/>flink-application-cluster"]
        CM_Data["Data 字段:"]
        CM_RM["resourceManager.lock<br/>leader: Pod1-UUID<br/>address: pod1-ip:6123"]
        CM_Dispatcher["dispatcher.lock<br/>leader: Pod1-UUID<br/>address: pod1-ip:6123"]
        CM_Job["job_123.lock<br/>leader: Pod1-UUID<br/>address: pod1-ip:6123"]
        CM --> CM_Data
        CM_Data --> CM_RM
        CM_Data --> CM_Dispatcher
        CM_Data --> CM_Job
    end

    subgraph "Kubernetes Services"
        SVC["Service:<br/>flink-service-rest<br/>Selector: app=flink<br/>Port: 8081"]
    end

    JM1_LE -.->|" LeaderElection<br/>竞争锁 "| CM
    JM2_LR -.->|" LeaderRetrieval<br/>监听 leader "| CM
    JM3_LR -.->|" LeaderRetrieval<br/>监听 leader "| CM
    JM1_Disp <-->|" RPC "| JM1_JM
    JM1_RM <-->|" RPC "| TM1
    JM1_RM <-->|" RPC "| TM2
    JM1_RM <-->|" RPC "| TM3
    SVC -->|" 负载均衡 "| JM1
    SVC -->|" 负载均衡 "| JM2
    SVC -->|" 负载均衡 "| JM3
    style JM1 fill: #90EE90
    style JM1_Disp fill: #90EE90
    style JM1_RM fill: #90EE90
    style JM1_JM fill: #90EE90
    style JM2 fill: #FFD700
    style JM3 fill: #FFD700
```

## JobManger leader 选举工作机制
[KubernetesLeaderElectorExample](../../kubernetes/fabric8-example/src/main/java/KubernetesLeaderElectorExample.java) 展示了如何通过 k8s configMap 做主节点选举。
```mermaid
  sequenceDiagram
    autonumber
    participant Pod1 as JobManager Pod 1
    participant Pod2 as JobManager Pod 2
    participant Pod3 as JobManager Pod 3
    participant ConfigMap as Kubernetes ConfigMap
    participant K8sAPI as Kubernetes API
    participant TM as TaskManager Pod
    participant Client as Client/REST API

    Note over Pod1, Pod3: 阶段1: 多个 JobManager Pod 启动
    Pod1->>Pod1: KubernetesApplicationClusterEntrypoint.start()
    Pod2->>Pod2: KubernetesApplicationClusterEntrypoint.start()
    Pod3->>Pod3: KubernetesApplicationClusterEntrypoint.start()

    Note over Pod1, ConfigMap: 阶段2: Leader 选举 (ResourceManager)
    Pod1->>Pod1: ResourceManagerLeaderElectionDriver.start()
    Pod2->>Pod2: ResourceManagerLeaderRetrievalService.start()
    Pod3->>Pod3: ResourceManagerLeaderRetrievalService.start()

    par 所有 Pod 竞争锁
        Pod1->>ConfigMap: tryAcquireLock("resourceManager.lock")
        Pod2->>ConfigMap: watch("resourceManager.lock")
        Pod3->>ConfigMap: watch("resourceManager.lock")
    end

    ConfigMap-->>Pod1: Lock acquired! (通过 resourceVersion)
    ConfigMap-->>Pod2: MODIFIED event (Pod1 is leader)
    ConfigMap-->>Pod3: MODIFIED event (Pod1 is leader)

    Note over Pod1: Pod1 成为 Active Leader
    Pod1->>Pod1: ResourceManager.onGrantLeadership()
    Pod1->>Pod1: ResourceManager.start()

    Note over Pod2, Pod3: Pod2, Pod3 进入 Standby 模式
    Pod2->>Pod2: LeaderRetrievalListener.notifyLeaderAddress()
    Pod3->>Pod3: LeaderRetrievalListener.notifyLeaderAddress()
    Note right of Pod2: 等待 leader 变化通知

    Note over Pod1, TM: 阶段3: TaskManager 连接到 Leader
    TM->>ConfigMap: getLeaderAddress("resourceManager.lock")
    ConfigMap-->>TM: Pod1 address: pod1-ip:6123
    TM->>Pod1: registerTaskExecutor()
    Pod1->>Pod1: SlotManager.registerTaskManager()

    Note over Client, Pod1: 阶段4: 客户端通过 Service 访问
    Client->>K8sAPI: GET /api/v1/namespaces/{ns}/services/flink-service-rest
    K8sAPI-->>Client: Endpoints: [pod1-ip, pod2-ip, pod3-ip]
    Client->>Pod1: REST API request (可能路由到任意 Pod)

    alt 请求路由到 Leader (Pod1)
        Pod1-->>Client: 正常响应
    else 请求路由到 Standby (Pod2/Pod3)
        Pod2->>Pod1: 转发请求 (或返回重定向)
        Pod1-->>Client: 正常响应
    end

    Note over Pod1, ConfigMap: 阶段5: Leader 故障转移
    Pod1->>Pod1: Pod1 Crash/Network Partition
    ConfigMap->>ConfigMap: Lock lease expires<br/>or K8s detects Pod not ready
    ConfigMap->>ConfigMap: Delete "resourceManager.lock" owner

    par 重新选举
        Pod2->>ConfigMap: tryAcquireLock("resourceManager.lock")
        Pod3->>ConfigMap: tryAcquireLock("resourceManager.lock")
    end

    ConfigMap-->>Pod2: Lock acquired!
    ConfigMap-->>Pod3: MODIFIED event (Pod2 is leader)

    Note over Pod2: Pod2 成为新的 Leader
    Pod2->>Pod2: ResourceManager.onGrantLeadership()
    Pod2->>Pod2: recoverJobs()
    Pod2->>TM: Heartbeat from new leader
    TM->>Pod2: Re-register with new leader
```

# Q & A
## 为什么不用 StatefulSet 而用deployment 实现主备选举？

StatefulSet 的核心特性
- 为 Pod 提供 稳定的网络标识（如 pod-name-0, pod-name-1）
- 固定的存储卷。
- 各个实例不会自动调度服务角色（谁是主、谁是备）

Flink 不需要 StatefulSet 提供的核心特性：
- 无状态的，每个实例都是平等的，不依赖于固定的网络标识。
- 状态不存储在本地
- Flink 依赖协调服务的 Leader 选举，而 StatefulSet 本身不提供选举能力

### 为什么mysql 等 DB 却依赖 StatefulSet？
* 稳定的网络标识
  * 为每个 Pod 分配 唯一且稳定的主机名（如 mysql-0, mysql-1, mysql-2）
  * 每个 Pod 有固定的 DNS 记录（如 mysql-0.mysql.default.svc.cluster.local）
  * 无论 Pod 重启或迁移到哪个节点，网络标识保持不变
* 固定的存储卷
  - 每个 Pod 有独立的存储卷，用于存储数据库数据
  - PVC 与 Pod 的序号绑定（如 mysql-data-mysql-0 对应 mysql-0）
  - Pod 故障或重启时，新 Pod 会挂载原来的 PVC，确保数据不丢失
  - 存储卷的生命周期独立于 Pod，即使删除 StatefulSet，PVC 也会保留
- 有序的部署和扩展
  - 当 StatefulSet 扩展时，新的 Pod 会按照序号顺序部署（如 mysql-3）
  - 当 StatefulSet 缩减时，Pod 会按照序号顺序删除（如 mysql-2 先于 mysql-1）
- MySQL 主从复制是典型的“依赖节点身份”的主备架构
  - 主节点（mysql-0）负责写入，从节点（mysql-1, mysql-2）负责读取
  - 「主 / 从身份是固定的、预设的、强绑定节点」，不会自动切换
  - 主节点将写入操作记录到二进制日志（binlog）
  - 从节点通过读取 binlog 来复制主节点的写入操作
  - 即使 mysql-0 Pod 故障重启，也会重新挂载原存储卷，保持主节点身份
