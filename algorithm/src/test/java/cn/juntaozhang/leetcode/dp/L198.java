package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

/**
 * @author juntzhang
 */
public class L198 {


    public int rob(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n + 1];
        dp[1] = nums[0];
        for (int i = 1; i < n; i++) {
            dp[i + 1] = Math.max(dp[i], dp[i - 1] + nums[i]);
        }
        return dp[n];
    }

    public int rob2(int[] nums) {
        if (nums == null) {
            return 0;
        }
        int res = 0, t1 = 0, t2 = 0, t3;
        for (int i = 0; i < nums.length; i++) {
            if (i - 2 >= 0) {
                t3 = Math.max(nums[i] + t1, t2);
            } else if (i == 1) {
                t3 = Math.max(nums[i], t1);
            } else {
                t3 = nums[i];
            }
            t1 = t2;
            t2 = t3;
            res = Math.max(res, t3);

        }
        return res;
    }


    @Test
    public void case1() {
        System.out.println(rob(new int[]{1, 2, 3, 1}));
        System.out.println(rob(new int[]{2, 7, 9, 3, 1}));
    }
}
