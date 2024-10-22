package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Arrays;

/**
 * https://segmentfault.com/a/1190000006325321
 *
 * 
 */
public class Backpack {
    //i为容量
    //dp[m-A[i]] => 递归
    //             不包含      包含 a[i]
    //dp[i] = max(dp[i], dp[j-A[i]]+a[i])
    public int backPack(int m, int[] A) {
        int[] dp = new int[m + 1];
        for (int i = 0; i < A.length; i++) {
            for (int j = m; j > 0; j--) {
                if (j >= A[i]) {
                    dp[j] = Math.max(dp[j], dp[j - A[i]] + A[i]);
                }
            }
        }
        return dp[m];
    }

    public int backPack2(int m, int[] A) {
        int[] dp = new int[m + 1];
        for (int a : A) {
            for (int i = m; i > 0; i--) {
                if (i >= a) {
                    dp[i] = Math.max(dp[i], dp[i - a] + a);
                }
            }
            System.out.println(Arrays.toString(dp));
        }
        return dp[m];
    }

    @Test
    public void backPack() {
        System.out.println(backPack2(10, new int[]{2, 3, 5, 2}));
    }

    //物品的空间和价值分离出来了，m 是size 求最大价值
    //dp[j] = max(dp[j], dp[j-A[i]] + V[i])
    public int backPackII(int m, int[] A, int V[]) {
        int[] dp = new int[m + 1];
        for (int i = 0; i < A.length; i++) {
            for (int j = m; j > 0; j--) {
                if (j >= A[i]) {
                    dp[j] = Math.max(dp[j], dp[j - A[i]] + V[i]);
                }
            }
        }
        return dp[m];
    }

    public int backPackIII(int[] A, int[] V, int m) {
        int[] dp = new int[m + 1];
        for (int i = 0; i < A.length; i++) {
            for (int j = 1; j <= m; j++) {
                if (j >= A[i]) {
                    dp[j] = Math.max(dp[j], dp[j - A[i]] + V[i]);
                }
            }
            System.out.println(Arrays.toString(dp));
        }
        return dp[m];
    }


    @Test
    public void backPackIII() {
        System.out.println(backPackIII(new int[]{2, 5, 7, 3}, new int[]{1, 2, 4, 5}, 10));
    }


    public int backPackIV(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 1; j <= target; j++) {
                if (nums[i] == j) dp[j]++;
                else if (nums[i] < j) dp[j] += dp[j - nums[i]];
            }
            System.out.println(Arrays.toString(dp));
        }
        return dp[target];
    }

    @Test
    public void backPackIV() {
//        System.out.println(backPackIV(new int[]{2,3,6,7},7));
        System.out.println(backPackIV(new int[]{1, 2, 4}, 4));
    }

    public int backPackV(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int i = 0; i < nums.length; i++) {
            for (int j = target; j >= 0; j--) {
                if (nums[i] <= j) dp[j] += dp[j - nums[i]];
            }
            System.out.println(Arrays.toString(dp));
        }
        return dp[target];
    }

    @Test
    public void backPackV() {
        System.out.println(backPackV(new int[]{1, 2, 3, 3, 7}, 7));
    }

    public int backPackVI(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int i = 1; i <= target; i++) {
            for (int num : nums) {
                if (num <= i) {
                    dp[i] += dp[i - num];
                }
            }
            System.out.println(Arrays.toString(dp));
        }
        return dp[target];
    }

    @Test
    public void backPackVI() {
        System.out.println(backPackVI(new int[]{1, 3, 2}, 4));
    }
}
