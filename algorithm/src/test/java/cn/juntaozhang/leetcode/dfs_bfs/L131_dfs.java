package cn.juntaozhang.leetcode.dfs_bfs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * L409
 *
 * 中等难度
 * 给你一个字符串 s，请你将 s 分割成一些 子串，使每个子串都是 回文串 。返回 s 所有可能的分割方案。
 * 添加要保留顺序 ab=>[a, b] aab=>[a, a, b],[aa, b]
 */
public class L131_dfs {
    List<List<String>> ans = new ArrayList<>();
    List<String> tmp = new ArrayList<>();
    boolean[][] palindrome;

    public List<List<String>> partition(String s) {
        int n = s.length();

        /*
            a  b  c  c  b  a
         a  1  0  0  0  0  1
         b  x  1  0  0  1  0
         c  x  x  1  1  0  0
         c  x  x  x  1  0  0
         b  x  x  x  x  1  0
         a  x  x  x  x  x  1
         */
        palindrome = new boolean[n][n];
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

    @Test
    public void case1() {
        // [[a, b, b, a, a, c], [a, b, b, aa, c], [a, bb, a, a, c], [a, bb, aa, c], [abba, a, c]]
        // [[b, b, a, a, c], [b, b, aa, c], [bb, a, a, c], [bb, aa, c]]
        // [[b, a, a, c], [b, aa, c]]
        System.out.println(partition("acc"));
    }
}
