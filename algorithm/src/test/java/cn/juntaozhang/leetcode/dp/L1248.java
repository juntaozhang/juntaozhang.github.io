package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

public class L1248 {
    public int numberOfSubarrays(int[] nums, int k) {
        //      1 1 2 1 1 1 1
        //    0 1 2 2 3 4 5 6 中间状态
        //dp: 0->1,1->1,2->2,[3->1,4->1,5->2,6->1] k = 3 前缀频次
        int n = nums.length;
        int[] dp = new int[n + 1];
        int result = 0;
        int odd = 0;
        dp[0] = 1;
        for (int num : nums) {
            if (num % 2 == 1) {
                odd++;
            }
            if (odd >= k) {
                result += dp[odd - k];
            }
            dp[odd]++;
        }
        return result;
    }

    @Test
    public void case1() {
        System.out.println(numberOfSubarrays(new int[]{1, 1, 2, 1, 1}, 3));
    }

    @Test
    public void case2() {
        System.out.println(numberOfSubarrays(new int[]{0,0,1,0,2,1,1,2}, 2));
    }
}
