package cn.juntaozhang.leetcode.dp;


import static cn.juntaozhang.utils.StringUtils.print;

/**
 * @author juntzhang
 */
public class L221 {
    public int maximalSquare1(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dp = new int[m + 1][n + 1];
        int ans = 0;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (matrix[i - 1][j - 1] == '1') {
                    dp[i][j] = Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1])) + 1;
                }
                ans = Math.max(ans, dp[i][j]);
            }
        }
        print(dp);
        return ans * ans;
    }

    public int maximalSquare(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dp = new int[m][n];
        int ans = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == '1') {
                    if (i != 0 && j != 0) {
                        dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j])) + 1;
                    } else if (i != 0 && j == 0) {
                        dp[i][j] = 1;
                    } else if (i == 0 && j != 0) {
                        dp[i][j] = 1;
                    } else if (i == 0 && j == 0) {
                        dp[i][j] = 1;
                    }
                }
                ans = Math.max(ans, dp[i][j]);
            }
        }
        return ans * ans;
    }

    public static void main(String[] args) {
        System.out.println(new L221().maximalSquare(new char[][]{
                {'1', '0', '1', '0', '0'},
                {'1', '0', '1', '1', '1'},
                {'1', '1', '1', '1', '1'},
                {'1', '0', '0', '1', '0'}
        }));
    }
}
