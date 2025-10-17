package cn.juntaozhang.leetcode.dp;

import java.util.Arrays;

/**
 * @author juntzhang
 */
public class L338 {

    public int[] countBits(int n) {
        int[] dp = new int[n + 1];
        dp[0] = 0;
        if (n < 1) {
            return dp;
        }
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - largestPower(i)] + 1;
        }
        return dp;
    }

    private static int largestPower(long N) {
        //changing all right side bits to 1.
        N = N | (N >> 1);
        N = N | (N >> 2);
        N = N | (N >> 4);
        N = N | (N >> 8);
        N = N | (N >> 16); // java中要是int型，只需要到这步
        N = N | (N >> 32);
        return (int) (N + 1) >> 1;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new L338().countBits(1)));
        System.out.println(Arrays.toString(new L338().countBits(1)));
        System.out.println(Arrays.toString(new L338().countBits(2)));
        System.out.println(Arrays.toString(new L338().countBits(3)));
        System.out.println(Arrays.toString(new L338().countBits(64)));
//        System.out.println(Arrays.toString(new L338().countBits((int) Math.pow(10, 5))));
    }
}
