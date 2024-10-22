package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class SubstringAnagrams {
    public List<Integer> findAnagrams(String s, String p) {
        // Write your code here
        List<Integer> ans = new ArrayList<Integer>();
        int[] sum = new int[30];

        int plength = p.length(), slength = s.length();
        for (char c : p.toCharArray()) {
            sum[c - 'a']++;
        }

        int start = 0, end = 0, matched = 0;
        while (end < slength) {
            if (sum[s.charAt(end) - 'a'] >= 1) {
                matched++;
            }
            sum[s.charAt(end) - 'a']--;
            end++;
            if (matched == plength) {
                ans.add(start);
            }
            if (end - start == plength) {
                if (sum[s.charAt(start) - 'a'] >= 0) {
                    matched--;
                }
                sum[s.charAt(start) - 'a']++;
                start++;
            }
        }
        return ans;
    }
    public List<Integer> findAnagrams2(String s, String p) {
        List<Integer> res = new ArrayList<>();
        int plen = p.length();
        int[] check = new int[30];
        for (int i = 0; i < plen; i++) {
            check[p.charAt(i) - 'a'] += 1;
        }
        int distance = p.length();
        for (int start = 0, end = 0; end < s.length();) {
            if (check[s.charAt(end) - 'a'] > 0) {
                distance --;
            }
            check[s.charAt(end) - 'a'] -= 1;
            if (distance == 0) {
                res.add(start);
            }
            end++;
            if (end - start == plen) {
                if (check[s.charAt(start) - 'a'] >= 0) {
                    distance ++;
                }
                check[s.charAt(start) - 'a'] += 1;
                start++;
            }
        }
        return res;
    }

    @Test
    public void findAnagrams() {
        System.out.println(findAnagrams2("cbaebabacd", "abc"));
    }
}
