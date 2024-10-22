package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L55 {

    public boolean canJump(int[] nums) {
        if (nums.length == 1) {
            return true;
        }
        boolean[] dp = new boolean[nums.length];
        dp[0] = true;
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                dp[i] = dp[j] && (j + nums[j] >= i);
                if (dp[i]) {
                    break;
                }
            }
        }
        return dp[nums.length - 1];

    }

    public static void main(String[] args) {
        System.out.println(new L55().canJump(new int[]{2, 3, 1, 1, 4}));
    }
}
