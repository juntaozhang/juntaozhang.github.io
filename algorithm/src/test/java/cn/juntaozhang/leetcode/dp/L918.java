package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * @author juntzhang
 */
public class L918 {
    @Test
    public void case1() {
        int[] nums = {5, -3, 5};
        System.out.println(maxSubarraySumCircular3(nums));
    }

    @Test
    public void case2() {
        int[] nums = {-3, -2, -3};
        System.out.println(maxSubarraySumCircular(nums));
    }

    public int maxSubarraySumCircular2(int[] nums) {
        int[] newNums = new int[nums.length * 2];
        for (int i = 0; i < nums.length; i++) {
            newNums[i] = nums[i];
            newNums[i + nums.length] = nums[i];
        }
        return maxSubarraySum(newNums);
    }

    public int maxSubarraySumCircular3(int[] nums) {// TODO
        int n = nums.length;
        Deque<int[]> queue = new ArrayDeque<int[]>();
        int pre = nums[0], res = nums[0];
        queue.offerLast(new int[]{0, pre});
        for (int i = 1; i < 2 * n; i++) {
            while (!queue.isEmpty() && queue.peekFirst()[0] < i - n) {
                queue.pollFirst();
            }
            pre += nums[i % n];
            res = Math.max(res, pre - queue.peekFirst()[1]);
            while (!queue.isEmpty() && queue.peekLast()[1] >= pre) {
                queue.pollLast();
            }
            queue.offerLast(new int[]{i, pre});
        }
        return res;
    }

    public int maxSubarraySumCircular(int[] nums) {
        int n = nums.length;
        int case1 = maxSubarraySum(nums);
        int sum = 0;

        // 取反计算最大子数组和, 即计算最小子数组和
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
//            if(dp[i - 1] > 0 && dp[i - 1] + nums[i] > 0) {
//                dp[i] = dp[i - 1] + nums[i];
//            } else {
//                dp[i] = nums[i];
//            }
            dp[i] = Math.max(dp[i - 1], 0) + nums[i];
            ans = Math.max(ans, dp[i]);
        }
        return ans;
    }
}
/*
[
    [9,-8,1,3,-2],
    [-3,7,6,-2,4],
    [6,-4,-4,8,-7]
    ]
 */