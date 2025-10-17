package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 */
public class NearestExit {
    @Test
    public void wallsAndGates() {
        wallsAndGates(new int[][]{{2147483647, -1, 0, 2147483647}, {2147483647, 2147483647, 2147483647, -1}, {2147483647, -1, 2147483647, -1}, {0, -1, 2147483647, 2147483647}});
    }

    public void wallsAndGates(int[][] rooms) {
        //row,col
        Queue<int[]> q = new LinkedList<>();
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms[i].length; j++) {
                if (rooms[i][j] == 0) {
                    q.offer(new int[]{i, j});
                }
            }
        }
        print(rooms);
        bfs(rooms, q);
        print(rooms);
    }

    void bfs(int[][] rooms, Queue<int[]> q) {
        int[] dRow = new int[]{0, 1, 0, -1};
        int[] dCol = new int[]{1, 0, -1, 0};
        while (!q.isEmpty()) {
            int[] rowCol = q.poll();
            for (int i = 0; i < 4; i++) {
                int nRow = rowCol[0] + dRow[i];
                int nCol = rowCol[1] + dCol[i];
                if (nRow >= 0 && nRow < rooms.length
                        && nCol >= 0 && nCol < rooms[0].length
                        && rooms[nRow][nCol] == Integer.MAX_VALUE) {
                    rooms[nRow][nCol] = rooms[rowCol[0]][rowCol[1]] + 1;
                    print(rooms);
                    q.offer(new int[]{nRow, nCol});
                }
            }
        }
    }

    private void print(int[][] nums) {
        for (int[] arr : nums) {
            for (int i : arr) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
/*
hit
    hot
dot
"hot"
"dot"
"dog"
"cog"
*/
}
