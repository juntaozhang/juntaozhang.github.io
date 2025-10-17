package cn.juntaozhang.leetcode;

import org.junit.Test;

public class L344 {
    public void reverseString(char[] s) {
        if (s == null || s.length < 2) {
            return;
        }
        char tmp;
        for (int i = 0, j = s.length; i < j; i++, j--) {
            tmp = s[i];
            s[i] = s[j - 1];
            s[j - 1] = tmp;
        }
    }

    @Test
    public void case1() {
        char[] s = new char[]{'h'};
        reverseString(s);
        System.out.println(s);
    }

    @Test
    public void case2() {
        char[] s = new char[]{'h', 'e'};
        reverseString(s);
        System.out.println(s);
    }

    @Test
    public void case3() {
        char[] s = new char[]{'h', 'e', 'l', 'l', 'o'};
        reverseString(s);
        System.out.println(s);
    }
}
