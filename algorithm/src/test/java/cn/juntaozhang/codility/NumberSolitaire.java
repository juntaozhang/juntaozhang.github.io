package cn.juntaozhang.codility;

import org.junit.Test;

import java.util.Arrays;

/**
 * 
 */
public class NumberSolitaire {

    @Test
    public void solution() {

        System.out.println(solution(new int[]{1, -2, 0, 9, -1, -2, -1, -1, -1, 0, 0, 1}));
    }


    public int solution(int[] A) {
        // dp[i] = max(dp[i-1],dp[i-2],dp[i-3],dp[i-4],dp[i-5],dp[i-6]) + A[i]
        if (A.length == 0) {
            return 0;
        }
        int[] dp = new int[A.length];
        dp[0] = A[0];
        for (int i = 1; i < dp.length; i++) {
            dp[i] = dp[i - 1];
            for (int j = i - 1; i - j <= 6 && j >= 0; j--) {
                dp[i] = Math.max(dp[i], dp[j]);
            }
            dp[i] += A[i];
        }
        System.out.println(Arrays.toString(A));
        System.out.println(Arrays.toString(dp));
        return dp[A.length - 1];
    }

}
