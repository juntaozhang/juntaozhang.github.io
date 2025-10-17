package cn.juntaozhang.leetcode.dp;

import java.util.Arrays;
import java.util.List;

/**
 * @author juntzhang
 */
public class L120 {
    public int minimumTotal(List<List<Integer>> triangle) {
        int rows = triangle.size();
        int lastCols = triangle.get(rows - 1).size();
        int[][] dp = new int[rows][lastCols];
        dp[0][0] = triangle.get(0).get(0);

        for (int i = 1; i < rows; i++) {
            for (int j = 0; j < triangle.get(i).size(); j++) {
                if (j == 0) {
                    dp[i][j] = dp[i - 1][j] + triangle.get(i).get(j);
                } else if (j >= i) {
                    dp[i][j] = dp[i - 1][j - 1] + triangle.get(i).get(j);
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], dp[i - 1][j]) + triangle.get(i).get(j);
                }
            }
        }
        L1314.print(dp);
        int ans = dp[rows - 1][0];
        for (int j = 1; j < lastCols; j++) {
            ans = Math.min(ans, dp[rows - 1][j]);
        }

        return ans;
    }

    public static void main(String[] args) {
        System.out.println(
                new L120().minimumTotal(
                        Arrays.<List<Integer>>asList(
                                Arrays.asList(2),
                                Arrays.asList(3, 4),
                                Arrays.asList(6, 5, 7),
                                Arrays.asList(4, 1, 8, 3)
                        )
                )
        );
    }
}
