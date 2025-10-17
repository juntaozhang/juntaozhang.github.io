package cn.juntaozhang.leetcode.dfs_bfs;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

/**
 * Breadth-First Search
 */
public class L1020_bfs {
    private final int[][] directions = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    private int rows;
    private int cols;
    private boolean[][] visited;

    public int numEnclaves(int[][] grid) {
        LinkedList<int[]> queue = new LinkedList<>();
        rows = grid.length;
        cols = grid[0].length;
        visited = new boolean[rows][cols];
        /*
            从第一列和最后一列开始遍历, 将与边界相连的陆地标记为已访问
            x o o o x
            x o o o x
            x o o o x
            x o o o x
         */
        for (int i = 0; i < rows; i++) {
            if (isLand(grid, i, 0)) queue.offer(new int[]{i, 0});
            if (isLand(grid, i, cols - 1)) queue.offer(new int[]{i, cols - 1});
        }
        /*
           从第一行和最后一行开始遍历, 将与边界相连的陆地标记为已访问
           第一个和最后一个元素已经被上面的循环遍历过了, 所以这里只需要遍历第二个到倒数第二个元素
           o x x x o
           o o o o o
           o o o o o
           o x x x o
         */
        for (int j = 1; j < cols - 1; j++) {
            if (isLand(grid, 0, j)) queue.offer(new int[]{0, j});
            if (isLand(grid, rows - 1, j)) queue.offer(new int[]{rows - 1, j});
        }

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0];
            int y = pos[1];
            visited[x][y] = true;
            for (int[] d : directions) {
                int nx = x + d[0];
                int ny = y + d[1];
                if (isLand(grid, nx, ny)) {
                    visited[nx][ny] = true;
                    queue.offer(new int[]{nx, ny});
                }
            }
        }

        /*
           遍历整个矩阵, 统计未访问的陆地
         */
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
