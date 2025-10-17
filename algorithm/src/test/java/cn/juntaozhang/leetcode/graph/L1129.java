package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L1129 {

    public int[] shortestAlternatingPaths(int n, int[][] redEdges, int[][] blueEdges) {
        // 构建邻接矩阵
        Map<Integer, List<Integer>> redMap = new HashMap<>();
        Map<Integer, List<Integer>> blueMap = new HashMap<>();

        for (int[] e : redEdges) {
            redMap.compute(e[0], (k, v) -> {
                if (v == null) {
                    v = new ArrayList<Integer>();
                }
                v.add(e[1]);
                return v;
            });
        }

        for (int[] e : blueEdges) {
            blueMap.compute(e[0], (k, v) -> {
                if (v == null) {
                    v = new ArrayList<Integer>();
                }
                v.add(e[1]);
                return v;
            });
        }

        Deque<int[]> q = new ArrayDeque<>();
        // node, length, color(0:red, 1:blue)
        q.offerLast(new int[]{0, 0, 0});
        q.offerLast(new int[]{0, 0, 1});
        int[] ans = new int[n];
        boolean[] rVisted = new boolean[n];
        boolean[] bVisted = new boolean[n];
        rVisted[0] = true;
        bVisted[0] = true;
        Arrays.fill(ans, -1);
        // BFS
        while (!q.isEmpty()) {
            int[] t = q.pollFirst();
            int node = t[0], len = t[1], color = t[2];
            if (ans[node] == -1) {
                ans[node] = len;
            }

            if (color == 0) {
                // next is blue
                if (blueMap.get(node) != null) {
                    for (int next : blueMap.get(node)) {
                        if (!bVisted[next]) {
                            q.offerLast(new int[]{next, len + 1, 1});
                            bVisted[next] = true;
                        }
                    }
                }
            } else {
                // next is red
                if (redMap.get(node) != null) {
                    for (int next : redMap.get(node)) {
                        if (!rVisted[next]) {
                            q.offerLast(new int[]{next, len + 1, 0});
                            rVisted[next] = true;
                        }

                    }
                }
            }

        }

        return ans;
    }

    public static void main(String[] args) {
        new L1129().shortestAlternatingPaths(5, new int[][]{

                {0, 1}, {1, 2}, {2, 3}, {3, 4}

        }, new int[][]{
                {1, 2}, {2, 3}, {3, 1}
        });
    }
}
