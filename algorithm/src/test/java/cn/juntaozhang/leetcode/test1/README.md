
### 📅 第 1 天：分布式 ID 与 数据分片 (Sharding & ID)
**系统场景**：生成全局唯一 ID (Snowflake)，数据如何路由到不同数据库分片。
**核心思维**：位运算、哈希映射、取模。

| 类型          | 题目 | 难度 | 核心考点 | 系统设计映射 |
|:------------| :--- | :--- | :--- | :--- |
| 🟢 基础复习 | [191. 位 1 的个数](https://leetcode.cn/problems/number-of-1-bits/) | 简单 | 位运算 (`n & (n-1)`) | 理解 Snowflake 算法中机器码、序列号的位操作。 |
| 🟢 基础复习 | [1. 两数之和](https://leetcode.cn/problems/two-sum/) | 简单 | **哈希表查找** | 最基础的 Key-Value 映射，所有路由表的基础。 |
| 🔵 系统核心 | [166. 分数到小数](https://leetcode.cn/problems/fraction-to-recurring-decimal/) | 中等 | 哈希表检测循环 + 模拟 | 模拟高精度计算，理解 ID 生成中序列号回滚的逻辑。 |
| 🔵 系统核心 | [705. 设计哈希集合](https://leetcode.cn/problems/design-hashset/) | 简单 | **哈希函数设计** | **核心**：手写一个简单的 Hash Map，理解冲突解决（链地址法），这是分片路由的雏形。 |

> 💡 **今日思考**：如果哈希冲突太多怎么办？（引出：扩容 rehash、一致性哈希环）。

---

### 📅 第 2 天：缓存策略与 限流算法 (Caching & Rate Limiting)
**系统场景**：Redis 缓存淘汰 (LRU)，API 网关限流 (滑动窗口)。
**核心思维**：双向链表 + 哈希表、队列、时间窗口。

| 类型          | 题目                                                                    | 难度 | 核心考点 | 系统设计映射 |
|:------------|:----------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 基础复习 | [20. 有效的括号](https://leetcode.cn/problems/valid-parentheses/)      | 简单 | **栈 (Stack)** | 理解 LIFO 结构，是链表操作的基础。 |
| 🟢 基础复习 | [141. 环形链表](https://leetcode.cn/problems/linked-list-cycle/)      | 简单 | 快慢指针 | 熟悉链表操作，防止链表死循环（LRU 实现必备）。 |
| 🔵 系统核心 | [146. LRU 缓存](https://leetcode.cn/problems/lru-cache/)            | 中等 | **HashMap + 双向链表** | **必考**：Redis 核心算法。必须掌握如何将节点从链表移到头部。 |
| 🔵 系统核心 | [359. 日志速率限制器](https://leetcode.cn/problems/logger-rate-limiter/) | 简单 | 哈希表 + 时间戳 | **最简限流**：实现一个基于时间戳的固定窗口限流器。 |

> 💡 **今日思考**：LRU 为什么不用数组？（随机访问 O(1) vs 移动元素 O(N)）。限流如果并发很高，单机计数不准怎么办？（引出：Redis Lua 脚本、分布式限流）。

---

### 📅 第 3 天：海量数据与 Top K 问题 (Big Data & Top K)
**系统场景**：热搜榜、高频 IP 统计、实时排行榜。
**核心思维**：堆 (Heap)、哈希计数、分治思想。

| 类型          | 题目                                                                                      | 难度 | 核心考点 | 系统设计映射 |
|:------------|:----------------------------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 基础复习 | [21. 合并两个有序链表](https://leetcode.cn/problems/merge-two-sorted-lists/)                | 简单 | 链表归并 | 理解多路归并的基础，用于处理分片后的数据合并。 |
| 🟢 基础复习 | [349. 两个数组的交集](https://leetcode.cn/problems/intersection-of-two-arrays/)            | 简单 | 哈希表去重 | 基础的数据清洗和集合操作。 |
| 🔵 系统核心 | [347. 前 K 个高频元素](https://leetcode.cn/problems/top-k-frequent-elements/)             | 中等 | **哈希表 + 小顶堆** | **经典场景**：统计频率并取 Top K。理解为什么用“小顶堆”维护前 K 大。 |
| 🔵 系统核心 | [215. 数组中的第 K 个最大元素](https://leetcode.cn/problems/kth-largest-element-in-an-array/) | 中等 | 快速选择 (Quick Select) | 比排序更快 (O(N)) 的查找方法，适合内存有限的场景。 |

> 💡 **今日思考**：如果数据有 100 亿条，内存只有 1GB，怎么做 Top K？（答案：哈希分片 -> 文件归并 -> 堆）。

---

### 📅 第 4 天：消息队列与 负载均衡 (MQ & Load Balancing)
**系统场景**：任务调度、请求分发、延迟队列。
**核心思维**：优先级队列、单调性、资源分配。

| 类型          | 题目                                                                                         | 难度 | 核心考点 | 系统设计映射 |
|:------------|:-------------------------------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 基础复习 | [225. 用队列实现栈](https://leetcode.cn/problems/implement-stack-using-queues/)              | 简单 | 队列与栈转换 | 深入理解 FIFO 和 LIFO 的区别。 |
| 🟢 基础复习 | [232. 用栈实现队列](https://leetcode.cn/problems/implement-queue-using-stacks/)              | 简单 | 双栈模拟队列 | 理解 amortized O(1) 的概念。 |
| 🔵 ~系统核心  | [239. 滑动窗口最大值](https://leetcode.cn/problems/sliding-window-maximum/)                   | 中等 | **单调队列** | 高性能流式计算。比堆更高效地获取窗口内的最大值（监控峰值）。 |
| 🔵 系统核心 | [1606. 找到处理最多请求的服务器](https://leetcode.cn/problems/find-servers-handled-most-requests/) | 中等 | **堆 + 有序集合** | **负载均衡**：模拟将请求分配给最早空闲的服务器，典型的调度算法。 |

> 💡 **今日思考**：单调队列为什么比堆快？（堆是 O(log K)，单调队列均摊 O(1)）。

---

### 📅 第 5 天：搜索自动补全与 敏感词过滤 (Trie & Search)
**系统场景**：搜索框提示、URL 路由匹配、敏感词屏蔽。
**核心思维**：字典树 (Trie)、前缀匹配。

| 类型          | 题目                                                                                           | 难度 | 核心考点 | 系统设计映射 |
|:------------|:---------------------------------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 **基础复习** | **[383. 赎金信](https://leetcode.cn/problems/ransom-note/)**                                    | 简单 | 字符计数数组 | 基础的字符串统计，敏感词匹配的简化版。 |
| 🟢 **基础复习** | **[125. 验证回文串](https://leetcode.cn/problems/valid-palindrome/)**                             | 简单 | 双指针 | 字符串处理的基本功。 |
| 🔵 系统核心 | [208. 实现 Trie (前缀树)](https://leetcode.cn/problems/implement-trie-prefix-tree/)           | 中等 | **Trie 树结构** | **必考**：搜索引擎自动补全的核心。理解节点结构和空间换时间。 |
| 🔵 系统核心 | [211. 添加与搜索单词](https://leetcode.cn/problems/design-add-and-search-words-data-structure/) | 中等 | Trie + DFS | 支持通配符 (`.`) 搜索，模拟简易的正则匹配引擎。 |

> 💡 **今日思考**：Trie 树太占内存怎么办？（引出：压缩 Trie / Radix Tree，如 Nginx 路由匹配）。

---

### 📅 第 6 天：时序监控与 区间查询 (Time Series & Intervals)
**系统场景**：监控系统（CPU/内存曲线）、IP 白名单管理、会议预定。
**核心思维**：单调栈、区间合并、前缀和。

| 类型          | 题目                                                                                                                         | 难度 | 核心考点 | 系统设计映射 |
|:------------|:---------------------------------------------------------------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 基础复习 | [704. 二分查找](https://leetcode.cn/problems/binary-search/)                                                               | 简单 | **二分查找** | 所有有序数据查找的基础，日志检索必备。 |
| 🟢 基础复习 | [26. 删除有序数组中的重复项](https://leetcode.cn/problems/remove-duplicates-from-sorted-array/)                                   | 简单 | 双指针去重 | 数据降采样（Downsampling）的基础逻辑。 |
| 🔵 系统核心 | [56. 合并区间](https://leetcode.cn/problems/merge-intervals/)                                                              | 中等 | **排序 + 贪心** | **核心**：管理 IP 段、时间片占用。判断时间段是否冲突。 |
| 🔵 **系统核心** | **[731. 我的日程安排表 II](https://leetcode.cn/problems/my-calendar-ii/)** (或 [729](https://leetcode.cn/problems/my-calendar-i/)) | 中等 | 列表遍历/差分数组 | 检查时间区间内的最大重叠数（防止资源超卖）。 |

> 💡 **今日思考**：如果区间非常多，每次遍历太慢怎么办？（引出：线段树、区间树）。

---

### 📅 第 7 天：综合设计演练 (Comprehensive Design)
**系统场景**：综合运用哈希、堆、链表设计一个小型系统组件。
**核心思维**：数据结构组合、API 设计。

| 类型          | 题目                                                                                       | 难度 | 核心考点 | 系统设计映射 |
|:------------|:-----------------------------------------------------------------------------------------| :--- | :--- | :--- |
| 🟢 基础复习 | [380. O(1) 时间插入/删除和获取随机元素](https://leetcode.cn/problems/insert-delete-getrandom-o1/) | 中等 | **哈希 + 数组交换** | **核心**：设计高性能 Session 存储。利用数组尾部交换实现 O(1) 删除。 |
| 🟢 基础复习 | [155. 最小栈](https://leetcode.cn/problems/min-stack/)                                  | 中等 | 辅助栈 | 在 O(1) 时间内获取当前状态的最小值（监控当前最低水位）。 |
| 🔵 系统核心 | [设计 Twitter](https://leetcode.cn/problems/design-twitter/)                           | 中等 | **堆 + 哈希 + 链表** | **微型社交网络**：模拟关注关系、时间线聚合（Merge K Sorted Lists）。 |
| 🔵 系统核心 | [706. 设计哈希映射](https://leetcode.cn/problems/design-hashmap/)                          | 简单 | 完整实现 HashMap | 再次巩固哈希表细节（扩容、哈希函数），这是所有系统的基石。 |

> 💡 **今日总结**：系统设计没有银弹，通常是 **HashMap (快速查找) + Heap (排序/TopK) + List/Queue (顺序/缓冲)** 的组合拳。

---

### 🚀 给“偏系统设计”选手的特别建议

1.  **重视“设计类”题目**：
    *   注意题目名称中带有 **“Design”** 的题（如 `Design HashMap`, `LRU Cache`, `Min Stack`）。
    *   这类题不仅考算法，还考 **API 接口定义** 和 **数据结构选型**，这与系统设计面试最为接近。

2.  **复杂度敏感度**：
    *   做每道题时，强制自己分析：
        *   时间复杂度是多少？(O(1), O(log N), O(N)?)
        *   空间复杂度是多少？
        *   **如果 QPS 达到 10 万，这个复杂度能扛住吗？**
    *   例如：如果在循环里用了排序 O(N log N)，在大数据量下就是系统瓶颈。

3.  **不要死磕代码细节**：
    *   对于简单题，追求一次写对，锻炼代码整洁度。
    *   对于中等题，重点在于**思路**：为什么选这个数据结构？有没有替代方案？（例如：用堆还是用快速选择？用数组还是链表？）

4.  **关联真实组件**：
    *   做完 LRU -> 联想 Redis 配置 `maxmemory-policy`。
    *   做完 Top K -> 联想 Elasticsearch 的 `top_hits` 或 Redis 的 `ZSET`。
    *   做完 合并区间 -> 联想 Nginx 的 IP 段配置或 数据库的范围锁。

这个计划去掉了高难度的算法陷阱，专注于**工程实践中真正用到**的数据结构逻辑，非常适合为系统设计打基础。祝你练习愉快！


### 1. 排序算法 (Sorting)
*   [912. 排序数组](https://leetcode.cn/problems/sort-an-array/) (中等)
    *   **理由**：纯粹考察手写排序。必须掌握 **快速排序** 或 **归并排序** 的标准实现，理解分治与递归边界。
*   [88. 合并两个有序数组](https://leetcode.cn/problems/merge-sorted-array/) (简单)
    *   **理由**：归并排序的核心步骤。考察 **双指针** 从后向前填充，是处理有序数据合并的基础模板。

### 2. 链表反转 (Linked List Reversal)
*   [206. 反转链表](https://leetcode.cn/problems/reverse-linked-list/) (简单)
    *   **理由**：绝对基础。必须能默写 **迭代法**（三指针）和 **递归法**，这是所有链表操作的基石。
    * [L206.java](../src/test/java/cn/juntaozhang/leetcode/L206.java)
*   [92. 反转链表 II](https://leetcode.cn/problems/reverse-linked-list-ii/) (中等)
    *   **理由**：进阶核心。考察 **指定区间** `[left, right]` 的反转，涉及头结点保护、断链与重连，是处理局部链表问题的标准范式。

### 3. 跳表 (Skip List)
*(注：LeetCode 中直接考察跳表的题目极少，且唯一一道是困难题。此处替换为 **1 道原理题 + 1 道应用场景题**，均使用常规数据结构模拟跳表逻辑)*

*   [705. 设计哈希集合](https://leetcode.cn/problems/design-hashset/) (简单)
    *   **理由**：**结构对比**。跳表常作为哈希表的替代方案（支持有序）。通过手写哈希表，理解“空间换时间”和“冲突处理”，对比跳表的层级索引思想。
*   [352. 将数据流变为多个不相交区间](https://leetcode.cn/problems/data-stream-as-disjoint-intervals/) (中等) *(原题标记为困难，但逻辑属于中等范畴，若严格限制可换为下题)*
    *   **理由**：**滑窗 + 有序集合**。这道题需要一个能快速获取“前驱”和“后继”的数据结构。虽然通常用 `TreeSet` 解，但其核心逻辑正是跳表最擅长的 **范围查找** 和 **邻近值定位**。

### 4. 深度优先 (DFS) 与 广度优先 (BFS)
*   [200. 岛屿数量](https://leetcode.cn/problems/number-of-islands/) (中等)
    *   **理由**：**DFS 代表**。网格搜索的经典模板，考察递归淹没、visited 标记，适用于所有连通块问题。
*   [102. 二叉树的层序遍历](https://leetcode.cn/problems/binary-tree-level-order-traversal/) (中等)
    *   **理由**：**BFS 代表**。队列使用的标准模板。相比单词接龙，这道题更纯粹地考察 BFS 的 **分层处理** 逻辑，是图论最短路径的基础。

---