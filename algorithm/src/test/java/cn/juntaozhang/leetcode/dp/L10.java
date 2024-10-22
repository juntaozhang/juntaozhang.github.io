package cn.juntaozhang.leetcode.dp;

public class L10 {
    public boolean isMatch(String s0, String p0) {
        char[] s = s0.toCharArray();
        char[] p = p0.toCharArray();
        int sn = s.length;
        int pn = p.length;
        boolean[][] dp = new boolean[sn + 1][pn + 1];
        dp[0][0] = true;
        for (int j = 1; j <= pn; j++) {
            if (p[j - 1] == '*') {
                dp[0][j] = dp[0][j - 2];
            }
        }
        for (int i = 1; i <= sn; i++) {
            for (int j = 1; j <= pn; j++) {
                if (p[j - 1] == '.' || p[j - 1] == s[i - 1]) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else if (p[j - 1] == '*') {
                    if (p[j - 2] == '.' || s[i - 1] == p[j - 2]) {
                        dp[i][j] = dp[i][j - 2] || dp[i][j - 1] || dp[i - 1][j];
                    } else {
                        dp[i][j] = dp[i][j - 2]; // a* counts as empty
                    }

                }
            }
        }
        return dp[sn][pn];
    }

    public static void main(String[] args) {
        System.out.println(new L10().isMatch("ssippi", "s*p*."));
        System.out.println(new L10().isMatch("aaa", "c*.*b"));
        System.out.println(new L10().isMatch("aaa", "c*a*b"));
        System.out.println(new L10().isMatch("aab", "c*a*b"));
        System.out.println(new L10().isMatch("aa", ".*"));
        System.out.println(new L10().isMatch("aa", "a*"));
        System.out.println(new L10().isMatch("aa", "a"));
    }
}
