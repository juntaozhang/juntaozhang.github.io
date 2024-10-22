package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L198 {


    public int rob(int[] nums) {
        if (nums == null) {
            return 0;
        }
        int[] dp = new int[nums.length];
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i - 2 >= 0) {
                dp[i] = Math.max(nums[i] + dp[i - 2], dp[i - 1]);
            } else if (i == 1) {
                dp[i] = Math.max(nums[i], dp[i - 1]);
            } else {
                dp[i] = nums[i];
            }
            res = Math.max(res, dp[i]);
        }

        for (int i = 0; i < nums.length; i++) {
            System.out.print(dp[i] + "\t");
        }
        System.out.println();
        return res;
    }

    public int rob2(int[] nums) {
        if (nums == null) {
            return 0;
        }
        int res = 0, t1 = 0, t2 = 0, t3;
        for (int i = 0; i < nums.length; i++) {
            if (i - 2 >= 0) {
                t3 = Math.max(nums[i] + t1, t2);
            } else if (i == 1) {
                t3 = Math.max(nums[i], t1);
            } else {
                t3 = nums[i];
            }
            t1 = t2;
            t2 = t3;
            res = Math.max(res, t3);

        }
        return res;
    }


    public static void main(String[] args) {
        System.out.println(new L198().rob(new int[]{1, 2, 3, 1}));
        System.out.println(new L198().rob(new int[]{2, 7, 9, 3, 1}));
    }
}
