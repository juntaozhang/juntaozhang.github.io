package cn.juntaozhang.leetcode;

/**
 * 戳气球
 * https://zxi.mytechroad.com/blog/dynamic-programming/leetcode-312-burst-balloons/
 *
 * 
 */
public class L312_burst_balloons {
    /**
     * 状态方式
     * dp[1][n]
     * dp[i][j] = max{ dp[i][k-1] + nums[i-1]*nums[k]*nums[j+1] + dp[k+1][j] }
     */
    class Solution {

        public int maxCoins(int[] nums) {
            int n = nums.length;
            int[] vals = new int[n + 2];
            System.arraycopy(nums, 0, vals, 1, n);
            vals[0] = vals[n + 1] = 1;
            int[][] dp = new int[n + 2][n + 2];

            for (int l = 1; l <= n; l++) {
                for (int i = 1; i + l < vals.length; i++) {
                    int j = i + l - 1;
                    for (int k = i; k <= j; k++) {
                        dp[i][j] = Math.max(dp[i][j], dp[i][k - 1] + vals[i - 1] * vals[k] * vals[j + 1] + dp[k + 1][j]);
                    }
                }
            }
            return dp[1][n];
        }
    }

    public static void main(String[] args) {
        Solution s = new L312_burst_balloons().new Solution();
        System.out.println(s.maxCoins(new int[]{3, 1, 5, 8}));
    }
}
