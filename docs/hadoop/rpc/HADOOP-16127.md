# HADOOP-16127 — In ipc.Client, put a new connection could happen after stop

## Commit

- **Hash**: `ce804be72e1256d23340b6450d5d87ca419d70f7`
- **Date**: 2019-02-26
- **涉及文件**: `hadoop-common/.../ipc/Client.java`、`ClientCache.java`

## Diff 机制分析**

`Client.stop()` 与 `getConnection()` 存在竞态：stop 把 running 置 false 并
空转 sleep(10ms) 等 connections 清空，而 getConnection 在检查后仍可能
`putIfAbsent` 放入新 Connection——连接永不关闭（其线程在已 stop 的 client 上
空跑），stop() 也可能因新连接不断插入而长时间不返回。
修复：引入 `putLock` 锁——stop 与 getConnection 的 computeIfAbsent 同在
putLock 临界区内先查 running；空转等待改为 `emptyCondition.wait()/notify`
（removeMethod 回调在删空连接集时唤醒）；refCount 改 AtomicInteger 去掉
ClientCache 的双重检查同步。

## 影响评估

**中** — IPC 客户端框架的关闭竞态：高频创建/销毁 RPC client（HiveServer2、
每次 job 新建 FileSystem 等）场景下会造成幽灵连接线程堆积与 stop 挂起。

## apply 检查结果

**冲突**：`Client.java`（getConnection/stop 区与 3.1.1 差异较大，需手工合并）。
