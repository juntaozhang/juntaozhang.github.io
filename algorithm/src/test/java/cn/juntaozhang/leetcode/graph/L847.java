package cn.juntaozhang.leetcode.graph;

import java.util.Arrays;

/**
 * @author juntzhang
 */
public class L847 {

    public int shortestPathLength(int[][] graph) {
        int n = graph.length;
        int[][] d = new int[n][n];
        for (int i = 0; i < n; ++i) {
            Arrays.fill(d[i], n + 1);
        }
        for (int i = 0; i < n; ++i) {
            for (int j : graph[i]) {
                d[i][j] = 1;
            }
        }
        // 使用 floyd 算法预处理出所有点对之间的最短路径长度
        for (int k = 0; k < n; ++k) {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
                }
            }
        }

        int[][] f = new int[n][1 << n];
        for (int i = 0; i < n; ++i) {
            Arrays.fill(f[i], Integer.MAX_VALUE / 2);
        }
        for (int mask = 1; mask < (1 << n); ++mask) {
            // 如果 mask 只包含一个 1，即 mask 是 2 的幂
            if ((mask & (mask - 1)) == 0) {
                int u = Integer.bitCount((mask & (-mask)) - 1);
                f[u][mask] = 0;
            } else {
                for (int u = 0; u < n; ++u) {
                    if ((mask & (1 << u)) != 0) {
                        for (int v = 0; v < n; ++v) {
                            if ((mask & (1 << v)) != 0 && u != v) {
                                f[u][mask] = Math.min(f[u][mask], f[v][mask ^ (1 << u)] + d[v][u]);
                            }
                        }
                    }
                }
            }
        }

        int ans = Integer.MAX_VALUE;
        for (int u = 0; u < n; ++u) {
            ans = Math.min(ans, f[u][(1 << n) - 1]);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L847().shortestPathLength2(new int[][]{
                {1}, {0, 2, 4}, {1, 3, 4}, {2}, {1, 2}
        }));
    }

    public int shortestPathLength2(int[][] graph) {
        int n = graph.length;
        int[][] f = new int[n][1 << n];
        int[][] d = new int[n][n];
        int fullMask = (1 << n) - 1;
        for (int i = 0; i < n; i++) {
            Arrays.fill(d[i], n + 1);
        }
        for (int i = 0; i < n; ++i) {
            for (int j : graph[i]) {
                d[i][j] = 1;
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
                }
            }
        }

        for (int u = 0; u < n; u++) {
            Arrays.fill(f[u], 10000000);
            f[u][1 << u] = 0;
        }

        for (int mask = 1; mask < (1 << n); mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask - (1 << u)) != 0 && (mask & (1 << u)) != 0) {
                    for (int v = 0; v < n; v++) {
                        if ((mask & (1 << v)) != 0 && u != v) {
                            f[u][mask] = Math.min(f[u][mask], f[v][mask ^ (1 << u)] + d[u][v]);
                        }
                    }
                }
            }
        }
        int ans = f[0][fullMask];
        for (int u = 0; u < n; u++) {
            ans = Math.min(ans, f[u][fullMask]);
        }
        return ans;
    }
}
