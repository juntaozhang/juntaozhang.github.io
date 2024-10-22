package cn.juntaozhang.leetcode.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author juntzhang
 */
public class DijkstraTest {


    public static void main(String[] args) {
        int w = Integer.MAX_VALUE;
        int n = 4;
        int[][] path = new int[][]{
                {0, 2, 1, 6},
                {2, 0, 3, 2},
                {1, 3, 0, 2},
                {6, 2, 2, 0}
        };

        int[] a = path[0];
        Set<Integer> set = new HashSet<>() {{
            add(1);
            add(2);
            add(3);
        }};

        for (int i = 0; i < n; i++) {
            int k = set.iterator().next(), v = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (set.contains(j) && j != i) {
                    if (path[i][j] < v) {
                        v = path[i][j];
                        k = j;
                    }
                }
            }
            set.remove(k);
            if (set.isEmpty()) {
                break;
            }
            for (int s : set) {
                a[s] = Math.min(a[s], a[k] + path[k][s]);
            }

        }

        System.out.println(Arrays.toString(a));

    }
}
