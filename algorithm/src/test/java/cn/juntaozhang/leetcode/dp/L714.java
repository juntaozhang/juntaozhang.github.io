package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L714 {
    public int maxProfit(int[] prices, int fee) {
        int n = prices.length;
        int[][] dp = new int[n][2];
        dp[0][0] = -prices[0];
        for (int i = 1; i < n; i++) {
            // 今天持股：昨天就持股 / 昨天空仓(非当日卖出)今天买入
            dp[i][0] = Math.max(dp[i - 1][0], dp[i - 1][1] - prices[i]);
            // 今天空仓：昨天本来就空仓 / 昨天刚卖出，今天保持空仓
            dp[i][1] = Math.max(dp[i - 1][1], dp[i - 1][0] + prices[i] - fee);
        }

        return dp[n - 1][1];
    }

    @Test
    public void case1() {
        int[] prices = {1, 3, 2, 8, 4, 9};
        System.out.println(maxProfit(prices, 2));
    }

}
