package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class L908 {

    public int smallestRangeI(int[] nums, int k) {
        if (nums.length < 2) {
            return 0;
        }
        int max = Arrays.stream(nums).max().getAsInt();
        int min = Arrays.stream(nums).min().getAsInt();
        return Math.max(0, max - k - min - k);
    }

    public static void main(String[] args) {
    }
}
