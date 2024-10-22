package cn.juntaozhang.leetcode.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juntzhang
 */
public class L1319_dfs {

    public int makeConnected(int n, int[][] connections) {
        if (connections.length < n - 1) {
            return -1;
        }
        Map<Integer, List<Integer>> graph = new HashMap<>();
        boolean[] vist = new boolean[n];
        for (int[] c : connections) {
            graph.compute(c[0], (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(c[1]);
                return v;
            });
            graph.compute(c[1], (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(c[0]);
                return v;
            });
        }

        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!vist[i]) {
                vist[i] = true;
                count++;
                dfs(graph, vist, i);
            }
        }

        return count - 1;
    }

    private void dfs(Map<Integer, List<Integer>> graph, boolean[] vist, int i) {
        if (graph.get(i) == null) {
            return;
        }
        for (int j : graph.get(i)) {
            if (!vist[j]) {
                vist[j] = true;
                dfs(graph, vist, j);
            }
        }
    }

    public static void main(String[] args) {

    }
}
