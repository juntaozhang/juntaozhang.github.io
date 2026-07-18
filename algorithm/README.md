## 字符串

[LeetCode 6](src/test/java/cn/juntaozhang/leetcode/L6.java). Z 字形变换

- 构建一个 HashMap，key 是行数，value 是 StringBuilder，按行数来回折返遍历。

[字符匹配-KMP](./KMP.md)

## sort

[排序](./src/test/java/cn/juntaozhang/leetcode/sort/README.md)

- [912. 排序数组](https://leetcode.cn/problems/sort-an-array/) (中等)
  - **理由**：纯粹考察手写排序。必须掌握 **快速排序** 或 **归并排序** 的标准实现，理解分治与递归边界。
- [88. 合并两个有序数组](https://leetcode.cn/problems/merge-sorted-array/) (简单)
  - **理由**：归并排序的核心步骤。考察 **双指针** 从后向前填充，是处理有序数据合并的基础模板。

## 二分查找

L35

L354 贪婪 + 二分查找 hard

### 跳表

- [352. 将数据流变为多个不相交区间](https://leetcode.cn/problems/data-stream-as-disjoint-intervals/) (中等)
  - **理由**：**滑窗 + 有序集合**。这道题需要一个能快速获取“前驱”和“后继”的数据结构。虽然通常用 `TreeSet` 解，但其核心逻辑正是跳表最擅长的 **范围查找** 和 **邻近值定位**。

## hash

- [705. 设计哈希集合](https://leetcode.cn/problems/design-hashset/) (简单)
  - **理由**：**结构对比**。跳表常作为哈希表的替代方案（支持有序）。通过手写哈希表，理解“空间换时间”和“冲突处理”，对比跳表的层级索引思想。

## 链表反转

- [206. 反转链表](https://leetcode.cn/problems/reverse-linked-list/) (简单)

  - **理由**：绝对基础。必须能默写 **迭代法**（三指针）和 **递归法**，这是所有链表操作的基石。

  * [L206.java](../src/test/java/cn/juntaozhang/leetcode/L206.java)
- [92. 反转链表 II](https://leetcode.cn/problems/reverse-linked-list-ii/) (中等)

  - **理由**：进阶核心。考察 **指定区间** `[left, right]` 的反转，涉及头结点保护、断链与重连，是处理局部链表问题的标准范式。

## 深度优先 (DFS) 与 广度优先 (BFS)

- [200. 岛屿数量](src/test/java/cn/juntaozhang/leetcode/graph/graph.md#200-岛屿数量) 
- [695. 岛屿的最大面积](src/test/java/cn/juntaozhang/leetcode/graph/dfs_bfs/L695.java)
- [102. 二叉树的层序遍历](https://leetcode.cn/problems/binary-tree-level-order-traversal/submissions/732150079/): bfs + 记录每层长度
- 拓扑排序 - bfs
  - [210. 课程表 II](src/test/java/cn/juntaozhang/leetcode/graph/dfs_bfs/L210_bfs.java)

## 动态规划 (DP)
- base
  - L70. 爬楼梯 $f(x)=f(x−1)+f(x−2)$
- 子数组结尾型：dp[i] 定义为以第 i 个元素结尾的最优解

  - [L53 最大子数组和](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#53-最大子数组和)
  - L674 最长连续递增数列
  - L918 环形最大子数组和
- 一维线性

  - 相邻不能同时选
    - [L198 打家劫舍](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#198-打家劫舍)
    - [L213. 打家劫舍 II](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#213-打家劫舍-ii)
      - 在198基础上形成环，比较 去头 与 去尾。
- 多维线性
  - [L121 买卖股票的最佳时机](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#121-买卖股票的最佳时机)
  - [L122 买卖股票的最佳时机II](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#122-买卖股票的最佳时机-ii)
  - [L714 买卖股票的最佳时机含手续费](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#714-买卖股票的最佳时机含手续费)
  - [L309-最佳买卖股票时机含冷冻期](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#309-最佳买卖股票时机含冷冻期)
- [L300 最长递增子序列(LIS)](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#l300-最长递增子序列-lis)
  - [L674 最长连续递增序列](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#l674-最长连续递增序列)
  - [L646 LIS变形](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#l646-最长数对链lis变形)
  - [L673. 最长递增子序列的个数](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#l673-最长递增子序列的个数lis加强)
  - ⭐️[354. 俄罗斯套娃信封问题](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#) (hard dp LIS) -> (贪心 + 二分查找)
- 背包问题
  - 01背包: 二维线性 可以优化成一维
  - 完全背包
    - [322 零钱兑换](src/test/java/cn/juntaozhang/leetcode/dp/dp.md#322-零钱兑换) 
    - [279. 完全平方数](src/test/java/cn/juntaozhang/leetcode/dp/L279.java) 
- 二维DP TODO
  - 518 零钱兑换 II
  - 139 单词拆分
- 5,91,L416
- 1143. 最长公共子序列
- **LeetCode 139 单词拆分**
  字符串 DP，前缀拆分判断。
-**LeetCode 279 完全平方数**
- 一维 DP，类比零钱兑换。
- **LeetCode 416 分割等和子集**

## 前缀和
特殊的DP问题

- L1248. 统计优美子数组(hard)


| 题目特征                                                                                            | 推荐解法                           | 时间复杂度 | 空间复杂度 |
| :-------------------------------------------------------------------------------------------------- | :--------------------------------- | :--------- | :--------- |
| 全都是正数（如[LeetCode 209](src/test/java/cn/juntaozhang/leetcode/L209.java). 长度最小的子数组）   | 滑动窗口                           | $O(N)$     | $O(1)$     |
| 有正有负有零（如[LeetCode L560](src/test/java/cn/juntaozhang/leetcode/L560.java). 和为 K 的子数组） | 前缀和 + HashMap                   | $O(N)$     | $O(N)$     |
| [363. 矩形区域不超过 K 的最大数值和](src/test/java/cn/juntaozhang/leetcode/dp/L363.java)            | 前缀和 + 二分查找（Binary Search） |            |            |

---

### L303 区域和检索 - 数组不可变

**题意**：多次查询区间 `[left, right]` 连续和
**前缀和定义**：`pre[0]=0`，`pre[i]` 前`i`个元素和

### L304 二维区域和检索

在 L303 基础上扩展成二维

### L560 和为 K 的子数组

**题意**：统计有多少个**连续子数组和 = k**
**思路**：前缀和 + 哈希
前缀和：$pre[j] - pre[i] = k$
**案例**
输入：`nums = [1,1,1], k = 2`
符合子数组：`[0,1]`、`[1,2]`
输出：`2`

### L1248. 统计优美子数组

感觉 hard
给定一个未排序的整数数组 `nums` ， *返回最长递增子序列的个数* 。

nums = [0,0,1,0,2,1,1,1], k = 2
满足条件子数组共 `7` 个

```
nums = [  0,0,1,0,2,1,1,0], k = 2
       [0,0,0,1,1,1,2,3,3]  频次
dp = {                                后面前缀频次
    0->3,  0个奇数 有3种             + {}  {0}   {0,0}
    1->3,  1个奇数 有3种 {1}         + {}  {0}   {0,2}
    2->1,  2个奇数 有1种 {1,0,2,1}   + {}
    3->2,  3个奇数 有2种 {1,0,2,1,1} + {}  {0}
}
```

## [系统设计](src/test/java/cn/juntaozhang/design/README.md)

### [Geohash](../misc/geohash.md)
