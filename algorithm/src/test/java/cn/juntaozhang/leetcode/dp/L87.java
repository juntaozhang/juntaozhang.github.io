package cn.juntaozhang.leetcode.dp;

public class L87 {
    Boolean[][][] dp;
    char[] c1;
    char[] c2;
    int n;

    public boolean isScramble(String s1, String s2) {
        c1 = s1.toCharArray();
        c2 = s2.toCharArray();
        if (c1.length != c2.length) return false;
        n = c1.length;
        dp = new Boolean[n][n][n];
        return dfs(0, 0, n);
    }

    public boolean dfs(int i, int j, int n) {
        if (dp[i][j][n - 1] != null) {
            return dp[i][j][n - 1];
        }

        if (n == 1) {
            dp[i][j][n - 1] = c1[i] == c2[j];
            return dp[i][j][n - 1];
        }

        dp[i][j][n - 1] = false;
        for (int k = 1; k < n; k++) {
            dp[i][j][n - 1] = (dfs(i, j, k) && dfs(i + k, j + k, n - k)) || dfs(i, j + n - k, k) && dfs(i + k, j, n - k);
            if (dp[i][j][n - 1]) {
                break;
            }
        }
        return dp[i][j][n - 1];
    }

    public static void main(String[] args) {
        System.out.println(new L87().isScramble("great", "rgeat"));
    }
}
