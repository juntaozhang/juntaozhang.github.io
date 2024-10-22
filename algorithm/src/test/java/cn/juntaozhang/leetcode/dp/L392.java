package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L392 {

    public boolean isSubsequence(String s, String t) {
        int m = s.length();
        if (m == 0) {
            return true;
        }
        int n = t.length();
        if (n == 0) {
            return false;
        }
        boolean[][] dp = new boolean[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = i; j < n; j++) {
                if (i == 0) {
                    dp[i][j] = s.charAt(i) == t.charAt(j);
                } else {
                    dp[i][j] = (s.charAt(i) == t.charAt(j) && dp[i - 1][j - 1]);
                }
                if (j > i) {
                    dp[i][j] = dp[i][j] || dp[i][j - 1];
                }
            }
        }
        return dp[m - 1][n - 1];
    }

    public static void main(String[] args) {
        System.out.println(new L392().isSubsequence("abc", "ahbgdc"));
    }
}
