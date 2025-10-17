package cn.juntaozhang.codility;

import org.junit.Test;

import java.util.Arrays;

/**
 * 
 */
public class Demo1 {
    @Test
    public void test() {
        System.out.println(backPack(10, new int[]{3}));
    }

    public int backPack(int m, int[] A) {
        if (A.length == 0) {
            return 0;
        }
        int[] dp = new int[m + 1];
        // dp[i] = max(dp[i - a] + a, dp[i])
        for (int a : A) {
            for (int i = m; i >= 0; i--) {
                if (i >= a) {
                    dp[i] = Math.max(dp[i - a] + a, dp[i]);
                }
                System.out.println(Arrays.toString(dp));
            }
        }
        return dp[m];
    }

    @Test
    public void longestCommonSubsequence() {
        System.out.println(longestCommonSubsequence("ABCD", "EACB"));
    }

    public int longestCommonSubsequence(String A, String B) {
        // dp[i][j] = dp[i] == dp[j] ? dp[i - 1][j - 1] + 1: max( dp[i - 1][j],  dp[i][j - 1])
        int[][] dp = new int[A.length() + 1][B.length() + 1];
        for (int i = 1; i < dp.length; i++) {
            for (int j = 1; j < dp[0].length; j++) {
                if (A.charAt(i - 1) == B.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[A.length()][B.length()];
    }
}
