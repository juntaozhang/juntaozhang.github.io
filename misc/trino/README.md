
## Join
Trino 目前不支持 Sort-Merge Join。

```
     算子                     用途
     ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     LookupJoinOperator       Hash Join（等值 Join）
     NestedLoopJoinOperator   Cross Join / 非等值 Join
     IndexJoinNode            索引 Join（利用连接器索引）
```

## trino vs spark

Spark 的执行模式（DAG）：
```
    Stage 1 (Map)        Stage 2 (Reduce)      Stage 3 (Map)
    ┌─────────┐          ┌─────────┐          ┌─────────┐
    │ Task    │ ───────→ │ Task    │ ───────→ │ Task    │
    │ Task    │  Shuffle │ Task    │  Shuffle │ Task    │
    │ Task    │          │ Task    │          │ Task    │
    └─────────┘          └─────────┘          └─────────┘
         ↑                    ↑                    ↑
      有依赖关系，必须等上一个 Stage 完成才能开始
      Shuffle 是 Stage 的边界，数据落盘
```

  Trino 的执行模式（MPP）：
```
    ┌─────────┐  ┌─────────┐  ┌─────────┐
    │ Pipeline│  │ Pipeline│  │ Pipeline│   ← 尽量流水线执行
    │ (Scan → │  │ (Scan → │  │ (Scan → │
    │  Filter→│  │  Filter→│  │  Filter→│
    │  Join)  │  │  Join)  │  │  Join)  │
    └─────────┘  └─────────┘  └─────────┘
         ↑            ↑            ↑
      各节点尽量独立执行，减少全局同步点
```