package cn.juntaozhang.leetcode.greedy;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 贪婪算法
 * <p>
 * https://leetcode.cn/problems/longest-palindrome/submissions/
 *
 * @author juntzhang
 */
public class L409 {

    public int longestPalindrome2(String s) {
        int[] map = new int[26 * 2];
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') {
                map[s.charAt(i) - 'A']++;
            } else if (s.charAt(i) >= 'a' && s.charAt(i) <= 'z') {
                map[s.charAt(i) - 'a' + 26]++;
            }
        }
        boolean flag = false;
        int ans = 0;
        for (int i : map) {
            ans += i;
            if (i % 2 != 0) {
                ans -= 1;
                flag = true;
            }
        }
        if (flag) {
            ans += 1;
        }
        return ans;
    }

    public int longestPalindrome(String s) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        boolean flag = false;
        int ans = 0;
        // 这里体现贪婪算法, 偶数全部，基数-1
        for (int i : map.values()) {
            if (i % 2 != 0) {
                flag = true;
            } else {
                ans += i;
            }
        }
        if (flag) {
            ans += 1;
        }
        return ans;
    }

    @Test
    public void case1() {
        System.out.println(longestPalindrome("abccccdd"));
    }

    @Test
    public void case2() {
        System.out.println((int) 'a');
        System.out.println((int) 'A');
        System.out.println(longestPalindrome("a"));
        System.out.println(longestPalindrome("Ab"));
    }
}
