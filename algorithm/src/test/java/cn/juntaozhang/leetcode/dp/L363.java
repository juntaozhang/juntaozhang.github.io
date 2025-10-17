package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
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

    int maxSubarraySum(int[] nums, int k) {
        int n = nums.length;
        int[] dp = new int[n];
        dp[0] = nums[0];
        for (int i = 1; i < n; i++) {
            dp[i] = dp[i - 1] + nums[i];
        }
        int ans = dp[k - 1];
        for (int i = k; i < n; i++) {
            ans = Math.max(ans, dp[i] - dp[i - k]);
        }
        return ans;
    }

    public static void main(String[] args) {

        System.out.println(new L363().maxSumSubmatrix(new int[][]{
                {1, 0, 1},
                {0, -2, 3}
        }, 2));
        System.out.println(new L363().maxSubarraySum(new int[]{1, -2, 4}, 3));
        System.out.println(new L363().maxSubarraySum(new int[]{1, -2, -4}, 2));
    }
}
