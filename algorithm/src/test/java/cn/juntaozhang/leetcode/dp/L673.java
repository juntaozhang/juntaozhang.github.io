package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L673 {

    public int findNumberOfLIS(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        int[] counts = new int[n];
        int maxDP = 0;
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            counts[i] = 1;
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    if (dp[i] < dp[j] + 1) {
                        counts[i] = counts[j];
                        dp[i] = dp[j] + 1;
                    } else if (dp[j] + 1 == dp[i]) {
                        counts[i] += counts[j];
                    }
                }
            }
            maxDP = Math.max(maxDP, dp[i]);
        }

        int ans = 0;
        for (int i = 0; i < n; i++) {
            if (dp[i] == maxDP) {
                ans += counts[i];
            }
        }
        return ans;
    }

    public int findNumberOfLIS2(int[] nums) {
        if (nums.length == 1) {
            return 1;
        }
        int[] dp = new int[nums.length];
        int[] cnt = new int[nums.length];
        dp[0] = 1;
        cnt[0] = 1;
        int maxDp = 0;
        int ans = 0;
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                int t = (nums[j] < nums[i] ? dp[j] : 0) + 1;
                if (t == dp[i] && nums[j] < nums[i]) {
                    cnt[i] += cnt[j];
                } else if (t > dp[i]) {
                    cnt[i] = cnt[j];
                    dp[i] = t;
                }
            }
            if (maxDp < dp[i]) {
                maxDp = dp[i];
            }
        }
        for (int i = 0; i < dp.length; i++) {
            if (dp[i] == maxDp) {
                ans += cnt[i];
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L673().findNumberOfLIS(new int[]{1, 2, 4, 3, 5, 4, 7, 2}));
        System.out.println(new L673().findNumberOfLIS(new int[]{2, 2, 2, 2}));
        System.out.println(new L673().findNumberOfLIS(new int[]{1, 3, 5, 4, 1}));
        System.out.println(new L673().findNumberOfLIS(new int[]{1, 3, 5, 4, 7}));
        System.out.println(new L673().findNumberOfLIS(new int[]{1, 1, 1, 2, 2, 2, 3, 3, 3}));

        System.out.println();
        System.out.println(new L673().findNumberOfLIS2(new int[]{1, 2, 4, 3, 5, 4, 7, 2}));
        System.out.println(new L673().findNumberOfLIS2(new int[]{2, 2, 2, 2}));
        System.out.println(new L673().findNumberOfLIS2(new int[]{1, 3, 5, 4, 1}));
        System.out.println(new L673().findNumberOfLIS2(new int[]{1, 3, 5, 4, 7}));
        System.out.println(new L673().findNumberOfLIS2(new int[]{1, 1, 1, 2, 2, 2, 3, 3, 3}));
    }
}
