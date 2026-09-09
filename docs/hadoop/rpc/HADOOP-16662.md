# HADOOP-16662 — Remove unnecessary InnerNode check in NetworkTopology#add()

## Commit

- **Hash**: `9434f36b436b15663c5e1b2739fe3c46aaef35ad`
- **Date**: 2019-10-19
- **涉及文件**: `hadoop-common/.../net/NetworkTopology.java`

## Diff 机制分析

`add(Node)` 中维护 depthOfAllLeaves（全叶子同深度假设用的缓存）时先
`if (!(node instanceof InnerNode))`——但 add 的入口已保证只在叶子层调用
（InnerNode 的创建走另外路径），该判断恒真，纯冗余。删除判断直接记录 level。
行为不变，纯简化。注意：叶子层级的一致性靠既有不变量维持。

## 影响评估

**低** — NetworkTopology.add 在 DN 注册/拓扑变更时调用，改动只是删冗余分支，
无行为变化。

## apply 检查结果

**clean**。
