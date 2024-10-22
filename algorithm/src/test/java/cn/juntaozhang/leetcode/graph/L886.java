package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L886 {

    Set<Integer> group1 = new HashSet<>();
    Set<Integer> group2 = new HashSet<>();
    Deque<Integer> q = new ArrayDeque<>();
    Map<Integer, List<Integer>> edges = new HashMap<>();

    public boolean possibleBipartition(int n, int[][] dislikes) {
        for (int[] dislike : dislikes) {
            final int a = dislike[0], b = dislike[1];

            edges.compute(a, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(b);
                return v;
            });

            edges.compute(b, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(a);
                return v;
            });
        }

        for (int i = 1; i <= n; i++) {
            if (!group1.contains(i) && !group2.contains(i) && !dfs(i)) {
                return false;
            }
        }

        return true;
    }

    public boolean dfs(int node) {
        q.offerLast(node);
        group1.add(node);
        while (!q.isEmpty()) {
            int a = q.pollFirst();
            if (edges.get(a) != null) {
                for (int b : edges.get(a)) {
                    if (group1.contains(a)) {
                        // a in group1
                        if (group1.contains(b)) {
                            // if b also in group1 then break
                            return false;
                        }
                        // b must in group2
                        if (!group2.contains(b)) {
                            group2.add(b);
                            q.offerLast(b);
                        }
                    } else if (group2.contains(a)) {
                        // a in group2
                        if (group2.contains(b)) {
                            // if a also in group2 then break
                            return false;
                        }
                        // b must in group1
                        if (!group1.contains(b)) {
                            group1.add(b);
                            q.offerLast(b);
                        }
                    }
                }
            }

        }

        return true;
    }


    public static void main(String[] args) {
        System.out.println(new L886().possibleBipartition(4, new int[][]{
                {1, 2},
                {1, 3},
                {2, 4}
        }));
    }
}
