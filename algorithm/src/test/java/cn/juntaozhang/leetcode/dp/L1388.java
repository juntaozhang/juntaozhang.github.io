package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;


/**
 * @author juntzhang
 */
public class L1388 {

    public int maxSizeSlices(int[] slices) {
        int n = slices.length;
        int[][] dp = new int[n][n];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = i; j < n; j++) {
                dp[i][j] = max(dp, i, j, slices);
            }
        }
        StringUtils.print(slices);
        StringUtils.print(dp);
        return dp[0][n - 1];
    }

    int max(int[][] dp, int i, int j, int[] slices) {
        if (i == j) {
            return slices[i];
        }
        // i < j
        int ans = Math.max(dp[i][j - 1], dp[i + 1][j]);
        if (i + 1 <= j - 2) {
            // [i, j - 1]
            ans = Math.max(ans, dp[i + 1][j - 2] + slices[j]);
        }
        if (i + 2 <= j - 1) {
            // [i + 1, j]
            ans = Math.max(ans, dp[i + 2][j - 1] + slices[i]);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L1388().maxSizeSlices(new int[]{4, 8, 8, 8, 10, 3, 9, 10, 7, 9, 4, 5, 4, 3, 1}));
//        System.out.println(new L1388().maxSizeSlices(new int[]{8, 9, 8, 6, 1, 1}));
    }
}
