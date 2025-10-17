package cn.juntaozhang.leetcode.dfs_bfs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author juntzhang
 */
public class L121_dfs {
    List<List<String>> ans = new ArrayList<>();
    List<String> tmp = new ArrayList<>();
    boolean[][] palindrome;

    public List<List<String>> partition(String s) {
        int n = s.length();
        palindrome = new boolean[n][n];
        int[] dp = new int[n];

        for (int i = n - 1; i >= 0; i--) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    palindrome[i][j] = true;
                } else if (j - i == 1) {
                    palindrome[i][j] = s.charAt(i) == s.charAt(j);
                } else {
                    palindrome[i][j] = palindrome[i + 1][j - 1] && s.charAt(i) == s.charAt(j);
                }
            }
        }
        dfs(s, 0, n - 1, tmp);
        return ans;
    }

    // aabbaac  => a abbaac,aa bbaac,aabbaa c
    // a abbaac => a bbaac,abba ac
    // bbaac    => b baac,bb aac
    // baac     => b aac
    // aac      => a ac,aa c
    private void dfs(String s, int start, int end, List<String> tmp) {
        if (start > end) {
            ans.add(new ArrayList<>(tmp));
            return;
        }
        for (int i = start; i <= end; i++) {
            if (palindrome[start][i]) {
                tmp.add(s.substring(start, i + 1));
                dfs(s, i + 1, end, tmp);
                tmp.remove(tmp.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new L121_dfs().partition("aab"));
    }
}
