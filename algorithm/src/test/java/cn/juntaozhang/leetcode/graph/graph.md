## 200 岛屿数量

流程：

1. 遍历网格，遇到未访问陆地就计数 + 1；
2. dfs/bfs 把整片连通陆地全部标记为已访问；

算法：

- bfs: 使用 LinkedList<int[]> queue = new LinkedList<>();
- dfs 非递归: 使用 LinkedList<int[]> stack = new LinkedList<>();
- FIFO 是 bfs， First in Last out 是 dfs

[code](https://leetcode.cn/problems/number-of-islands/submissions/732145123/)：

- [695. 岛屿的最大面积](https://leetcode.cn/problems/max-area-of-island/submissions/732147811/)
- [L200_dfs.java](dfs_bfs/L200_dfs.java)
- [L200_bfs.java](dfs_bfs/L200_bfs.java)
- [L695.java](dfs_bfs/L695.java)


## 210. 课程表 II
把所有入度 = 0 的节点入队（无前置课，可直接学）