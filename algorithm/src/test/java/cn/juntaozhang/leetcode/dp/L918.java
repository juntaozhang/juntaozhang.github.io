package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L918 {

    public int maxSubarraySumCircular(int[] nums) {
        int n = nums.length;
        int case1 = maxSubarraySum(nums);
        int sum = 0;
        int[] nums2 = new int[n];
        for (int i = 0; i < n; i++) {
            nums2[i] = -nums[i];
            sum += nums[i];

        }
        int case2 = sum + maxSubarraySum(nums2);
        if (case2 == 0) {
            return case1;
        }
        return Math.max(case1, case2);
    }

    int maxSubarraySum(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        dp[0] = nums[0];
        int ans = dp[0];
        for (int i = 1; i < n; i++) {
            dp[i] = Math.max(dp[i - 1], 0) + nums[i];
            ans = Math.max(ans, dp[i]);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L918().maxSubarraySumCircular(new int[]{-3, -2, -3}));
        System.out.println(new L918().maxSubarraySumCircular(new int[]{5, -3, 5}));
    }
}
/*
[
    [9,-8,1,3,-2],
    [-3,7,6,-2,4],
    [6,-4,-4,8,-7]
    ]
 */