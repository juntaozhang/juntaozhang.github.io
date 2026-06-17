package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

import java.util.TreeSet;

/**
 * 给你一个 m x n 的矩阵 matrix 和一个整数 k ，找出并返回矩阵内部矩形区域的不超过 k 的最大数值和。
 */
public class L363 {

    public int maxSumSubmatrix(int[][] matrix, int K) {
        int n = matrix.length, m = matrix[0].length;
        int[][] dp = new int[n + 1][m];

        int ans = dp[1][0] = matrix[0][0];

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < m; j++) {
                dp[i][j] = dp[i - 1][j] + matrix[i - 1][j];
            }
        }

        int[] nums = new int[m];
        for (int i = 0; i <= n - 1; i++) {
            for (int k = i + 1; k <= n && k - i <= K; k++) {
                for (int j = 0; j < m; j++) {
                    nums[j] = dp[k][j] - dp[i][j];
                }
                ans = Math.max(maxSubarraySum(nums, k - i), ans);
            }
        }

        return ans;
    }


    // 和不超过 k 的最大连续子数组和 L560
    // 前缀和
    public int maxSumSubarray(int[] nums, int k) {
        TreeSet<Integer> sumSet = new TreeSet<>();
        sumSet.add(0);
        int s = 0;
        int ans = Integer.MIN_VALUE;
        for (int v : nums) {
            s += v;
            Integer ceil = sumSet.ceiling(s - k);// sort + 二分查找（Binary Search）
            if (ceil != null) {
                ans = Math.max(ans, s - ceil);
            }
            sumSet.add(s);
        }
        return ans;
    }

    // 找长度正好等于 k 的最大连续子数组和
    // 直接遍历一遍也可以实现，不一定要用前缀和
    int maxSubarraySum(int[] nums, int k) {
        int n = nums.length;
        int[] s = new int[n];
        s[0] = nums[0];
        for (int i = 1; i < n; i++) {
            s[i] = s[i - 1] + nums[i];
        }
        int ans = s[k - 1];
        for (int i = k; i < n; i++) {
            ans = Math.max(ans, s[i] - s[i - k]);
        }
        return ans;
    }


    @Test
    public void case1() {
        System.out.println(maxSumSubmatrix(new int[][]{
                {1, 0, 1},
                {0, -2, 3}
        }, 2));
    }

    @Test
    public void case11() {
        System.out.println(maxSubarraySum(new int[]{1, -2, 4}, 3));
    }

    @Test
    public void case12() {
        System.out.println(maxSubarraySum(new int[]{1, -2, -4}, 2));
    }

    @Test
    public void case13() {
        System.out.println(maxSumSubarray(new int[]{7}, 5));
    }

    @Test
    public void case14() {
        System.out.println(maxSumSubarray(new int[]{4, 3, -1, -7, -9, 6, 2, -7}, 8));
    }
}
