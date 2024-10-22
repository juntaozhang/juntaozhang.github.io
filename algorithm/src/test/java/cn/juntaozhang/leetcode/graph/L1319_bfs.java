package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L1319_bfs {

    private Deque<Integer> queue = new ArrayDeque<>();

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
                queue.offerLast(i);
                count++;
                bfs(graph, vist);
            }
        }

        return count - 1;
    }

    private void bfs(Map<Integer, List<Integer>> graph, boolean[] vist) {
        while (!queue.isEmpty()) {
            Integer i = queue.pollFirst();
            if (i == null || graph.get(i) == null) {
                return;
            }
            for (int j : graph.get(i)) {
                if (!vist[j]) {
                    vist[j] = true;
                    queue.offerLast(j);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new L1319_bfs().makeConnected(11, new int[][]{
                {1, 4}, {0, 3}, {1, 3}, {3, 7}, {2, 7}, {0, 1}, {2, 4}, {3, 6}, {5, 6}, {6, 7}, {4, 7}, {0, 7}, {5, 7}
        }));
    }
}
