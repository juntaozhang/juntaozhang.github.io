package cn.juntaozhang.leetcode;

/**
 * _312_burst_balloons
 * 488
 *
 * 
 */
public class L546_remove_boxes {
    /**
     * https://zxi.mytechroad.com/blog/dynamic-programming/leetcode-546-remove-boxes/
     * dp[i][j][k] 表示boxes从i到j,boxes[j] X之后有k个X
     * 比如: CDABACA|AABAA dp[0,6,4] 表示CDABACA之后有4个A
     * dp[i][j][k] = max(case) case 1:dp[i][j-1][0] + (k+1)*(k+1)
     * case 2:dp[i][p1][k+1] + dp[p1][j-1][0]
     * case 3:dp[i][p2][k+1] + dp[p2][j-1][0]
     */
    class Solution {
        int[][][] dp;
        int[] boxes;

        public int removeBoxes(int[] boxes) {
            int len = boxes.length;
            this.boxes = boxes;
            this.dp = new int[len][len][len];
            return dfs(0, len - 1, 0);
        }

        public int dfs(int i, int j, int k) {
            if (i > j) {
                return 0;
            }
            // CDABACAAAAA 0,10,0 == CDABACA|AAAA 0,6,4
            while (i < j && boxes[j] == boxes[j - 1]) {
                j--;
                k++;
            }
            if (dp[i][j][k] > 0) {
                return dp[i][j][k];
            }
            dp[i][j][k] = dfs(i, j - 1, 0) + (k + 1) * (k + 1);
            for (int p = i; p < j; p++) {
                if (boxes[p] == boxes[j]) {
                    dp[i][j][k] = Math.max(dfs(i, p, k + 1) + dfs(p + 1, j - 1, 0), dp[i][j][k]);
                }
            }
            return dp[i][j][k];
        }
    }

    public static void main(String[] args) {
        Solution s = new L546_remove_boxes().new Solution();

    }
}
