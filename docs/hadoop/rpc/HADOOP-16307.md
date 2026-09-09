# HADOOP-16307 — FileStatus 中 intern 用户名/组名

## Commit

- **Hash**: `cf0d5a0e6ee`（branch-3.1，2019-05-16，David Mollitor）
- **涉及文件**（1 个，+3/-2）：`PBHelper.java`（hadoop-common fs.protocolPB）

## Diff 机制分析

`PBHelper.convert(FileStatusProto)` 把 protobuf 的 `FileStatus` 转成客户端对象，其中 `owner`/`group` 字段是字符串。大规模 `listStatus` / `getFileStatus` 时，每个返回项都从 protobuf 解码出**全新的** owner/group 字符串副本——实际值高度重复（集群用户/组数量很少），白白制造大量短命 String，增加 young GC 压力和客户端堆占用。

修复：`StringInterner.weakIntern()` 对 owner/group 做弱引用 intern，相同值的 FileStatus 共享同一 String 实例。

代码路径：客户端每次 `listStatus`/`getFileStatus` RPC 响应的反序列化（NN 返回的 FileStatusProto → FileStatus）。

## 影响评估

**评级：低**。客户端侧内存/GC 微优化，对 NN 服务端无直接收益；对频繁列目录的应用（Hive/Spark 任务规划）有可见的 young GC 改善。

## apply 检查结果

`git apply --check` 到 3.1.1：**clean**。
