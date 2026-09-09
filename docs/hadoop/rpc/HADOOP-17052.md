# HADOOP-17052 — NetUtils.connect() throws unchecked exception (UnresolvedAddressException) causing clients to abort

## Commit

- **Hash**: `2c40f20debd`
- **日期**: 2020-06-01
- **涉及文件**: `hadoop-common/.../net/NetUtils.java`（+3）；测试 `TestNetUtils`

## Diff 机制分析

`NetUtils.connect` 对 unresolved 地址抛 UnresolvedAddressException（RuntimeException），绕过所有受检 IOException 重试逻辑，上层 IPC 客户端直接失败退出。修复在 connect 内显式检测 `!addr.isUnresolved()`/捕获该 unchecked 异常并包装为 IOException，纳入既有重试体系。

## 影响评估

**中**。与 HADOOP-17068 配套的底层修复，影响所有走 NetUtils.connect 的客户端重连行为（NN 换 IP/DNS 抖动场景）。改动 3 行，低风险。

## apply 检查结果

**clean**。
