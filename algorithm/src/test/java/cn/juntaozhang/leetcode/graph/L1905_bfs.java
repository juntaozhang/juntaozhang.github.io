package cn.juntaozhang.leetcode.graph;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author juntzhang
 */
public class L1905_bfs {

    public int countSubIslands(int[][] grid1, int[][] grid2) {
        int rows = grid1.length;
        int cols = grid1[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid1[i][j] == 0 && grid2[i][j] != 0) {
                    bfs(grid2, i, j, 0);
                }
            }
        }
        int res = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid2[i][j] == 1) {
                    bfs(grid2, i, j, 0);
                    res++;
                }
            }
        }
        return res;
    }

    private int[][] directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public void bfs(int[][] grid, int sr, int sc, int target) {
        int rows = grid.length;
        int cols = grid[0].length;
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{sr, sc});
        grid[sr][sc] = target;

        while (!q.isEmpty()) {
            int[] t = q.poll();

            for (int[] d : directions) {
                int x = t[0] + d[0];
                int y = t[1] + d[1];
                if (x >= 0 && x < rows && y >= 0 && y < cols && grid[x][y] == 1) {
                    q.offer(new int[]{x, y});
                    grid[x][y] = target;
                }
            }
        }

    }
}
