package cn.juntaozhang.leetcode.graph;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author juntzhang
 */
public class L1926 {

    int[][] directions = new int[][]{
            {-1, 0},
            {1, 0},
            {0, 1},
            {0, -1}
    };

    public boolean isExit(char[][] grid, int x, int y) {
        return x == 0 || y == 0 || x == grid.length - 1 || y == grid[0].length - 1;
    }

    public int nearestExit(char[][] grid, int[] entrance) {
        int m = grid.length, n = grid[0].length;
        Queue<int[]> q = new LinkedList<>();
        boolean[][] vist = new boolean[m][n];
        q.offer(new int[]{entrance[0], entrance[1], 0});
        vist[entrance[0]][entrance[1]] = true;
        int x, y, p;
        while (!q.isEmpty()) {
            int[] t = q.poll();
            for (int[] d : directions) {
                x = d[0] + t[0];
                y = d[1] + t[1];
                p = t[2] + 1;
                if (x >= 0 && x < m && y >= 0 && y < n && grid[x][y] == '.' && !vist[x][y]) {
                    q.offer(new int[]{x, y, p});
                    vist[x][y] = true;
                    if (isExit(grid, x, y)) {
                        return p;
                    }
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[][] a = new int[][]{
                {1, 1},
                {1},
                {}
        };
        System.out.println(a[2][0]);
        System.out.println(new L1926().nearestExit(new char[][]{
                {'+', '+', '+'},
                {'.', '.', '.'},
                {'+', '+', '+'}
        }, new int[]{1, 0}));
    }
}
