package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L300 {
    public int lengthOfLIS(int[] nums) {
        int[] dp = new int[nums.length];
        dp[0] = 1;
        int result = 1;
        for (int i = 1; i < nums.length; i++) {
            int t = 0;
            for (int j = i - 1; j >= 0; j--) {
                if (nums[i] > nums[j]) {
                    t = Math.max(dp[j], t);
                }
            }
            dp[i] = t + 1;
            result = Math.max(result, dp[i]);
        }
        return result;
    }

    @Test
    public void case1() {
        System.out.println(lengthOfLIS(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
    }

    @Test
    public void case2() {
        System.out.println(lengthOfLIS(new int[]{0, 1, 0, 3, 2, 3}));
    }

    @Test
    public void case3() {
        System.out.println(lengthOfLIS(new int[]{7, 7, 7, 7, 7, 7, 7}));
        System.out.println(lengthOfLIS(new int[]{0}));
    }
}
