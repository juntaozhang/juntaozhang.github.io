package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

public class L2291 {
    // 0-1 背包问题
    public int maximumProfit(int[] present, int[] future, int budget) {
        int n = present.length;
        int[] dp = new int[budget + 1];
        for (int i = 0; i < n; i++) {
            for (int j = budget; j >= present[i]; j--) {
                dp[j] = Math.max(dp[j], dp[j - present[i]] + future[i] - present[i]);
            }
            StringUtils.print2(dp);
        }
        return dp[budget];
    }

    @Test
    public void case1() {
        System.out.println(maximumProfit(new int[]{5, 4, 6, 2, 3}, new int[]{8, 5, 4, 3, 5}, 10));
    }

    @Test
    public void case2() {
        System.out.println(maximumProfit(new int[]{2, 2, 5}, new int[]{3, 4, 10}, 6));
    }
}
