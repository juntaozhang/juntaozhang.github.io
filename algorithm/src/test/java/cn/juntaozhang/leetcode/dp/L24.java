package cn.juntaozhang.leetcode.dp;

import java.util.Arrays;

/**
 * @author juntzhang
 */
public class L24 {
    public int[] getMaxMatrix(int[][] matrix) {
        int n = matrix.length, m = matrix[0].length;
        int[][] dp = new int[n + 1][m];

        int max = dp[1][0] = matrix[0][0];
        int[] ans = new int[]{0, 0, 0, 0};

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < m; j++) {
                dp[i][j] = dp[i - 1][j] + matrix[i - 1][j];
            }
        }

        int[] nums = new int[m];
        for (int i = 0; i <= n - 1; i++) {
            for (int k = i + 1; k <= n; k++) {
                for (int j = 0; j < m; j++) {
                    nums[j] = dp[k][j] - dp[i][j];
                }
                int[] arr = maxSubArray2(nums);
                if (arr[0] > max) {
                    max = arr[0];
                    ans[0] = i;  // r1
                    ans[1] = arr[1]; // c1
                    ans[2] = k - 1;      // r2
                    ans[3] = arr[2];  // c2
                }
            }
        }

        return ans;
    }

    public int[] getMaxMatrix2(int[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        int[] pos = new int[]{0, 0, 0, 0};
        int ans = matrix[0][0];
        for (int i1 = 0; i1 < n; i1++) {
            int[] sum = new int[m];
            for (int i2 = i1; i2 < n; i2++) {
                for (int j = 0; j < m; j++) {
                    sum[j] += matrix[i2][j];
                }
                int[] arr = maxSubArray2(sum);
                if (arr[0] > ans) {
                    ans = arr[0];
                    pos = new int[]{i1, arr[1], i2, arr[2]};
                }
            }
        }
        return pos;
    }

    public int[] maxSubArray2(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        int[] idx = new int[n];
        dp[0] = nums[0];
        idx[0] = 0;
        int[] ans = {dp[0], 0, 0};
        for (int i = 1; i < n; i++) {
            if (dp[i - 1] < 0) {
                idx[i] = i;
                dp[i] = nums[i];
            } else {
                dp[i] = dp[i - 1] + nums[i];
                idx[i] = idx[i - 1];
            }
            if (dp[i] > ans[0]) {
                ans[0] = dp[i];
                ans[1] = idx[i];
                ans[2] = i;
            }
        }
        return ans;
    }

    public int maxSubArray(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        dp[0] = nums[0];
        int res = dp[0];

        for (int i = 1; i < n; i++) {
            if (dp[i - 1] > 0 && dp[i - 1] + nums[i] > 0) {
                dp[i] = dp[i - 1] + nums[i];
            } else {
                dp[i] = nums[i];
            }
            res = Math.max(res, dp[i]);
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new L24().getMaxMatrix2(new int[][]{
                {9, -8, 1, 3, -2},
                {-3, 7, 6, -2, 4},
                {6, -4, -4, 8, -7}
        })));
        System.out.println(Arrays.toString(new L24().getMaxMatrix2(new int[][]{{-1, 0}, {0, -1}})));
        System.out.println(Arrays.toString(new L24().maxSubArray2(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4})));
        System.out.println(Arrays.toString(new L24().maxSubArray2(new int[]{-2, -2, -3})));
    }
}
