package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L45 {

    public int jump(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }
        int[] dp = new int[nums.length];
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if ((j == 0 || dp[j] > 0) && nums[j] + j >= i) {
                    if (dp[i] > 0) {
                        dp[i] = Math.min(dp[j] + 1, dp[i]);
                    } else {
                        dp[i] = dp[j] + 1;
                    }
                }
            }
        }
        return dp[nums.length - 1];
    }

    public static void main(String[] args) {
//    System.out.println(new L45().jump(new int[]{2, 3, 1, 1, 4}));
        System.out.println(new L45().jump(new int[]{2, 3, 1, 1, 0, 4}));
    }
}
