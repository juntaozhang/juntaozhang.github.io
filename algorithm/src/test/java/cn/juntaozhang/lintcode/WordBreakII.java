package cn.juntaozhang.lintcode;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.*;

/**
 * 
 */
public class WordBreakII {
    @Test
    public void wordBreakI() {
        System.out.println(wordBreakI("aaa", Sets.newHashSet("a","aa","aaa")));
    }

    public boolean wordBreakI(String s, Set<String> dict) {
        int mDL = 0;
        for (String w : dict) {
            mDL = Math.max(mDL, w.length());
        }
        boolean[] canBreak = new boolean[s.length() + 1];
        canBreak[0] = true;
        for (int i = 1; i < canBreak.length; i++) {
            canBreak[i] = false;
            for (int j = 1; j <= mDL && j <= i; j++) {
                if (!canBreak[i - j]) {
                    continue;
                }
                String w = s.substring(i - j, i);
                if (dict.contains(w)) {
                    canBreak[i] = true;
                    break;
                }
            }
        }
        return canBreak[s.length()];
    }



    int count = 0;

    @Test
    public void wordBreakII() {
//        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ["a","aa","aaa","aaaa","aaaaa","aaaaaa","aaaaaaa","aaaaaaaa","aaaaaaaaa","aaaaaaaaaa"]
        System.out.println(
                wordBreak("aaaaaaaaaa", Sets.newHashSet("a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "aaaaaaa", "aaaaaaaa", "aaaaaaaaa", "aaaaaaaaaa"))
        );
        System.out.println(count);
    }

    Map<String, List<String>> dict = new HashMap<>();
    Set<String> wordDict;

    public List<String> wordBreak2(String s, Set<String> wordDict) {
        this.wordDict = wordDict;
        return dfs(s);
    }

    private List<String> dfs(String s) {
        List<String> res = dict.get(s);
        if (res == null) {
            res = new ArrayList<>();
        } else {
            return res;
        }
        for (int i = 1; i <= s.length(); i++) {
            String prefix = s.substring(0, i);
            if (wordDict.contains(prefix)) {
                if (i == s.length()) {
                    res.add(prefix);
                } else {
                    String suffix = s.substring(i);
                    List<String> t = dfs(suffix);
                    for (String str : t) {
                        res.add(prefix + " " + str);
                    }
                }
            }
        }
        dict.put(s, res);
        return res;
    }

    public ArrayList<String> wordBreak(String s, Set<String> dict) {
        // Note: The Solution object is instantiated only once and is reused by each test case.
        Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        return wordBreakHelper(s, dict, map);
    }

    public ArrayList<String> wordBreakHelper(String s, Set<String> dict, Map<String, ArrayList<String>> memo) {
        if (memo.containsKey(s)) return memo.get(s);
        ArrayList<String> result = new ArrayList<String>();
        int n = s.length();
        if (n <= 0) return result;
        for (int len = 1; len <= n; ++len) {
            String subfix = s.substring(0, len);
            if (dict.contains(subfix)) {
                if (len == n) {
                    result.add(subfix);
                } else {
                    String prefix = s.substring(len);
                    ArrayList<String> tmp = wordBreakHelper(prefix, dict, memo);
                    for (String item : tmp) {
                        item = subfix + " " + item;
                        result.add(item);
                    }
                }
            }
        }
        memo.put(s, result);
        return result;
    }


//    private List<String> res = new ArrayList<>();
//    private String s;
//    private Map<Character, List<String>> dict = new HashMap<>();
//    private Set<String> wordDict;
//    public List<String> wordBreak2(String s, Set<String> wordDict) {
//        this.wordDict = wordDict;
//        this.s = s;
//        for (String w : wordDict) {
//            if (w == null || "".equals(w)) {
//                continue;
//            }
//            List<String> t = dict.get(w.charAt(0));
//            if (t == null) {
//                t = new ArrayList<>();
//                dict.put(w.charAt(0), t);
//            }
//            t.add(w);
//        }
//        dfs(0, new ArrayList<String>());
//        System.out.println(count);
//        return res;
//    }
//
//    private void dfs(int index, List<String> candidate) {
//        if (index == s.length()) {
//            StringBuilder str = new StringBuilder(candidate.get(0));
//            for (int i = 1; i < candidate.size(); i++) {
//                str.append(" ").append(candidate.get(i));
//            }
//
//            res.add(str.toString());
//            return;
//        }
//
//        List<String> words = dict.get(s.charAt(index));
//        if (words != null) {
//            for (String w : words) {
//                count++;
//                if (candidate.contains(w)) {
//                    continue;
//                }
//                if (s.indexOf(w, index) != -1) {
//                    List<String> t = new ArrayList<>(candidate);
//                    t.add(w);
//                    dfs(w.length() + index, t);
//                }
//            }
//        }
//    }

}
