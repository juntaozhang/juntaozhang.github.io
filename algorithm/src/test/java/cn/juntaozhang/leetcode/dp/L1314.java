package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L1314 {


    public int[][] matrixBlockSum(int[][] mat, int k) {
        int rows = mat.length;
        int cols = mat[0].length;
        int[][] dp = new int[rows + 1][cols + 1];
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1] - dp[i - 1][j - 1] + mat[i - 1][j - 1];
            }
        }
        print(dp);
        int[][] ans = new int[rows][cols];
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                int x2 = i + k, y2 = j + k;
                if (x2 > rows) {
                    x2 = rows;
                }
                if (y2 > cols) {
                    y2 = cols;
                }
                int x1 = i - k - 1, y1 = j - k - 1;
                if (x1 < 0) {
                    x1 = 0;
                }
                if (y1 < 0) {
                    y1 = 0;
                }
                ans[i - 1][j - 1] = dp[x2][y2] + dp[x1][y1] - dp[x1][y2] - dp[x2][y1];
            }
        }
        return ans;
    }

    public int[][] matrixBlockSum2(int[][] mat, int k) {
        int[][] dp = new int[mat.length][mat[0].length];
        int[][] ans = new int[mat.length][mat[0].length];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                dp[i][j] = mat[i][j];
                if (i > 0 && j > 0) {
                    dp[i][j] -= dp[i - 1][j - 1];
                }
                if (i > 0) {
                    dp[i][j] += dp[i - 1][j];
                }
                if (j > 0) {
                    dp[i][j] += dp[i][j - 1];
                }
            }
        }
        print(dp);
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                int x1 = Math.max(i - k, 0);
                int y1 = Math.max(j - k, 0);
                int x2 = i + k >= mat.length ? mat.length - 1 : i + k;
                int y2 = j + k >= mat[0].length ? mat[0].length - 1 : j + k;
                ans[i][j] = dp[x2][y2];
                if (x1 - 1 >= 0 && y1 - 1 >= 0) {
                    ans[i][j] += dp[x1 - 1][y1 - 1];
                }
                if (x1 - 1 >= 0) {
                    ans[i][j] -= dp[x1 - 1][y2];
                }
                if (y1 - 1 >= 0) {
                    ans[i][j] -= dp[x2][y1 - 1];
                }
            }
        }
        return ans;
    }


    public static void print(int[][] mat) {
        System.out.println();
        for (int i = 0; i < mat.length; i++) {
//      if(i == 0) {
//        for (int j = 0; j < mat[0].length; j++) {
//          System.out.printf("%02d\t", j);
//        }
//        System.out.println();
//        System.out.println();
//      }
            for (int j = 0; j < mat[0].length; j++) {
                System.out.printf("%01d\t", mat[i][j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] mat = new int[][]{
                {1, 2, 3}, {4, 5, 6}, {7, 8, 9}
        };

        print(mat);
        mat = new L1314().matrixBlockSum(mat, 1);
        print(mat);
    }
}
