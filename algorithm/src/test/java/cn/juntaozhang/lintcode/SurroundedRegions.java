package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 */
public class SurroundedRegions {
    int row = 0, col = 0;
    @Test public void surroundedRegions(){
        surroundedRegions(new char[][]{"XXXX".toCharArray(),"XOOX".toCharArray(),"XXOX".toCharArray(),"XOXX".toCharArray()});
    }

    private void print(char[][] nums) {
        for (char[] arr : nums) {
            for (char i : arr) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void surroundedRegions(char[][] board) {
        print(board);
        //(row,col)
        Queue<int[]> q = new LinkedList<>();
        row = board.length;
        if (row == 0) {
            return;
        }
        col = board[0].length;
        /*
         X X O X    X X O X
         O O O X    W O O X
         X X O O    X X O W
         X O X X    X O X X
         */
        for (int i = 0; i < row; i++) {
            if (board[i][0] == 'O') {
                board[i][0] = 'W';
                q.offer(new int[]{i, 0});//两竖
            }
            if (board[i][col - 1] == 'O') {
                board[i][col - 1] = 'W';
                q.offer(new int[]{i, col - 1});
            }
        }

        /*
         X X O X        ===>       X X W X
         W O O X        ===>       W O O X
         X X O W        ===>       X X O W
         X O X X        ===>       X W X X
         */
        for (int j = 0; j < col; j++) {
            if (board[0][j] == 'O') {
                board[0][j] = 'W';
                q.offer(new int[]{0, j});
            }
            if (board[row - 1][j] == 'O') {
                board[row - 1][j] = 'W';
                q.offer(new int[]{row - 1, j});
            }
        }

        print(board);
        bfs(board, q);
        print(board);

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                } else if (board[i][j] == 'W') {
                    board[i][j] = 'O';
                }
            }
        }

        print(board);
    }

    void bfs(char[][] board, Queue<int[]> q) {
        int[] dRow = new int[]{1, 0, -1, 0};
        int[] dCol = new int[]{0, 1, 0, -1};
        while (!q.isEmpty()) {
            int[] rowCol = q.poll();
            for (int i = 0; i < 4; i++) {
                int nRow = rowCol[0] + dRow[i];
                int nCol = rowCol[1] + dCol[i];
                if ((nRow >= 0 && nRow < row) && (nCol >= 0 && nCol < col)
                        && board[nRow][nCol] == 'O') {
                    board[nRow][nCol] = 'W';
                    q.offer(new int[]{nRow, nCol});
                }
            }
        }
    }

}
