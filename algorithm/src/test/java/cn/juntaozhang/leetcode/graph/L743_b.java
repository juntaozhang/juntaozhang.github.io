package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
class L743_b {

    boolean[] visit;
    int[] distance;
    Map<Integer, List<int[]>> map = new HashMap<>();
    Deque<Integer> q = new ArrayDeque<>();

    public int networkDelayTime(int[][] times, int n, int k) {
        visit = new boolean[n + 1];
        distance = new int[n + 1];
        Arrays.fill(distance, -1);
        for (int[] time : times) {
            map.compute(time[0], (_k, v) -> {
                if (v == null) {
                    v = new ArrayList<int[]>();
                }
                v.add(time);
                return v;
            });
        }

        int node = k;
        visit[node] = true;
        distance[node] = 0;
        q.offerLast(node);

        while (!q.isEmpty()) {
            node = q.pollFirst();
            resetDistance(node);
            node = findMinNode();
            if (node == -1) {
                return -1;
            }
            visit[node] = true;
        }

        int d = distance[1];
        for (int i = 2; i < distance.length; i++) {
            if (distance[i] == -1) {
                return -1;
            }
            d = Math.max(d, distance[i]);
        }
        return d;
    }

    private void resetDistance(int k) {
        List<int[]> times = map.get(k);
        if (times == null) {
            return;
        }
        int node, d;
        for (int[] time : times) {
            node = time[1];
            d = time[2] + distance[k];
            if (distance[node] == -1 || d < distance[node]) {
                distance[node] = d;
            }
        }
    }

    private int findMinNode() {
        int node = -1;
        int minDistance = -1;
        for (int i = 1; i < distance.length; i++) {
            if (visit[i] && (minDistance == -1 || minDistance < distance[i])) {
                node = i;
                minDistance = distance[i];
            }
        }
        return node;
    }

    public static void main(String[] args) {
        System.out.println(new L743().networkDelayTime(new int[][]{
                {2, 1, 1},
                {2, 3, 1},
                {3, 4, 1}
        }, 4, 2));
    }
}

