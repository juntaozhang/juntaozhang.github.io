package cn.juntaozhang.leetcode.greedy;

public class L861 {
    public int matrixScore(int[][] grid) {
        int n = grid.length;
        int m = grid[0].length;
        for (int i = 0; i < n; i++) {
            if (grid[i][0] == 0) {
                for (int j = 0; j < m; j++) {
                    grid[i][j] = grid[i][j] == 0 ? 1 : 0;
                }
            }
        }

        int ans = n * (1 << (m - 1));
        for (int j = 1; j < m; j++) {
            int s = 0;
            for (int[] ints : grid) {
                if (ints[j] == 1) s++;
            }
            ans += (Math.max(n - s, s)) * (1 << (m - j - 1));
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L861().matrixScore(new int[][]{
                {0, 0, 1, 1},
                {1, 0, 1, 0},
                {1, 1, 0, 0}
        }));
        System.out.println(new L861().matrixScore(new int[][]{
                {0}
        }));
    }
}
