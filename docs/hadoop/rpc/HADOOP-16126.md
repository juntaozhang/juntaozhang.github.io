# HADOOP-16126. ipc.Client.stop() may sleep too long to wait for all connections

## Commit
- Hash: 8d71b508abc（2019-02-25）
- 文件: hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Client.java

## Diff 机制分析
Client.stop() 里等待所有 connection 关闭的循环使用 `Thread.sleep(100)`，连接关闭是毫秒级事件，每轮却固定睡眠 100ms。patch 将睡眠改为常量 `STOP_SLEEP_TIME_MS = 10`。不改动关闭协议本身，只把轮询间隔从 100ms 降到 10ms。

## 影响评估
低。只影响客户端关闭阶段的总耗时（最坏从 100ms 降到 10ms 粒度），不改 RPC 热路径。但对高频启停 client 的进程（如短生命周期任务、部分组件 shutdown 序列）可缩短退出延迟，风险几乎为零。

## apply 检查
clean
