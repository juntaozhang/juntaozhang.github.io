package cn.juntaozhang.leetcode.dp;

public class L712 {
    public int minimumDeleteSum(String s1, String s2) {
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        int n1 = c1.length, n2 = c2.length;
        int[][] dp = new int[n2 + 1][n1 + 1];
        for (int i = 1; i < n2 + 1; i++) {
            dp[i][0] = dp[i - 1][0] + c2[i - 1];
        }
        for (int j = 1; j < n1 + 1; j++) {
            dp[0][j] = dp[0][j - 1] + c1[j - 1];
        }
        for (int i = 1; i < n2 + 1; i++) {
            for (int j = 1; j < n1 + 1; j++) {
                if (c2[i - 1] == c1[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j] + c2[i - 1], dp[i][j - 1] + c1[j - 1]);
                }
            }
        }
        return dp[n2][n1];
    }

    public static void main(String[] args) {
        System.out.println(new L712().minimumDeleteSum("", ""));
        System.out.println(new L712().minimumDeleteSum("e", ""));
        System.out.println(new L712().minimumDeleteSum("e", "e"));
        System.out.println(new L712().minimumDeleteSum("e", "s"));
        System.out.println(new L712().minimumDeleteSum("sea", "eat"));
        System.out.println(new L712().minimumDeleteSum("delete", "leet"));
    }
}
