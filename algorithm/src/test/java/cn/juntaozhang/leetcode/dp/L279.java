package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L279 {
    public int numSquares(int n) {
        int[] dp = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            dp[i] = i;
        }

        for (int i = 2; i * i <= n; i++) {
            int square = i * i;
            for (int j = 2; j <= n; j++) {
                if (j >= square) {
                    dp[j] = Math.min(dp[j], dp[j - square] + 1);
                }
            }
        }

        return dp[n];
    }

    @Test
    public void case1() {
        System.out.println(numSquares(12));
        System.out.println(numSquares(13));
    }
}
