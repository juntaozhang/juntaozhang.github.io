# HADOOP-15742 — Log if ipc backoff is enabled in CallQueueManager

## Commit

- **Hash**: ee051ef9fec1fddb612aa1feae9fd3df7091354f
- **Author**: Yiqun Lin（补丁贡献者：Ryan Wu）
- **Date**: 2018-09-18
- **涉及文件**（1 个，+3/-2）:
  - `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/CallQueueManager.java`

## Diff 机制分析

`CallQueueManager` 构造函数里唯一的 INFO 日志由字符串拼接改为参数化日志，并补上 `ipcBackoff` 字段：

```
# 改前
LOG.info("Using callQueue: " + backingClass + " queueCapacity: " +
    maxQueueSize + " scheduler: " + schedulerClass);
# 改后
LOG.info("Using callQueue: {}, queueCapacity: {}, " +
    "scheduler: {}, ipcBackoff: {}.",
    backingClass, maxQueueSize, schedulerClass, clientBackOffEnabled);
```

机制价值：`clientBackOffEnabled`（即配置 `ipc.<port>.backoff.enable`，开启后服务器端队列满时通过持续读取请求促使客户端退避重试）此前在启动日志中完全不可见。RPC 过载行为对 backoff 配置极其敏感——同样的队列压力，开启/未开启 backoff 时客户端表现完全不同（重试风暴 vs 服务端静默排队），启动日志里缺这一项会让"是否生效"只能靠抓包或行为推测。

## JIRA 页面信息

- **标题**: Log if ipc backoff is enabled in CallQueueManager
- **类型/状态**: Improvement，Resolved/Fixed（2018-09-18），Hadoop Flags: Reviewed
- **Affects Version/s**: 3.1.1
- **Fix Version/s**: 3.2.0
- **Priority**: Minor
- **Reporter**: Yiqun Lin（Assignee: Ryan Wu）
- **报告人描述要点**: IPC backoff 状态当前没有被记录；建议在现有日志语句中补上，使用户能直接看出 backoff 是否激活。描述中贴了 `CallQueueManager` 构造函数的现有日志代码（callQueue class / capacity / scheduler），并给出预期格式——与最终补丁完全一致。
- **Benchmark/性能数据**: 无，纯日志改进。
- **讨论**: 页面无可见评论；Ryan Wu 上传 3 版小补丁（13/Sep ×2，17/Sep ×1，各约 1 kB），iter 修订后合入。

## 对 RPC 调优/排障的价值评估：**中**

理由：

- `ipc.backoff.enable` 是 RPC 过载保护的关键开关，直接影响"队列满"时的全链路行为（配合 HADOOP-15673 一类 backoff 工作）。启动时一行日志即可确认配置是否被读取生效，省去对照端口/配置文件的核对工作——对排查"客户端为何大量重试/超时"这类高频问题有直接的排障价值。
- 评级不到"高"的原因：信息量本身很小（一个布尔值），且只影响启动日志，不涉及运行时行为或性能。

## 是否值得 cherry-pick 到 3.1.1：**值得，冲突难度极低（几乎零风险）**

- 该 JIRA 的 Affects Version 就是 3.1.1——backoff 日志缺失正是 3.1.1 用户遇到的问题。
- `git apply --check` 实测对 rel/release-3.1.1 **干净通过**（1 个文件，单 hunk）。
- 附带小收益：顺带把字符串拼接日志改成了 SLF4J 参数化形式，避免了每次启动都发生的字符串拼接开销（虽然微小）。
- 与 HADOOP-15611 同属 Ryan Wu 的 FairCallQueue/backoff 可观测性系列，建议一起 cherry-pick。
