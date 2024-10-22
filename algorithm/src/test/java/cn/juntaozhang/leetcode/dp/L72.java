package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L72 {

    public int minDistance(String word1, String word2) {
        int n = word2.length(), m = word1.length();
        if (n == 0 && m == 0) return 0;
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                boolean flag = word2.charAt(i - 1) == word1.charAt(j - 1);
                dp[i][j] = Math.min(flag ? dp[i - 1][j - 1] - 1 : dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
            }
        }
        print(dp, word1, word2);
        return dp[n][m];
    }

    public static void print(int[][] mat, String word1, String word2) {
        System.out.println();
        for (int i = 0; i < mat.length; i++) {
//      if (i == 0) {
//        System.out.print("  \t");
//        for (int j = 0; j < mat[0].length; j++) {
//          System.out.printf("% 2d\t", j);
//        }
//        System.out.println();
//        System.out.print("  \t");
//        for (int j = 0; j < mat[0].length; j++) {
//          System.out.printf(" %s\t", word1.charAt(j));
//        }
//        System.out.println();
//      }
//      System.out.printf(" %s\t", word2.charAt(i));
            for (int j = 0; j < mat[0].length; j++) {
                System.out.printf("%02d\t", mat[i][j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println(new L72().minDistance(
                "horse",
                "ros"
        ));
//    System.out.println(new L72().minDistance(
//        "pneumonou",
//        "u"
//    ));
    }
}
/*
pneumono ultramicroscop ic
         ultramicroscop ic

 */