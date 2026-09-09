# HADOOP-17130 — Configuration.getValByRegex() shouldn't be updating the results while fetching

## Commit

- **Hash**: `cb3e3787820`
- **日期**: 2020-07-16
- **涉及文件**: `hadoop-common/.../conf/Configuration.java`（+4/-2）

## Diff 机制分析

`getValByRegex` 在遍历 properties 的同时向结果 map 写匹配项——若正则恰好匹配结果 map 自身相关的内部键（或调用方在遍历时读取结果），会触发 ConcurrentModificationException/不一致。修复先把匹配收集到临时 map，遍历结束后再返回。

## 影响评估

**低**。正确性修复，只在特定正则+并发使用下出问题；Configuration 是所有组件的公共底座，修复零风险。

## apply 检查结果

**clean**。
