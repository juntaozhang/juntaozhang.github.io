package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Arrays;

/**
 * 
 */
public class CoinsInALine {
    public boolean firstWillWin(int n) {
        if (n == 1 || n == 2) {
            return false;
        } else if (n == 0) {
            return false;
        }
        boolean pre = true, curr = true;//false true true false
        System.out.print("true,true,");
        for (int i = 3; i <= n; i++) {
            boolean t = curr;
            curr = !(pre && curr);
            System.out.print(curr + ",");
            pre = t;
        }
        return curr;
    }

    @Test
    public void firstWillWin() {
        firstWillWin(10);
    }


    public boolean firstWillWinII(int[] values) {
        // write your code here
        int n = values.length;
        int[] sum = new int[n + 1];
        for (int i = 1; i <= n; ++i) {
            sum[i] = sum[i - 1] + values[n - i];
        }
        System.out.println(Arrays.toString(sum));

        int[] dp = new int[n + 1];
        dp[1] = values[n - 1];
        for (int i = 2; i <= n; ++i) {
            dp[i] = Math.max(sum[i] - dp[i - 1], sum[i] - dp[i - 2]);
            System.out.println(Arrays.toString(dp));
        }
        return dp[n] > sum[n] / 2;
    }

    public boolean firstWillWinII2(int[] values) {
        // write your code here
        // dp 表示从i到end 的最大值
        // int values[] ={1,2,4,3,4,8,5,6,12};
        int len = values.length;
        // 长度小于2的时候第一个人一定获胜
        if (len <= 2)
            return true;
        int dp[] = new int[len + 1];
        dp[len] = 0;
        dp[len - 1] = values[len - 1];
        dp[len - 2] = values[len - 1] + values[len - 2];
        dp[len - 3] = values[len - 3] + values[len - 2];
        for (int i = len - 4; i >= 0; i--) {
            dp[i] = values[i] + Math.min(dp[i + 2], dp[i + 3]);
            dp[i] = Math.max(dp[i], values[i] + values[i + 1] + Math.min(dp[i + 3], dp[i + 4]));
            System.out.println(Arrays.toString(dp));

        }
        int sum = 0;
        for (int a : values)
            sum += a;
        return dp[0] > sum - dp[0];
    }


    @Test
    public void firstWillWinII() {
        System.out.println(firstWillWinII2(new int[]{1, 1, 2, 5}));
//        System.out.println(firstWillWinII(new int[]{5, 1, 2, 10}));

    }
}
