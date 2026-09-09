# HADOOP-16028 — NetworkTopology chooseRandom 不支持 excluded nodes

## Commit

- **Hash**: `3dd40008846`（branch-3.1，2019-01-04，Sihai Ke）
- **涉及文件**（2 个，+42/-2）：`NetworkTopology.java`、`TestClusterTopology.java`

## Diff 机制分析

`NetworkTopology.chooseRandom(scope, excludedScope, excludedNodes)` 用于按机架/作用域随机选 DN（balancer、block placement 的排除选点）。

原实现 bug：当传入 `excludedScope` 时，可用节点数用 `countNumOfAvailableNodes("~" + excludedScope, excludedNodes)` 计算，即"求反"路径——`countNumOfAvailableNodes` 的 `~` 排除逻辑**不扣除 excludedNodes 中已经落在 excludedScope 之外的节点**，导致可用节点数被高估。后果：算法以为还有候选节点，实际范围内节点全被排除时，会**死循环重试或选中被排除的节点**，balancer / 写路径选点异常。

修复：改为在 `netlock` 读锁内直接做减法——`countNumOfAvailableNodes(scope, excludedNodes) - countNumOfAvailableNodes(excludedScope, excludedNodes)`，语义正确且线程安全。

代码路径：NN `Balancer` / `BlockPlacementPolicyDefault.chooseTarget`（写 block 选点）、`DatanodeManager` 等所有调 `chooseRandom` 带排除集的调用方。

## 影响评估

**评级：中**。影响带排除集的随机选点正确性（balancer 和 block placement 都可能走到），错误场景下表现为选点死循环/卡慢（NN 热路径）或选点不符合排除约束。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
