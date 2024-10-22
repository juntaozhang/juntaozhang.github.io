package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;

/**
 * @author juntzhang
 */
public class L132 {
    public int minCut(String s) {
        int n = s.length();
        boolean[][] palindrome = new boolean[n][n];
        int[] dp = new int[n];

        for (int i = n - 1; i >= 0; i--) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    palindrome[i][j] = true;
                } else if (j - i == 1) {
                    palindrome[i][j] = s.charAt(i) == s.charAt(j);
                } else {
                    palindrome[i][j] = palindrome[i + 1][j - 1] && s.charAt(i) == s.charAt(j);
                }
            }
        }
        StringUtils.print(palindrome);
        dp[0] = 0;
        for (int i = 1; i < n; i++) {
            if (palindrome[0][i]) {
                dp[i] = 0;
            } else {
                //idx 0 1 2 3 4 5
                //s   a b a a b a
                //dp  0 1 0 1 1
                dp[i] = dp[i - 1] + 1;
                for (int j = 0; j < i; j++) {
                    // j = 0 i = 3 palindrome[0 + 1][3] == false dp[3] = dp[2] + 1
                    // j = 1 i = 3 palindrome[1 + 1][3] == true  dp[3] = dp[1] + 1
                    // j = 2 i = 3 palindrome[2 + 1][3] == true  dp[3] = dp[2] + 1
                    // j = 0 i = 4 palindrome[0 + 1][4] == true  dp[4] = dp[0] + 1
                    if (palindrome[j + 1][i]) {
                        dp[i] = Math.min(dp[i], dp[j] + 1);
                    }
                }
            }

        }
        return dp[n - 1];
    }

    public static void main(String[] args) {
        // aabbaa c
        // a abba a c
        // aa bb aa c
        // a a b b a a c
        System.out.println(new L132().minCut("aabbaac"));
    }
}
