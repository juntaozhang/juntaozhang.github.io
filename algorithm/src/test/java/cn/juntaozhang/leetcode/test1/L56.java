package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

public class L56 {
    public int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        LinkedList<int[]> list = new LinkedList<>();
        list.add(intervals[0]);
        for (int i = 1; i < intervals.length; i++) {
            int[] t1 = list.peekLast();
            int[] t2 = intervals[i];
            if (t1[1] >= t2[0]) {
                if (t1[1] < t2[1]) t1[1] = t2[1];
            } else {
                list.add(t2);
            }
        }

        int i = 0;
        int[][] result = new int[list.size()][2];
        while (!list.isEmpty()) {
            result[i++] = list.poll();
        }
        return result;
    }

    @Test
    public void case1() {
        int[][] intervals = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        int[][] result = merge(intervals);
        Assert.assertArrayEquals(new int[][]{{1, 6}, {8, 10}, {15, 18}}, result);
    }

    @Test
    public void case2() {
        int[][] intervals = {{4, 7}, {1, 4}};
        int[][] result = merge(intervals);
        Assert.assertArrayEquals(new int[][]{{1, 7}}, result);
    }

    @Test
    public void case3() {
        int[][] intervals = {{1, 4}, {2, 3}};
        int[][] result = merge(intervals);
        Assert.assertArrayEquals(new int[][]{{1, 4}}, result);
    }
}
