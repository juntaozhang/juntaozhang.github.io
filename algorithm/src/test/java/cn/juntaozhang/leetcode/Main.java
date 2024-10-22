package cn.juntaozhang.leetcode;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author juntzhang
 */
public class Main {
    public static void main(String[] args) {
        TreeMap<String, String> tree = new TreeMap<>((s1, s2) -> s1.compareTo(s2));
        tree.put("1", "a1");
        tree.put("3", "a3");
        tree.put("2", "a2");
        for (Map.Entry<String, String> e : tree.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }

        int[][] points = new int[][]{
                {2147483646, 2147483647},
                {-2147483646, -2147483645}
        };
        Arrays.sort(points, (a1, a2) -> {
            if (a1[0] == a2[0]) {
                return a1[1] > a2[1] ? 1 : -1;
            } else {
                return a1[0] > a2[0] ? 1 : -1;
            }
        });
        System.out.println(points);
    }
}
