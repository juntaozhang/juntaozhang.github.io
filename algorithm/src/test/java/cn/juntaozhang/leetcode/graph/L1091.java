package cn.juntaozhang.leetcode.graph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author juntzhang
 */
public class L1091 {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] paths = new int[m][n];
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{0, 0, 1});
        for (int i = 0; i < m; i++) {
            Arrays.fill(paths[i], -1);
        }
        paths[0][0] = 1;
        int[][] directions = new int[][]{
                {-1, 0},
                {-1, 1},
                {0, 1},
                {1, 1},
                {1, 0},
                {1, -1},
                {0, -1},
                {-1, -1}
        };
        int x, y, path;
        while (!q.isEmpty()) {
            int[] t = q.poll();
            for (int[] d : directions) {
                x = d[0] + t[0];
                y = d[1] + t[1];
                path = t[2] + 1;
                if (x >= 0 && x < m && y >= 0 && y < n && grid[x][y] == 0 && (paths[x][y] == -1 || paths[x][y] > path)) {
                    q.offer(new int[]{x, y, path});
                    paths[x][y] = path;
                }
            }
        }
        return paths[m - 1][n - 1];
    }

    public static void main(String[] args) {
        System.out.println(
                new L1091().shortestPathBinaryMatrix(new int[][]{
                        {1, 0, 0},
                        {1, 1, 0},
                        {1, 1, 0}
                })
        );
    }
}
