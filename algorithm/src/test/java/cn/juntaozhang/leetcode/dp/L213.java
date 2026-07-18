package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L213 {

    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 1) {
            return nums[0];
        }
        return Math.max(
                rob(nums, 0, n - 1),
                rob(nums, 1, n)
        );
    }

    public int rob(int[] nums, int start, int end) {
        int n = end - start;
        int pre1 = 0;
        int pre2 = nums[start];
        start++;
        while (start < end) {
            int t = pre2;
            pre2 = Math.max(pre1 + nums[start], pre2);
            pre1 = t;
            start++;
        }
        return pre2;
    }

    @Test
    public void case1() {
        System.out.println(rob(new int[]{2, 3, 2}));
        System.out.println(rob(new int[]{2, 7, 9, 3, 1}));
    }
}
