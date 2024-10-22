package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.*;

public class L310 {
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, List<Integer>> adj = new HashMap<>();
        int[] parent = new int[n];
        buildAdj(edges, adj);
        int x = findLongestNode(0, adj, parent);
        Arrays.fill(parent, -1);
        int y = findLongestNode(x, adj, parent);
        List<Integer> paths = findPath(x, y, parent);
        result.add(paths.get(paths.size() / 2));
        if (paths.size() % 2 == 0) {
            result.add(paths.get((paths.size() - 1) / 2));
        }
        return result;
    }

    private void buildAdj(int[][] edges, Map<Integer, List<Integer>> adj) {
        for (int[] edge : edges) {
            if (adj.get(edge[0]) == null) {
                adj.put(edge[0], new ArrayList<Integer>());
            }
            adj.get(edge[0]).add(edge[1]);
            if (adj.get(edge[1]) == null) {
                adj.put(edge[1], new ArrayList<Integer>());
            }
            adj.get(edge[1]).add(edge[0]);
        }
    }

    private int findLongestNode(int i, Map<Integer, List<Integer>> adj, int[] parent) {
        Queue<Integer> q = new LinkedList<>();
        boolean[] visited = new boolean[parent.length];
        q.offer(i);
        int node = -1;
        while (!q.isEmpty()) {
            int curr = q.poll();
            visited[curr] = true;
            node = curr;
            if (adj.get(curr) != null) {
                for (Integer j : adj.get(curr)) {
                    if (!visited[j]) {
                        q.offer(j);
                        parent[j] = curr;
                    }
                }
            }
        }
        return node;
    }

    private List<Integer> findPath(int i, int j, int[] parent) {
        List<Integer> paths = new ArrayList<>();
        while (parent[j] != -1) {
            paths.add(j);
            j = parent[j];
        }
        paths.add(i);
        return paths;
    }

    @Test
    public void case1() {
        List<Integer> res = findMinHeightTrees(4, new int[][]{{1, 0}, {1, 2}, {1, 3}});
        System.out.println(res);
    }
}
