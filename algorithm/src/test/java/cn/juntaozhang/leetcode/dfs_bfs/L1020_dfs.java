package cn.juntaozhang.leetcode.dfs_bfs;

import org.junit.Assert;
import org.junit.Test;

public class L1020_dfs {
    private final int[][] directions = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    private int rows;
    private int cols;
    private boolean[][] visited;

    public int numEnclaves(int[][] grid) {
        rows = grid.length;
        cols = grid[0].length;
        visited = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            dfs(grid, i, 0);
            dfs(grid, i, cols - 1);
        }

        for (int j = 1; j < cols - 1; j++) {
            dfs(grid, 0, j);
            dfs(grid, rows - 1, j);
        }

        int res = 0;
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (grid[i][j] == 1 && !visited[i][j]) {
                    res++;
                }
            }
        }
        return res;
    }

    public void dfs(int[][] grid, int i, int j) {
        if (!isLand(grid, i, j)) {
            return;
        }
        visited[i][j] = true;
        for (int[] d : directions) {
            dfs(grid, i + d[0], j + d[1]);
        }
    }

    public boolean isLand(int[][] grid, int i, int j) {
        return i >= 0 && i < rows && j >= 0 && j < cols && grid[i][j] == 1 && !visited[i][j];
    }


    @Test
    public void test1() {
        int[][] grid = new int[][]{{0, 0, 0, 0}, {1, 0, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}};
        Assert.assertEquals(3, numEnclaves(grid));
    }

    @Test
    public void test2() {
        int[][] grid = new int[][]{{0, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}};
        Assert.assertEquals(0, numEnclaves(grid));
    }

    @Test
    public void test3() {
        int[][] grid = new int[][]{{0, 1, 1, 0}, {0, 0, 1, 0}, {0, 1, 0, 1}, {0, 0, 1, 1}};
        Assert.assertEquals(1, numEnclaves(grid));
    }
}
