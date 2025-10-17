package cn.juntaozhang.leetcode.greedy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class L56 {
    public int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> {
            if (a[0] != b[0]) {
                return a[0] - b[0];
            } else {
                return a[1] - b[1];
            }
        });
        List<int[]> list = new ArrayList<>(intervals.length);
        int[] left = intervals[0];
        list.add(left);
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] <= left[1]) {
                left[1] = Math.max(intervals[i][1], left[1]);
            } else {
                left = intervals[i];
                list.add(left);
            }
        }
        return list.toArray(new int[0][0]);
    }

    public static void main(String[] args) {
        int[][] a = new L56().merge(new int[][]{
                {2, 2},
                {1, 3},
                {8, 10},
                {15, 18}
        });
        System.out.println(a);
    }
}
