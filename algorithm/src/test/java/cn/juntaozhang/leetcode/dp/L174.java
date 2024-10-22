package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;

public class L174 {
    public int calculateMinimumHP(int[][] dungeon) {
        int n = dungeon.length;
        int m = dungeon[0].length;
        int[][] dungeon2 = new int[n][m];
        int[][] dp = new int[n][m];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                dungeon2[n - 1 - i][m - 1 - j] = dungeon[i][j];
            }
        }
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                if(i > 0 && j > 0) {
                    dp[i][j] = Math.max(1, Math.min(dp[i - 1][j],dp[i][j - 1]) - dungeon2[i][j]);
                } else if(i > 0) {
                    dp[i][j] = Math.max(1, dp[i - 1][j] - dungeon2[i][j]);
                } else if(j > 0) {
                    dp[i][j] = Math.max(1, dp[i][j - 1] - dungeon2[i][j]);
                } else {
                    dp[i][j] = Math.max(1, 1 - dungeon2[i][j]);
                }
            }
        }
        return dp[n - 1][m - 1];
    }

    public static void main(String[] args) {
        System.out.println(new L174().calculateMinimumHP(StringUtils.str2arr2("[[-2,-3,3],[-5,-10,1],[10,30,-5]]")));
        System.out.println(new L174().calculateMinimumHP(StringUtils.str2arr2("[[1,-3,3],[0,-2,0],[-3,-3,-3]]")));
    }
}
