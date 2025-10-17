package cn.juntaozhang.leetcode.graph;

import org.junit.Test;

/**
 * @author juntzhang
 */
public class L200_uf {

    @Test
    public void case1() {

    }

    @Test
    public void case2() {
        System.out.println(new L200_uf().numIslands(new char[][]{{'1', '1', '0', '0', '1'}, {'1', '1', '0', '1', '1'}, {'0', '1', '1', '1', '0'}, {'0', '0', '0', '1', '1'}}));
    }

    public int numIslands(char[][] grid) {
        int n = grid.length;
        int m = grid[0].length;
        UnionFind uf = new UnionFind(grid);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == '0') {
                    continue;
                }
                grid[i][j] = '0';
                if (i - 1 >= 0 && grid[i - 1][j] == '1') {
                    uf.union(i, j, i - 1, j);
                }
                if (i + 1 < n && grid[i + 1][j] == '1') {
                    uf.union(i, j, i + 1, j);
                }
                if (j + 1 < m && grid[i][j + 1] == '1') {
                    uf.union(i, j, i, j + 1);
                }
                if (j - 1 >= 0 && grid[i][j - 1] == '1') {
                    uf.union(i, j, i, j - 1);
                }
            }
        }
        return uf.count;
    }

    public static class UnionFind {

        final int[] parents;
        final int[] rank;
        final int n;
        final int m;
        int count = 0;

        public UnionFind(char[][] grid) {
            n = grid.length;
            m = grid[0].length;
            parents = new int[n * m];
            rank = new int[n * m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (grid[i][j] == '1') {
                        parents[i * m + j] = i * m + j;
                        count++;
                        rank[i * m + j] = 1;
                    } else {
                        parents[i * m + j] = -1;
                    }
                }
            }
        }

        public void union(int x1, int y1, int x2, int y2) {
            union(x1 * m + y1, x2 * m + y2);
        }

        public void union(int x1, int x2) {
            int p1 = find(x1);
            int p2 = find(x2);
            if (p1 != p2) {
                if (rank[p1] < rank[p2]) {
                    parents[p1] = p2;
                } else if (rank[p1] > rank[p2]) {
                    parents[p2] = p1;
                } else {
                    parents[p2] = p1;
                    rank[p1]++;
                }
                count--;
            }
        }

        public int find(int x) {
            if (x == parents[x]) {
                return x;
            } else {
                parents[x] = find(parents[x]);
                return parents[x];
            }
        }
    }
}
