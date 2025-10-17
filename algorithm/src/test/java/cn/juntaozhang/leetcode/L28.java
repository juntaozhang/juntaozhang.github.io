package cn.juntaozhang.leetcode;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

public class L28 {
    public int strStr(String haystack, String needle) {
        for (int skip = 0; skip < haystack.length(); skip++) {
            int i = skip;
            for (int j = 0; j < needle.length() && i < haystack.length(); ) {
                if (haystack.charAt(i) == needle.charAt(j)) {
                    j++;
                    i++;
                    if (j == needle.length()) {
                        return i - needle.length();
                    }
                } else {
                    break;
                }
            }
        }
        return -1;
    }

    public int strStr2(String haystack, String needle) {
        if (needle.isEmpty()) {
            return 0;
        }
        int[] kmp = kmp(needle);
        for (int i = 0, j = 0; i < haystack.length(); i++) {
            while (j > 0 && haystack.charAt(i) != needle.charAt(j)) {
                j = kmp[j - 1];
            }
            if (haystack.charAt(i) == needle.charAt(j)) {
                j++;
            }
            if (j == needle.length()) {
                return i - needle.length() + 1;
            }
        }
        return -1;
    }

    public int[] kmp(String pattern) {
        int[] dp = new int[pattern.length()];
        for (int i = 1, j = 0; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = dp[j - 1];
            }

            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            dp[i] = j;
        }
        return dp;
    }


    @Test
    public void caseKMP() {
        StringUtils.print("abcabeabcabab");
        StringUtils.print(kmp("abcabeabcabab"));
//        StringUtils.print(kmp("abaabcabaa"));
    }

    @Test
    public void case1() {
        System.out.println(strStr("hello", "ll"));
    }


    @Test
    public void case2() {
        System.out.println(strStr2("miiiiippi", "iiiip"));
    }

}
