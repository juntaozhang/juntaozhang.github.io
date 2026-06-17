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

- [200. 岛屿数量](https://leetcode.cn/problems/number-of-islands/) (中等)
  - **理由**：**DFS 代表**。网格搜索的经典模板，考察递归淹没、visited 标记，适用于所有连通块问题。
- [102. 二叉树的层序遍历](https://leetcode.cn/problems/binary-tree-level-order-traversal/) (中等)
  - **理由**：**BFS 代表**。队列使用的标准模板。相比单词接龙，这道题更纯粹地考察 BFS 的 **分层处理** 逻辑，是图论最短路径的基础。

## 动态规划 (DP)

- 子数组结尾型：dp[i] 定义为以第 i 个元素结尾的最优解
  - L53
- 最长递增子序列(LIS)
  - L300, L646, L354(hard dp -> 贪心+二分查找)
  - L673. 最长递增子序列的个数
  - L1248. 统计优美子数组
- TODO 线性 DP(打家劫舍)
  - L198, L213, L337
- TODO 子数组 / 子序列
  * LeetCode 322 零钱兑换
  * LeetCode 518 零钱兑换 II

    完全背包求方案数，和 673 计数思路相通。
  * **LeetCode 139 单词拆分**

    字符串 DP，前缀拆分判断。
  * **LeetCode 279 完全平方数**

    一维 DP，类比零钱兑换。
  * **LeetCode 416 分割等和子集**

### L53 最大子数组和

输入：`[-2,1,-3,4,-1,2,1,-5,4]`
最优连续子数组：`[4,-1,2,1]`
输出：`6`

DP定义：`dp[i]` 以下标`i`结尾的最大连续子数组和，这里可以使用一个变量替代优化。
状态方程:

$$
dp[i] = \max(nums[i],\ dp[i-1]+nums[i])
$$

[L53.java](assets/L53.java)

---

### L918 环形子数组的最大和

**题意**：数组首尾相连成环，求最大连续子数组和
**思路**

1. 普通最大子数组和（同53）
2. 跨首尾：数组总和 - 中间最小子数组和
3. 状态方程（LaTeX）

$$
dpMax[i] = \max(nums[i], dpMax[i-1] + nums[i]) \\
dpMin[i] = \min(nums[i], dpMin[i-1] + nums[i])
$$

4. 最终答案

   $ans = \max(dpMax,\ total - \max(dpMin))$

   **案例**
   输入：`[1,-2,3,-2]`
   环形最优：`3`

### L673. 最长递增子序列的个数

对 \(j < i\) 且 \(nums[j] < nums[i]\)：

$$
\begin{cases}
\boldsymbol{dp}[i] = \max\big(dp[i],\ dp[j]+1\big)\\[4pt]
\boldsymbol{cnt}[i] =
\begin{cases}
cnt[j], & dp[j]+1 > dp[i]\\
cnt[i] + cnt[j], & dp[j]+1 = dp[i]
\end{cases}
\end{cases}
$$

$$
L = \max_{0\le i<n} dp[i]\\
ans = \sum_{\,i:\ dp[i]=L} cnt[i]
$$

### L1248. 统计优美子数组

感觉 hard
给定一个未排序的整数数组 `nums` ， *返回最长递增子序列的个数* 。

nums = [0,0,1,0,2,1,1,1], k = 2
满足条件子数组共 `7` 个

```
nums = [  0,0,1,0,2,1,1,1], k = 2
pre  = [0,0,0,1,1,1,2,3,4], pre[0] 指的是什么都不选[1,0,2,1]，选1个[0,1,0,2,1]，选2个[0,0,1,0,2,1]
count= {
    0->3,
    1->3,
    2->1,
    3->1,
    4->1,
}

pre = 2: count[pre[2]-2] = 3
pre = 3: count[pre[3]-2] = 3
pre = 4: count[pre[4]-2] = 1
```

$$
dp[i] = 1,\quad cnt[i] = 1 \quad (\forall i)
$$

对每个 \(i\)，遍历所有 \(j < i\) 且 \(nums[j] < nums[i]\)：

1. **长度更新**：

$$
dp[i] = \left( \max_{\substack{0 \le j < i \\ nums[j] < nums[i]}} dp[j] \right) + 1
$$

2. **计数更新**：需要求所有 \(dp[j] = dp[i] - 1\) 的 cnt 和：

$$
cnt[i] = \sum_{\substack{0 \le j < i \\ nums[j] < nums[i] \\ dp[j] = dp[i] - 1}} cnt[j]
$$

以 `nums=[1,1,2,2,5]` 中 `i=4`（元素5）为例

$$
dp[4] = \left( \max_{\substack{0 \le j \le 3 \\ nums[j] < 5}} dp[j] \right) + 1 \Rightarrow dp[2] = dp[3] = 2 \Rightarrow dp[4] = 3
$$

$$
cnt[4] = cnt[2] + cnt[3] = 2 + 2 = 4
$$

```text
nums=[1, 1, 2, 2, 5]
  dp=[1, 1, 2, 2, 3]
 cnt=[1, 1, 2, 2, 4]
```

---

### L978 最长湍流子数组

**题意**：相邻元素高低交替（波浪形），求最长连续长度
**DP定义**

- `up[i]`：i结尾最后一步上升
- `down[i]`：i结尾最后一步下降
- 状态方程
  $$
  ums[i]>nums[i-1] \Rightarrow up[i]=down[i-1]+1,\ down[i]=1\\
  nums[i]<nums[i-1] \Rightarrow down[i]=up[i-1]+1,\ up[i]=1\\
  else \Rightarrow up[i]=down[i]=1
  $$

**案例**
输入：`[9,4,2,10,7,8,8,1,9]`
最长湍流子数组长度：`5`
输出：`5`

```
num =[9, 4, 2, 10, 7, 8, 8, 1, 9]
down=[1, 2, 2,  1, 4, 1, 1, 1, 3]
up  =[1, 1, 1,  3, 1, 5, 1, 2, 1]
```

---

### L674 最长连续递增序列

**题意**：求**严格连续递增**子数组最长长度
**DP定义**：`dp[i]` 以`i`结尾的最长连续递增长度
**状态方程**

$$
dp[i]=
\begin{cases}
dp[i-1]+1 & nums[i]>nums[i-1]\\
1 & else
\end{cases}
$$

**案例**
输入：`[1,3,5,4,7]`
最长连续递增：`[1,3,5]`、`[4,7]`
输出：`3`

---

### L152 乘积最大子数组

**题意**：含负数，求连续子数组乘积最大值
**DP定义**

- `dpMax[i]`：i结尾最大乘积
- `dpMin[i]`：i结尾最小乘积
  **状态方程**

  $$
  dpMax[i] = \max(nums[i], dpMax[i-1]*nums[i], dpMin[i-1]*nums[i])\\
  dpMin[i] = \min(nums[i], dpMax[i-1]*nums[i], dpMin[i-1]*nums[i])
  $$

  **案例**
  输入：`[2,3,-2,4]`
  最优子数组：`[2,3]`
  输出：`6`

---

### L300. 最长递增子序列

$ dp[i]：以\ nums[i]\ 结尾的最长递增子序列长度$

$dp[i] = \left( \max_{\substack{0 \le j < i \\ nums[j] < nums[i]}} dp[j] \right) + 1$

输入：nums = [0,1,0,3,2,3]
输出：4

---

### L646. 最长数对链

$ dp[i]：以\ nums[i]\ 结尾的最长递增子序列长度$

$dp[i] = \left( \max_{\substack{0 \le j < i \\ nums[j] < nums[i]}} dp[j] \right) + 1$

输入：pairs = [[1,2], [2,3], [3,4]]
输出：2
解释：最长的数对链是 [1,2] -> [3,4] 。

---

### 背包问题

#### 0/1背包问题

物品数n=4，背包容量C=5
物品：
w = [1, 2, 3, 2]
v = [1, 3, 4, 3]

#### L416. 分割等和子集

#### L322 [零钱兑换](https://leetcode.cn/problems/coin-change/) : 最少硬币数量

dp[0] = 0

dp[1] = dp[0] + 1

dp[11] = min(dp[11-1],dp[11-2],dp[11,5])

```
输入：coins = [1, 2, 5], amount = 11
输出：3 
解释：11 = 5 + 5 + 1
```

- L518 零钱兑换 II: 有多少种组合方式

  输入：amount = 5, coins = [1, 2, 5]
  输出：4
  解释：有四种方式可以凑成总金额：

  ```
  5=5
  5=2+2+1
  5=2+1+1+1
  5=1+1+1+1+1
  ```

  dp[0~5]状态方程：

$$
\begin{cases}
dp[0] = 1\\
dp[j] \mathrel{+}= dp[j - coin], & j \ge coin
\end{cases}
$$

---

## 前缀和

特殊的DP问题


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

## [系统设计](src/test/java/cn/juntaozhang/design/README.md)

### [Geohash](../misc/geohash.md)
