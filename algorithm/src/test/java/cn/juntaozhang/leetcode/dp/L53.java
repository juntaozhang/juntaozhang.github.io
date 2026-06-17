package cn.juntaozhang.leetcode.dp;

import org.junit.Test;


// L363/L560 和不超过 k 的最大连续子数组和
public class L53 {
    // 贪心
    public int maxSubArray1(int[] nums) {
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

    // dp
    public int maxSubArray2(int[] nums) {
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int res = dp[0];

        for (int i = 1; i < nums.length; i++) {
            dp[i] = Math.max(dp[i - 1] + nums[i], nums[i]);
            res = Math.max(res, dp[i]);
        }
        return res;
    }

    // 优化后 dp
    public int maxSubArray(int[] nums) {
        int dp = nums[0];
        int result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            dp = Math.max(dp + nums[i], nums[i]);
            result = Math.max(dp, result);
        }
        return result;
    }

    @Test
    public void case1() {
        System.out.println(maxSubArray2(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));
    }

    @Test
    public void case2() {
        System.out.println(maxSubArray(new int[]{4, 3, -1, -7, -9, 6, 2, -7}));
    }
}
