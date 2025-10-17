package cn.juntaozhang.leetcode.greedy;

import java.util.HashMap;
import java.util.Map;

public class L1400 {
    public boolean canConstruct0(String s, int k) {
        if (k > s.length()) return false;
        else if (k == s.length()) return true;
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            map.compute(s.charAt(i), (_k, _v) -> {
                if (_v == null) {
                    _v = 0;
                }
                return ++_v;
            });
        }
        int count = 0;
        for (Integer i : map.values()) {
            if (i % 2 != 0) count++;
        }
        return count <= k;
    }

    public boolean canConstruct(String s, int k) {
        if (k > s.length()) return false;
        else if (k == s.length()) return true;
        int[] map = new int[26];
        for (int i = 0; i < s.length(); i++) {
            ++map[s.charAt(i) - 'a'];
        }
        int count = 0;
        for (Integer i : map) {
            if (i % 2 == 1) count++;
        }
        return count <= k;
    }

    public static void main(String[] args) {
        System.out.println(new L1400().canConstruct("cr", 3));
        System.out.println(new L1400().canConstruct("yzyzyzyzyzyzyzy", 2));
        System.out.println(new L1400().canConstruct("leetcode", 3));
        System.out.println(new L1400().canConstruct("annabelle", 2));
    }
}
