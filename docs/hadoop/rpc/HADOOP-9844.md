# HADOOP-9844. NPE when trying to create an error message response of SASL RPC

## Commit
- Hash: 1b7c7dc860d（2017-10-13）
- 文件: IpcException.java, Server.java

## Diff 机制分析
SASL RPC 协商失败构建错误响应时，某字段（errorMessage/exception 类名）为 null，Server.setupResponse 解引用抛 NPE，客户端收到的不是预期的 SASL 错误而是连接中断。patch 修 IpcException 的构造与 Server 的响应组装，对 null 做保护。

## 影响评估
低。触发条件是 SASL 认证失败路径本身，正常环境不命中；命中时表现为认证失败的诊断信息丢失/连接异常，影响 Kerberos 环境的故障定位而非正常吞吐。修复极小，可顺手 cherry-pick。

## apply 检查
clean
