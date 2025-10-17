package cn.juntaozhang.leetcode.dp;

/**
 * https://leetcode.cn/problems/longest-palindromic-substring/
 *
 * @author juntzhang
 */
public class L5 {

    public String longestPalindrome(String s) {
        if (s == null || s.length() < 1) {
            return s;
        }
        boolean[][] dp = new boolean[s.length()][s.length()];
        int ans = 0;
        int start = 0;
        int end = 0;
        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j <= i; j++) {
                if (i - j == 0) {
                    dp[i][j] = true;
                } else {
                    boolean b = s.charAt(i) == s.charAt(j);
                    if (i - j == 1) {
                        dp[i][j] = b;
                    } else {
                        dp[i][j] = b && dp[i - 1][j + 1];
                    }
                }
                if (dp[i][j] && ans < i - j) {
                    ans = i - j;
                    start = j;
                    end = i;
                }
            }
        }
        return s.substring(start, end + 1);
    }

    public static void main(String[] args) {
//    System.out.println(new L5().longestPalindrome("babad"));
        System.out.println(new L5().longestPalindrome("cbbd"));
    }
}
