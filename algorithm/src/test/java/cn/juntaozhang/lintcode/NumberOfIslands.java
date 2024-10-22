package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 */
public class NumberOfIslands {

    /*
      1,1,1,1,1,1
      1,0,0,0,0,1
      1,0,1,1,0,1
      1,0,0,0,0,1
      1,1,1,1,1,1


     */
    @Test
    public void numIslands() {
        //[[1,1,0,0,0],[0,1,0,0,1],[0,0,0,1,1],[0,0,0,0,0],[0,0,0,0,1]]
        System.out.println(numIslands(new boolean[][]{
//                {true, true, true},
//                {true, false, true},
//                {true, false, true},

                {true, true, true, true, true, true},
                {true, false, false, false, false, true},
                {true, false, true, true, false, true},
                {true, false, false, true, false, true},
                {true, false, true, true, false, true},
                {true, false, false, false, false, true},
                {true, true, true, true, true, true},
        }));
    }

    public int numIslands(boolean[][] grid) {
        int res = 0;
        // row, col
        int mRow = grid.length;
        if (mRow == 0) {
            return 0;
        }
        Queue<int[]> q = new LinkedList<>();
        int mCol = grid[0].length;
        for (int i = 0; i < mRow; i++) {
            for (int j = 0; j < mCol; j++) {
                if (grid[i][j]) {
                    q.offer(new int[]{i, j});
                    bfs(grid, q, mRow, mCol);
                    res++;
                }
            }
        }
        return res;
    }

    private void bfs(boolean[][] grid, Queue<int[]> q, int mRow, int mCol) {
        int[] dRow = new int[]{1, 0, -1, 0};
        int[] dCol = new int[]{0, 1, 0, -1};
        while (!q.isEmpty()) {
            int[] rowCol = q.poll();
            grid[rowCol[0]][rowCol[1]] = false;
            for (int i = 0; i < 4; i++) {
                int nRow = rowCol[0] + dRow[i];
                int nCol = rowCol[1] + dCol[i];
                if (nRow < mRow && nRow >= 0 && nCol < mCol && nCol >= 0 && grid[nRow][nCol]) {
                    q.offer(new int[]{nRow, nCol});
                }
            }
        }
    }
}
