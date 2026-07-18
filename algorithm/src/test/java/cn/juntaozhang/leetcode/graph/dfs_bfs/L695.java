package cn.juntaozhang.leetcode.graph.dfs_bfs;

import org.junit.Test;

import java.util.LinkedList;

public class L695 {
    public int maxAreaOfIsland(int[][] grid) {
        int row = grid.length;
        int col = grid[0].length;
        boolean[][] visited = new boolean[row][col];
        int result = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (grid[i][j] == 1 && !visited[i][j]) {
                    result = Math.max(result, bfs(grid, i, j, visited));
                }
            }
        }
        return result;
    }


    int[][] directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public int bfs(int[][] grid, int i, int j, boolean[][] visited) {
        int count = 0;
        LinkedList<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{i, j});
        visited[i][j] = true;
        int row = grid.length;
        int col = grid[0].length;
        while (!queue.isEmpty()) {
            int[] arr = queue.poll();
            count++;
            for (int[] d : directions) {
                i = arr[0] + d[0];
                j = arr[1] + d[1];
                if (i >= 0 && j >= 0 && i < row && j < col && !visited[i][j] && grid[i][j] == 1) {
                    queue.offer(new int[]{i, j});
                    visited[i][j] = true;
                }
            }
        }
        return count;
    }

    @Test
    public void case1() {

    }
}
