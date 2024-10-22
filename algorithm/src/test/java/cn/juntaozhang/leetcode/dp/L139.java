package cn.juntaozhang.leetcode.dp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author juntzhang
 */
public class L139 {

    public boolean wordBreak(String s, List<String> wordDict) {
        boolean[] dp = new boolean[s.length()];
        Set<String> set = new HashSet<>(wordDict);
        dp[0] = set.contains(s.substring(0, 1));
        for (int i = 1; i < s.length(); i++) {
            for (int j = 0; j <= i; j++) {
                if (j == 0) {
                    if (set.contains(s.substring(j, i + 1))) {
                        dp[i] = true;
                        break;
                    }
                } else if (dp[j - 1] && set.contains(s.substring(j, i + 1))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[s.length() - 1];
    }

    public static void main(String[] args) {
        System.out.println("ab".substring(0, 2));
        System.out.println(new L139().wordBreak("ab", List.of("a", "b")));
//    System.out.println(new L139().wordBreak("leetcode", List.of("leet", "code")));
    }
}
