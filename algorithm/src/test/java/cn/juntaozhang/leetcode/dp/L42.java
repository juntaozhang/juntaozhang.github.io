package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L42 {

    public int trap(int[] height) {
        int ans = 0;
        int[][] dp = new int[2][height.length];
        dp[0][0] = height[0];
        dp[1][height.length - 1] = height[height.length - 1];
        for (int i = 1; i < height.length; i++) {
            dp[0][i] = Math.max(dp[0][i - 1], height[i]);
        }
        for (int i = height.length - 2; i > 0; i--) {
            dp[1][i] = Math.max(dp[1][i + 1], height[i]);
        }
        for (int i = 1; i < height.length - 1; i++) {
            int t = Math.min(dp[0][i - 1], dp[1][i + 1]) - height[i];
            if (t > 0) {
                ans += t;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L42().trap(new int[]{0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1}));
    }
}
