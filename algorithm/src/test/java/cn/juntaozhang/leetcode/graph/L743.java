package cn.juntaozhang.leetcode.graph;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author juntzhang
 */
class L743 {

    boolean[] visit;
    int[] distance;
    Map<Integer, List<int[]>> map = new HashMap<>();

    public int networkDelayTime(int[][] times, int n, int k) {
        visit = new boolean[n + 1];
        distance = new int[n + 1];
        Arrays.fill(distance, -1);
        distance[k] = 0;
        visit[k] = true;
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
        for (int i = 0; i < n; i++) {
            resetDistance(node);
            OptionalInt nodeOpt = IntStream.range(1, distance.length).filter(_i -> !visit[_i] && distance[_i] != -1)
                    .reduce((left, right) -> {
                        if (distance[left] - distance[right] < 0) {
                            return left;
                        } else {
                            return right;
                        }
                    });
            if (nodeOpt.isPresent()) {
                visit[nodeOpt.getAsInt()] = true;
                node = nodeOpt.getAsInt();
            }
        }

        int d = distance[1];
        for (int i = 2; i < distance.length; i++) {
            if (d == -1 || distance[i] == -1) {
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

    public static void main(String[] args) {
        System.out.println(new L743().networkDelayTime(new int[][]{
                {2, 7, 63}, {4, 3, 60}, {1, 3, 53}, {5, 6, 100}, {1, 4, 40}, {4, 7, 95}, {4, 6, 97}, {3, 4, 68},
                {1, 7, 75}, {2, 6, 84}, {1, 6, 27}, {5, 3, 25}, {6, 2, 2}, {3, 7, 57}, {5, 4, 2}, {7, 1, 53},
                {5, 7, 35}, {4, 1, 60}, {5, 2, 95}, {3, 5, 28}, {6, 1, 61}, {2, 5, 28}
        }, 7, 3));
        System.out.println(new L743().networkDelayTime(new int[][]{
                {4, 2, 76}, {1, 3, 79}, {3, 1, 81}, {4, 3, 30}, {2, 1, 47}, {1, 5, 61}, {1, 4, 99},
                {3, 4, 68}, {3, 5, 46}, {4, 1, 6}, {5, 4, 7}, {5, 3, 44}, {4, 5, 19}, {2, 3, 13}, {3, 2, 18}, {1, 2, 0},
                {5, 1, 25}, {2, 5, 58}, {2, 4, 77}, {5, 2, 74}
        }, 5, 3));

        System.out.println(new L743().networkDelayTime(new int[][]{
                {2, 1, 1},
                {2, 3, 1},
                {3, 4, 1}
        }, 4, 2));
    }
}
