package cn.juntaozhang.leetcode.dp;

/**
 * 0-1 背包问题
 *
 * @author juntzhang
 */
public class KnapsackProblem0_1 {

    public static int run(int[] w, int[] v, int W) {
        int[] dp = new int[W + 1];
        int ans = 0;
        System.out.print("              ");
        for (int j = 1; j <= W; j++) {
            System.out.print(j + "\t");
        }
        System.out.println();
        for (int i = 0; i < w.length; i++) {
            int[] tmp = new int[W + 1];
            for (int j = 1; j <= W; j++) {
                if (j >= w[i]) {
                    tmp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
                } else {
                    // 背包容量放不下 i
                    tmp[j] = Math.max(dp[j], 0);
                }
            }

            dp = tmp;

            System.out.print(i + ",w=" + w[i] + ",v=" + v[i] + " = [ ");
            for (int j = 1; j <= W; j++) {
                System.out.print(dp[j] + "\t");
            }
            System.out.println("]");
        }
        for (int i = 1; i <= W; i++) {
            ans = Math.max(dp[i], ans);
        }
        return ans;
    }

    public static int run2(int[] w, int[] v, int W) {
        int[] dp = new int[W + 1];
        int ans = 0;
        for (int i = 0; i < w.length; i++) {
            for (int j = W; j > 0; j--) {
                if (j >= w[i]) {
                    dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
                }
            }
        }
        for (int i = 1; i <= W; i++) {
            ans = Math.max(dp[i], ans);
        }
        return ans;
    }


    public static void main(String[] args) {
        System.out.println(run(new int[]{1, 1, 2, 2}, new int[]{1, 3, 4, 5}, 4));
        System.out.println(run2(new int[]{20, 30, 40, 50, 60}, new int[]{20, 30, 44, 55, 60}, 100));
    }
}
