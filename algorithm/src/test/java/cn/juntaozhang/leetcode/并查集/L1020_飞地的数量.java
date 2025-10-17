package cn.juntaozhang.leetcode.并查集;

/**
 * @author juntzhang
 */
public class L1020_飞地的数量 {

    public static void main(String[] args) {
        System.out.println(
                new L1020_飞地的数量().numEnclaves(
                        new int[][]{
                                {1, 1, 0, 0, 1},
                                {1, 0, 1, 0, 1},
                                {0, 0, 0, 1, 0},
                                {0, 0, 0, 0, 0}
                        }
                )
        );
    }


    public int numEnclaves(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        UnionFind uf = new UnionFind(grid);

        int[][] directions = new int[][]{{1, 0}, {0, 1}};

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 1) {
                    int x, y;
                    for (int[] d : directions) {
                        x = i + d[0];
                        y = j + d[1];
                        if (x >= 0 && y >= 0 && x < rows && y < cols && grid[x][y] == 1) {
                            uf.union(i, j, x, y);
                        }
                    }
                }
            }
        }
        int ans = 0;
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (grid[i][j] == 1 && !uf.isEdge(i * cols + j)) {
                    ans++;
                }
            }
        }

        return ans;
    }


    static class UnionFind {
        private final int rows;
        private final int cols;
        private final int[] parents;
        private final int[] ranks;
        private final boolean[] edges;
        private int size = 0;

        public UnionFind(int[][] grid) {
            this.rows = grid.length;
            this.cols = grid[0].length;
            this.parents = new int[this.rows * this.cols];
            this.ranks = new int[this.rows * this.cols];
            this.edges = new boolean[this.rows * this.cols];

            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    if (grid[i][j] == 1) {
                        this.parents[i * cols + j] = i * cols + j;
                        this.ranks[i * cols + j] = 1;
                        this.size++;
                    } else {
                        this.parents[i * cols + j] = -1;
                        this.ranks[i * cols + j] = 0;
                    }
                    if (i == 0 || j == 0 || i == this.rows - 1 || j == this.cols - 1) {
                        this.edges[i * cols + j] = true;
                    }
                }
            }
        }

        public boolean isEdge(int x) {
            return edges[find(x)];
        }

        public int find(int x) {
            int p = parents[x];
            if (p == x) {
                return p;
            }
            parents[x] = find(p);
            return parents[x];
        }

        public void union(int x1, int y1, int x2, int y2) {
            union(x1 * this.cols + y1, x2 * this.cols + y2);
        }

        public void union(int x1, int x2) {
            int p1 = find(x1);
            int p2 = find(x2);
            if (p1 != p2) {
                if (this.edges[p1] || this.edges[p2]) {
                    this.edges[p1] = true;
                    this.edges[p2] = true;
                }
                if (this.ranks[p1] > this.ranks[p2]) {
                    this.parents[p2] = p1;
                } else if (this.ranks[p1] < this.ranks[p2]) {
                    this.parents[p1] = p2;
                } else {
                    this.parents[p1] = p2;
                    this.ranks[p2]++;
                }
                this.size--;
            }
        }
    }
}