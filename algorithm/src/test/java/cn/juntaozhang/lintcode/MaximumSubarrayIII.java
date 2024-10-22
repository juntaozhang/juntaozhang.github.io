package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class MaximumSubarrayIII {
    @Test
    public void maxSubArray() {
        System.out.println(maxSubArray(new int[]{-1,-2,-3,-100,-1,-50}, 2));
    }

    public int maxSubArray2(int[] nums, int k) {
        if (nums.length < k) {
            return 0;
        }
        int len = nums.length;


        int[][] globalMax = new int[k + 1][len + 1];
        int[][] localMax = new int[k + 1][len + 1];

        for (int i = 1; i <= k; i++) {
            localMax[i][i-1] = Integer.MIN_VALUE;
            //小于 i 的数组不能够partition
            for (int j = i; j <= len; j++) {
                localMax[i][j] = Math.max(localMax[i][j-1], globalMax[i - 1][j-1]) + nums[j-1];
                if (j == i)
                    globalMax[i][j] = localMax[i][j];
                else
                    globalMax[i][j] = Math.max(globalMax[i][j-1], localMax[i][j]);
            }
        }
        return globalMax[k][len];
    }

    public int maxSubArray(int[] nums, int k) {
        int[][] localMax = new int[k + 1][nums.length + 1];
        int[][] globalMax = new int[k + 1][nums.length + 1];

        for (int i = 1; i <= k; i++) {
            localMax[i][i - 1] = Integer.MIN_VALUE;
            globalMax[i][i - 1] = Integer.MIN_VALUE;
            for (int j = i; j <= nums.length;
                 j++) {
                localMax[i][j] = Math.max(globalMax[i - 1][j - 1], localMax[i][j - 1]) + nums[j - 1];
                globalMax[i][j] = Math.max(globalMax[i][j - 1], localMax[i][j]);
            }
        }

        return globalMax[k][nums.length];
    }
}
