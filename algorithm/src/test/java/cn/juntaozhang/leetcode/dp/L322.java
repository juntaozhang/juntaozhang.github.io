package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

import java.util.Arrays;

public class L322 {
    public int coinChange(int[] coins, int amount) {
        Arrays.sort(coins);
        int[] dp = new int[amount + 1];
        for (int i = 1; i <= amount; i++) {
            dp[i] = amount + 1;
            for (int coin : coins) {
                if (i < coin) {
                    break;
                }
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);

            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    @Test
    public void case1() {
        System.out.println(coinChange(new int[]{1, 2, 5}, 11));
    }

    @Test
    public void case2() {
        System.out.println(coinChange(new int[]{2}, 3));
    }

    @Test
    public void case3() {
        System.out.println(coinChange(new int[]{186, 419, 83, 408}, 6249));
    }
}
