package cn.juntaozhang.lintcode;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

/**
 * 
 */
public class MinimumAdjustmentCost {
    @Test
    public void test() {
        System.out.println(MinAdjustmentCost2(Lists.newArrayList(1,4,2,3), 1));
    }

    public int MinAdjustmentCost2(ArrayList<Integer> A, int target) {
        //dp[i][h] = min(dp[i - 1][low ~ high]) + abs(h - A[i])
        //low=max(1, h - A[i]) high=min(100,h + A[i])
        int maxh = 100 + 1;
        int n = A.size();
        int[][] dp = new int[n + 1][maxh];
        for (int i = 1; i < dp.length; i++) {
            int a = A.get(i - 1);
            for (int h = 1; h < maxh; h++) {
                int low = Math.max(1, h - target);
                int high = Math.min(100, h + target);
                int min = Integer.MAX_VALUE;
                for (int j = low; j <= high; j++) {
                    min = Math.min(min, dp[i - 1][j]);
                }
                dp[i][h] = min + Math.abs(h - a);
            }
        }
        int rst = 0x7fffffff;
        for (int h = 1; h < maxh; ++h) {
            if (dp[n][h] < rst) {
                rst = dp[n][h];
            }
        }
        return rst;
    }

    public int MinAdjustmentCost(ArrayList<Integer> A, int target) {
        if (A.isEmpty() || A.size() <= 1) {
            return 0;
        }

        int n = A.size(), maxh = 100;
        int[][] f = new int[n + 1][maxh + 1];
        // Init
        f[0][0] = 0;
        for (int i = 1; i <= n; ++i) {
            f[i][0] = 0;
        }
        for (int h = 1; h <= maxh; ++h) {
            f[0][h] = 0;
        }
        // DP
        for (int i = 1; i <= n; ++i) {
            for (int h = 1; h <= maxh; ++h) {
                int low = Math.max(1, h - target);
                int high = Math.min(maxh, h + target);
                int tmp = f[i - 1][low];
                for (int j = low + 1; j <= high; ++j) {
                    if (f[i - 1][j] < tmp) {
                        tmp = f[i - 1][j];
                    }
                }
                f[i][h] = tmp + Math.abs(h - A.get(i - 1));
            }
        }
        print(f);
        // Result
        int rst = 0x7fffffff;
        for (int h = 1; h <= maxh; ++h) {
            if (f[n][h] < rst) {
                rst = f[n][h];
            }
        }
        return rst;
    }


    private void print(int[][] nums) {
        for (int[] arr : nums) {
            for (int i : arr) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

}
