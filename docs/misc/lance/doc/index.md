# Vector Index


|            | ANN (Approximate Nearest Neighbor) | KNN (K-Nearest Neighbor) |
| ---------- | ---------------------------------- | ------------------------ |
| 中文       | 近似最近邻                         | 精确最近邻               |
| 算法       | 使用索引（IVF_PQ 等）              | 暴力扫描                 |
| 精度       | 近似，可能遗漏                     | 100% 精确                |
| 速度       | 快                                 | 慢                       |
| 适用数据量 | 大数据（百万级以上）               | 小数据（万级以下）       |
| 资源消耗   | 内存中缓存索引                     | 全表扫描                 |

## KNN

### HNSW

Hierarchical Navigable Small World

HNSW：多层图 + 贪心导航（近邻传播）

HNSW 多层图结构: 查询时从顶层开始，逐层下降，每层找到最近邻进入下一层

```text
  Layer 2 (最稀疏):         o────────o
                          /          \
  Layer 1 (中等):     o───o───o──────o───o
                     / \   \   \    /   /
  Layer 0 (最密集): o─o─o─o─o─o─o─o─o─o─o─o─o─o
                   ↑ 所有数据点都在这层
```

#### 查询过程

```text

  查询 Q(6, 5)，找最近邻

  Step 1: 从顶层随机入口开始
    Layer 2: 入口=E
      计算距离：Q-E=1.4, Q-H=7.1
      最近=E，进入下一层

  Step 2: Layer 1，从 E 开始
      E 的邻居：B, H, G
      计算距离：Q-B=4.2, Q-E=1.4, Q-G=5.7, Q-H=7.1
      最近=E，但检查 E 的邻居是否有更近
      → 没有，进入下一层

  Step 3: Layer 0，从 E 开始
      E 的邻居：B, D, F, G, H
      计算距离：Q-D=1.0, Q-F=1.0, Q-E=1.4
      最近=D 或 F

      检查 D 的邻居：B, E
      检查 F 的邻居：E
      → D 和 F 是最近邻

  结果：D(5,5) 和 F(7,5) 是 Top-2
```

#### 为什么会内存高？




## ANN

### IVF
[ivf-pq](ivf-pq.md)


### IVF_HNSW_FLAT
IVF（粗量化） + HNSW（分区内部搜索） + FLAT（原始向量）

查询流程: 查询 Q
1. IVF 层：选分区（近似！）
    - 计算 Q 到所有 centroid 的距离
    - 选最近的 nprobes 个分区（如 nprobes=2）

2. 每个选中分区内：
    - 用 HNSW 图索引搜索
    - 从入口点开始，贪心导航
    - 找到该分区内最近的 k 个

3. 合并所有分区结果：
    - 精确计算原始向量距离（FLAT）
    - 取全局 Top-K


