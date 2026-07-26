# Flink 基础知识
## Flink三种重启策略

根据Flink 1.20的代码实现，以下是三种核心重启策略的详细示例：

### 固定延迟重启策略 (Fixed Delay)

```yaml
restart-strategy.type: fixed-delay
restart-strategy.fixed-delay.attempts: 3
restart-strategy.fixed-delay.delay: 5s
```


| 时间点 | 事件 | 说明 |
|-------|------|------|
| T=0s | 作业启动 | 正常开始运行 |
| T=10s | 任务失败 | 第一次失败 |
| T=15s | 重启尝试1 | 等待5秒后第一次重启 |
| T=20s | 任务失败 | 第二次失败 |
| T=25s | 重启尝试2 | 等待5秒后第二次重启 |
| T=30s | 任务失败 | 第三次失败 |
| T=35s | 重启尝试3 | 等待5秒后第三次重启 |
| T=40s | 任务失败 | 第四次失败，达到最大尝试次数(3次) |
| T=40s | 作业最终失败 | 不再尝试重启 |

关键特性
- 每次重启间隔固定（5秒）
- 最多尝试3次重启
- 重启尝试计数从0开始：`currentRestartAttempt <= maxNumberRestartAttempts`
- 适用于临时的、可快速恢复的故障

### 失败率重启策略 (Failure Rate)

```yaml
restart-strategy.type: failure-rate
restart-strategy.failure-rate.max-failures-per-interval: 3
restart-strategy.failure-rate.failure-rate-interval: 1min
restart-strategy.failure-rate.delay: 2s
```

**场景1：短时间内频繁失败**

| 时间点 | 事件 | 失败队列 | 说明 |
|-------|------|---------|------|
| T=0s | 作业启动 | [] | 正常开始运行 |
| T=5s | 失败1 | [5] | 第一次失败 |
| T=7s | 重启1 | [5] | 等待2秒后重启 |
| T=10s | 失败2 | [5, 10] | 第二次失败 |
| T=12s | 重启2 | [5, 10] | 等待2秒后重启 |
| T=15s | 失败3 | [5, 10, 15] | 第三次失败 |
| T=17s | 重启3 | [5, 10, 15] | 等待2秒后重启 |
| T=20s | 失败4 | [10, 15, 20] | 第四次失败（队列满，移除最早记录） |
| T=20s | 作业失败 | [10, 15, 20] | 1分钟窗口内失败次数达到3次上限 |

**场景2：失败间隔较长**

| 时间点 | 事件 | 失败队列 | 说明 |
|-------|------|---------|------|
| T=0s | 作业启动 | [] | 正常开始运行 |
| T=5s | 失败1 | [5] | 第一次失败 |
| T=7s | 重启1 | [5] | 等待2秒后重启 |
| T=120s | 失败2 | [120] | 1分钟后第二次失败（之前记录已过期） |
| T=122s | 重启2 | [120] | 等待2秒后重启 |
| T=180s | 失败3 | [120, 180] | 3分钟时第三次失败 |
| T=182s | 重启3 | [120, 180] | 等待2秒后重启 |
| T=240s | 失败4 | [180, 240] | 4分钟时第四次失败 |
| T=242s | 重启4 | [180, 240] | 继续重启（1分钟内只有2次失败） |

关键特性
- 维护一个失败时间队列，跟踪最近失败记录
- 1分钟窗口内最多允许3次失败
- 每次重启间隔固定（2秒）
- 适用于间歇性、不可预测的故障

### 指数延迟重启策略 (Exponential Delay)

配置示例
```yaml
restart-strategy.type: exponential-delay
restart-strategy.exponential-delay.initial-backoff: 1s
restart-strategy.exponential-delay.max-backoff: 30s
restart-strategy.exponential-delay.backoff-multiplier: 2.0
restart-strategy.exponential-delay.jitter-factor: 0.1
restart-strategy.exponential-delay.reset-backoff-threshold: 10min
restart-strategy.exponential-delay.attempts: 4
```

| 失败次数 | 基础间隔 | 抖动范围 | 实际间隔 | 时间点 |
|---------|---------|---------|---------|-------|
| 1 | 1s | ±0.1s | 0.95s | T=10.95s |
| 2 | 2s | ±0.2s | 2.1s | T=13.05s |
| 3 | 4s | ±0.4s | 3.8s | T=16.85s |
| 4 | 8s | ±0.8s | 8.6s | T=25.45s |
| 5 | 16s | ±1.6s | 15.2s | T=40.65s |

稳定运行后重置场景
```
T=40.65s: 第五次重启后作业稳定运行
T=10m40s: 稳定运行10分钟，间隔重置
T=10m45s: 再次失败，从初始间隔开始计算
T=10m46.1s: 重启（基础间隔1s + 10%抖动）
```

关键特性
- 重启间隔指数增长（1s → 2s → 4s → 8s...）
- 最大间隔限制为30秒
- 10%抖动避免集群资源竞争
- 稳定运行10分钟后重置间隔
- Flink 1.20默认策略（启用Checkpoint时）

