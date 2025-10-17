package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L802_v2 {

    public Map<Integer, List<Integer>> outMap = new HashMap<>();
    public Map<Integer, List<Integer>> inMap = new HashMap<>();
    Deque<Integer> queue = new ArrayDeque<>();

    public List<Integer> eventualSafeNodes(int[][] graph) {
        List<Integer> ans = new ArrayList<>();

        for (int i = 0; i < graph.length; i++) {
            int finalI = i;
            outMap.put(finalI, new ArrayList<>());
            for (int j : graph[i]) {
                outMap.compute(i, (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(j);
                    return v;
                });

                inMap.compute(j, (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(finalI);
                    return v;
                });
            }
        }
        while (true) {
            if (queue.isEmpty()) {
                if (fillZeroOutDegree()) {
                    break;
                }
            }
            Integer node = queue.pollFirst();
            ans.add(node);
            outMap.remove(node);
            if (inMap.get(node) != null) {
                inMap.get(node).forEach(v -> {
                    outMap.get(v).remove(node);
                });
            }
        }

        ans.sort((o1, o2) -> o1 - o2);
        return ans;
    }

    private boolean fillZeroOutDegree() {
        outMap.forEach((k, v) -> {
            if (v.size() == 0) {
                queue.offerLast(k);
            }
        });
        return queue.isEmpty();
    }

    public static void main(String[] args) {
//        System.out.println(new L802_v2().eventualSafeNodes(new int[][]{
//                {1, 2, 3, 4}, {1, 2}, {3, 4}, {0, 4}, {}
//        }));
        System.out.println(new L802_v2().eventualSafeNodes(new int[][]{
                {1, 2}, {2, 3}, {5}, {0}, {5}, {}, {}
        }));
    }
}
