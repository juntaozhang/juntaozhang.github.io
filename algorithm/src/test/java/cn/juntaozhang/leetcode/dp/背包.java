package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class 背包 {
    public int knapsack01(int[] w, int[] v, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < w.length; i++) {
            for (int j = W; j > 0; j--) {
                if (j >= w[i]) {
                    dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
                }
            }
        }
        return dp[W];
    }

    public int knapsackInf(int[] w, int[] v, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < w.length; i++) {
            for (int j = 1; j <= W; j++) {
                if (j >= w[i]) {
                    dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
                }
            }
        }
        return dp[W];
    }

    public int knapsackSi(int[] w, int[] v, int[] s, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < w.length; i++) {
            for (int k = 0; k < s[i]; k++) { // loop si
                for (int j = W; j > 0; j--) {
                    if (j >= w[i]) {
                        dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
                    }
                }
            }
        }
        return dp[W];
    }

    @Test
    public void case1() {
        int[] w = {2, 1, 1, 2};
        int[] v = {5, 1, 3, 4};
        System.out.println(knapsack01(w, v, 4));
        System.out.println(knapsackInf(w, v, 4));
    }

    @Test
    public void case2() {
        int[] w = {2, 1, 1, 2};
        int[] v = {5, 1, 3, 4};
        int[] s = {2, 5, 2, 1};
        System.out.println(knapsackSi(w, v, s, 6));
    }
}
