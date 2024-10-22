package cn.juntaozhang.leetcode.dp;

import java.util.TreeSet;

public class L53 {
    public int maxSubArray(int[] nums) {
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int res = dp[0];

        for (int i = 1; i < nums.length; i++) {
            if (dp[i - 1] > 0 && dp[i - 1] + nums[i] > 0) {
                dp[i] = dp[i - 1] + nums[i];
            } else {
                dp[i] = nums[i];
            }
            res = Math.max(res, dp[i]);
        }
        return res;
    }

    public static int maxSubArray2(int[] nums) {
        int res = nums[0];
        int cur = nums[0];
        for (int i = 1; i < nums.length; i++) {
            if (cur < 0) {
                cur = nums[i];
            } else {
                cur += nums[i];
            }
            res = Math.max(res, cur);
        }
        return res;
    }

    public static int maxSumSubarray(int[] nums, int k) {
        TreeSet<Integer> sumSet = new TreeSet<>();
        sumSet.add(0);
        int s = 0;
        int ans = Integer.MIN_VALUE;
        for (int v : nums) {
            s += v;
            Integer ceil = sumSet.ceiling(s - k);
            if (ceil != null) {
                ans = Math.max(ans, s - ceil);
            }
            sumSet.add(s);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(maxSumSubarray(new int[]{7}, 5));
        System.out.println(maxSumSubarray(new int[]{4, 3, -1, -7, -9, 6, 2, -7}, 8));
        System.out.println(maxSubArray2(new int[]{4, 3, -1, -7, -9, 6, 2, -7}));

    }
}
