
sqlContext.sql("SELECT name, age FROM people WHERE age >= 13 AND age <= 19")

SparkSQLParser.parse

## catalyst

## spark sql core
### SparkSessionExtensions
提供了一系列方法用于扩展 Spark SQL 的核心功能，允许开发者注入自定义逻辑来增强解析、优化、执行等环节。以下是你提到的几个方法的具体作用：
例如：[paimon compact](../bigdata/paimon/docs/compact-src.md) 在paimon 中增加了对 `COMPACT` 语句的支持等

injectResolutionRule
Spark SQL 逻辑计划处理的早期阶段，主要负责将未解析的逻辑计划（Unresolved Logical Plan）转换为已解析的逻辑计划（Resolved Logical Plan）

injectPostHocResolutionRule
该规则在基础解析规则执行完成后运行，用于对已解析的逻辑计划进行补充处理

injectOptimizerRule
优化阶段是将已解析的逻辑计划转换为优化后的逻辑计划（Optimized Logical Plan），通过应用各种规则（如谓词下推、常量折叠、列裁剪等）提升查询效率

injectPlannerStrategy
规划阶段负责将优化后的逻辑计划转换为物理计划（Physical Plan）


## 参考
- [rule of CTESubstitution ResolveReferences](https://liuxiaofei.com.cn/blog/analyzer-in-spark/)