package cn.juntaozhang.leetcode.dp;

public class L97 {
    public boolean isInterleave(String s1, String s2, String s3) {
        char[] c1 = s1.toCharArray(), c2 = s2.toCharArray(), c3 = s3.toCharArray();
        int n1 = c1.length, n2 = c2.length, n3 = c3.length;
        boolean[][] dp = new boolean[n2 + 1][n1 + 1];
        dp[0][0] = true;
        if (n3 != n1 + n2) return false;
        for (int i = 1; i <= n2; i++) {
            dp[i][0] = dp[i - 1][0] && c2[i - 1] == c3[i - 1];
        }
        for (int j = 1; j <= n1; j++) {
            dp[0][j] = dp[0][j - 1] && c1[j - 1] == c3[j - 1];
        }
        for (int i = 1; i <= n2; i++) {
            for (int j = 1; j <= n1; j++) {
                dp[i][j] = (dp[i - 1][j] && c2[i - 1] == c3[i + j - 1]) || (dp[i][j - 1] && c1[j - 1] == c3[i + j - 1]);
            }
        }
        return dp[n2][n1];
    }

    public static void main(String[] args) {
        System.out.println(new L97().isInterleave("aabcc", "dbbca", "aadbbcbcac"));// true
        System.out.println(new L97().isInterleave("a", "", "a"));// true
    }
}
