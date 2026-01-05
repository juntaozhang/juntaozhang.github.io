# JobManager Election in Kubernetes

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

