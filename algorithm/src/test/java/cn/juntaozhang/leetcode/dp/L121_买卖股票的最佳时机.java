package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L121_买卖股票的最佳时机 {
    public int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) return 0;
        int n = prices.length;
        int[] dp = new int[n];
        dp[0] = 0;
        int min = prices[0];
        for (int i = 1; i < n; i++) {
            dp[i] = Math.max(dp[i - 1], prices[i] - min);
            min = Math.min(min, prices[i]);
        }
        return dp[n - 1];
    }

    @Test
    public void case1() {
        int[] prices = {7, 1, 5, 3, 6, 4};
        System.out.println(maxProfit(prices));
    }

}
