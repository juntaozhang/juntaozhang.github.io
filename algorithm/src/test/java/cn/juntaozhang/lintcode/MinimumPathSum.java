package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class MinimumPathSum {
    @Test
    public void minPathSum() {
        minPathSum(new int[][]{{1, 2}, {1, 1}});
    }

    public int minPathSum(int[][] grid) {
        if (grid.length == 0) {
            return 0;
        }
        int row = grid.length, col = grid[0].length;
        int[][] sum = new int[row + 1][col + 1];
        if (row > 1) {
            for (int i = 2; i <= row; i++) {
                sum[i][0] = Integer.MAX_VALUE;
            }
        }
        if (col > 1) {
            for (int i = 2; i <= col; i++) {
                sum[0][i] = Integer.MAX_VALUE;
            }
        }
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= col; j++) {
                sum[i][j] = Math.min(sum[i - 1][j], sum[i][j - 1]) + grid[i - 1][j - 1];
            }
        }
        return sum[row][col];
    }
}
