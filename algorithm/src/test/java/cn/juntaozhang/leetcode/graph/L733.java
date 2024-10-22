package cn.juntaozhang.leetcode.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author juntzhang
 */
public class L733 {

    public int[][] floodFill0(int[][] image, int sr, int sc, int color) {
        int[] dx = {1, 0, 0, -1};
        int[] dy = {0, 1, -1, 0};

        int sColor = image[sr][sc];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{sr, sc});
        image[sr][sc] = color;
        Set<String> set = new HashSet<>();
        set.add(sr + "_" + sc);

        int rows = image.length, cols = image[0].length;
        while (!queue.isEmpty()) {
            int[] arr = queue.poll();
            for (int i = 0; i < 4; i++) {
                int x = arr[0] + dx[i];
                int y = arr[1] + dy[i];
                if (x >= 0 && y >= 0 && x < rows && y < cols && image[x][y] == sColor && !set.contains(x + "_" + y)) {
                    queue.offer(new int[]{x, y});
                    set.add(x + "_" + y);
                    image[x][y] = color;
                }
            }

        }
        return image;
    }

    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        int[][] directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int n = image.length, m = image[0].length;
        int target = image[sr][sc];
        Queue<int[]> q = new LinkedList<>();
        boolean[][] visited = new boolean[n][m];
        q.offer(new int[]{sr, sc});
        while (!q.isEmpty()) {
            int[] t = q.poll();
            int r = t[0], c = t[1];
            image[r][c] = color;
            visited[r][c] = true;
            for (int[] xy : directions) {
                int x = r + xy[0], y = c + xy[1];
                if (x >= 0 && x < n && y >= 0 && y < m && !visited[x][y] && image[x][y] == target) {
                    q.offer(new int[]{x, y});
                }
            }
        }
        return image;
    }

    public static void main(String[] args) {
//    new L733().floodFill(new int[][]{
//        {1, 1, 1},
//        {1, 1, 0},
//        {1, 0, 1}
//    }, 1, 1, 2);
        new L733().floodFill(new int[][]{
                {0, 0, 0},
                {0, 0, 0}
        }, 1, 0, 2);
    }
}
