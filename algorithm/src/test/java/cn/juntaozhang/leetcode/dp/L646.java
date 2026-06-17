package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

import java.util.Arrays;

public class L646 {
    public int findLongestChain(int[][] pairs) {
        Arrays.sort(pairs,(p1,p2)->p1[0]-p2[0]);
        int[] dp = new int[pairs.length];
        int result = 0;
        for(int i = 0; i < pairs.length; i++) {
            int t = 0;
            for(int j = i - 1; j >= 0; j--) {
                if(pairs[j][1] < pairs[i][0]) {
                    t = Math.max(t, dp[j]);
                }
            }
            dp[i] = t + 1;
            result = Math.max(result, dp[i]);
        }
        return result;
    }

    @Test
    public void case1() {
        System.out.println(findLongestChain(new int[][]{{1,2},{7,8},{4,5}}));
    }

    @Test
    public void case2() {
        System.out.println(findLongestChain(new int[][]{{1,2},{2,3},{3,4}}));
    }
}
