package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.Arrays;

public class L200_uf {
    boolean[][] visited;
    int row, col;

    public int numIslands(char[][] grid) {
        row = grid.length;
        col = grid[0].length;
        visited = new boolean[row][col];
        UnionFind uf = new UnionFind(grid);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (!visited[i][j] && grid[i][j] == '1') {
                    visited[i][j] = true;
                    if (check(grid, i - 1, j)) uf.union(i, j, i - 1, j);
                    if (check(grid, i + 1, j)) uf.union(i, j, i + 1, j);
                    if (check(grid, i, j - 1)) uf.union(i, j, i, j - 1);
                    if (check(grid, i, j + 1)) uf.union(i, j, i, j + 1);
                }
            }
        }
        return uf.islands;
    }

    public boolean check(char[][] grid, int i, int j) {
        return i >= 0 && j >= 0 && i < row && j < col && !visited[i][j] && grid[i][j] == '1';
    }

    @Test
    public void case1(){
        numIslands(new char[][]{{'1'},{'1'}});
    }

    public static class UnionFind {
        public int[] parents;
        public int[] size;
        public int islands = 0;
        int row, col;

        public UnionFind(char[][] grid) {
            row = grid.length;
            col = grid[0].length;
            parents = new int[row * col];
            size = new int[row * col];
            Arrays.fill(size, 1);
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    parents[i * col + j] = i * col + j;
                    if (grid[i][j] == '1') {
                        islands++;
                    }
                }
            }
            Arrays.sort(new int[]{},1,3);
        }

        public int find(int i) {
            if (parents[i] != i) {
                parents[i] = find(parents[i]);
            }
            return parents[i];
        }

        public void union(int i1, int j1, int i2, int j2) {
            union(i1 * col + j1, i2 * col + j2);
        }

        public void union(int i, int j) {
            int p1 = find(i);
            int p2 = find(j);
            if (p1 == p2) {
                return;
            }
            if (size[p1] > size[p2]) {
                parents[p2] = p1;
                size[p1] += size[p2];
            } else {
                parents[p1] = p2;
                size[p2] += size[p1];
            }
            islands--;
        }
    }

}
