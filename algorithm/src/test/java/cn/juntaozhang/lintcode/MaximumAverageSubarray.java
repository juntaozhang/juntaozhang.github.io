package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class MaximumAverageSubarray {

    public double maxAverage2(int[] nums, int k) {
        double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int n : nums) {
            if (n < min) {
                min = n;
            }
            if (n > max) {
                max = n;
            }
        }

        while ((max - min) > 1e-4) {
            double mid = (max + min) / 2.0;
            System.out.println("mid=" + min + ",max=" + max + ",mid=" + mid);

            //有子序列 > mid
            if (check(nums, mid, k)) {
                min = mid;
            } else{
                max = mid;
            }
        }
        return min;
    }

    public boolean check(int[] nums, double mid, int k) {
        double[] sum = new double[nums.length + 1];
        double pre = 0;
        for (int i = 1; i < sum.length; i++) {
            sum[i] = nums[i - 1] + sum[i - 1] - mid;
            if(i >= k && (sum[i] - pre) >= 0 ) {
                return true;
            }
            if (i >= k) {
                pre = Math.min(pre, sum[i - k + 1]);
            }
        }
        return false;
    }

    @Test
    public void maxAverage() {
        System.out.println(maxAverage2(new int[]{-1,0,1}, 3));
//        System.out.println(maxAverage(new int[]{-1,-2,-3,-100,-1,-50}, 4));
    }

    public double maxAverage(int[] nums, int k) {
        // Write your code here
        double l = Integer.MAX_VALUE, r = Integer.MIN_VALUE;
        for (int i = 0; i < nums.length; ++i) {
            if (nums[i] < l)
                l = nums[i];
            if (nums[i] > r)
                r = nums[i];
        }


        while (r - l >= 1e-3) {
            double mid = (l + r) / 2.0;
            System.out.println("l=" + l + ",r=" + r + ",mid=" + mid);

            if (check_valid(nums, mid, k)) {
                l = mid;
            } else {
                r = mid;
            }
        }

        return l;
    }

    private boolean check_valid(int nums[], double mid, int k) {
        int n = nums.length;
        double min_pre = 0;
        double[] sum = new double[n + 1];
        sum[0] = 0;
        for (int i = 1; i <= n; ++i) {
            sum[i] = sum[i - 1] + nums[i - 1] - mid;
            if (i >= k && sum[i] - min_pre >= 0) {
                return true;
            }
            if (i >= k)
                min_pre = Math.min(min_pre, sum[i - k + 1]);
        }
        return false;
    }

}
