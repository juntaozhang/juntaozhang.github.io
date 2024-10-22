package cn.juntaozhang.lintcode;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.*;

/**
 * 
 */
public class WordLadder {
    @Test
    public void ladderLength() {
        // "hit","cog", ["hot","dot","dog","lot","log"]
//        System.out.println(ladderLength("hit", "cog", Sets.newHashSet("hot", "dot", "dog", "lot", "log", "cog")));
//        System.out.println(ladderLength("hit", "cog", Sets.newHashSet("hot","dot","dog","lot","log","cog")));

        //"a", "c", ["a","c"]
        System.out.println(ladderLength("a", "c", Sets.newHashSet("b")));

        //"kiss", "tusk", ["miss","dusk","kiss","musk","tusk","diss","disk","sang","ties","muss"]
        //"kiss" -> "diss" -> disk -> dusk
        //"kiss" -> "miss" -> "muss" ->
//        System.out.println(ladderLength("kiss", "tusk", Sets.newHashSet("miss","dusk","kiss","musk","tusk","diss","disk","sang","ties","muss")));
    }

    public int ladderLength(String start, String end, Set<String> dict) {
        Queue<String> q = new LinkedList<>();
        q.offer(start);
        Map<String, Integer> map = new HashMap<>();
        map.put(start, 1);
        while (!q.isEmpty()) {
            String str1 = q.poll();
            //System.out.println(str1);
            if (lessOneStep(str1, end)) {
                //System.out.println(map.get(str1) + 1);
                System.out.println(map);
                return map.get(str1) + (str1.equals(end) ? 0 : 1);
            }
            Iterator<String> it = dict.iterator();
            while (it.hasNext()) {
                String str2 = it.next();
                if (lessOneStep(str1, str2) && !str2.equals(start)) {
                    it.remove();
                    q.offer(str2);
                    map.put(str2, map.get(str1) + 1);
                }
            }
        }
        return -1;
    }

    //==========================================WordLadder II===========================================================

    @Test
    public void findLadders() {
        // "hit","cog", ["hot","dot","dog","lot","log"]
//        System.out.println(ladderLength("hit", "cog", Sets.newHashSet("hot", "dot", "dog", "lot", "log", "cog")));
//        System.out.println(ladderLength("hit", "cog", Sets.newHashSet("hot","dot","dog","lot","log","cog")));

        //"a", "c", ["a","c"]
        System.out.println(findLadders("qa", "sq", Sets.newHashSet("si", "go", "se", "cm", "so", "ph", "mt", "db", "mb", "sb", "kr", "ln", "tm", "le", "av", "sm", "ar", "ci", "ca", "br", "ti", "ba", "to", "ra", "fa", "yo", "ow", "sn", "ya", "cr", "po", "fe", "ho", "ma", "re", "or", "rn", "au", "ur", "rh", "sr", "tc", "lt", "lo", "as", "fr", "nb", "yb", "if", "pb", "ge", "th", "pm", "rb", "sh", "co", "ga", "li", "ha", "hz", "no", "bi", "di", "hi", "qa", "pi", "os", "uh", "wm", "an", "me", "mo", "na", "la", "st", "er", "sc", "ne", "mn", "mi", "am", "ex", "pt", "io", "be", "fm", "ta", "tb", "ni", "mr", "pa", "he", "lr", "sq", "ye")));

        //"kiss", "tusk", ["miss","dusk","kiss","musk","tusk","diss","disk","sang","ties","muss"]
        //"kiss" -> "diss" -> disk -> dusk
        //"kiss" -> "miss" -> "muss" ->
//        System.out.println(ladderLength("kiss", "tusk", Sets.newHashSet("miss","dusk","kiss","musk","tusk","diss","disk","sang","ties","muss")));
    }

    public List<List<String>> findLadders(String start, String end, Set<String> dict) {
        Queue<String> q = new LinkedList<>();
        Map<String, List<List<String>>> map = new HashMap<>();
        q.offer(start);
        int len = -1;
        List<List<String>> res = new ArrayList<>();
        while (!q.isEmpty()) {
            String str1 = q.poll();
            if (str1.equals("si")) {
                System.out.println();
            }
            List<List<String>> lists = map.get(str1);
            if (lists == null) {
                lists = new ArrayList<>();
                lists.add(new ArrayList<>());
                map.put(str1, lists);
            }
            for (List<String> l : lists) {
                l.add(str1);
            }
            //final
            if (lessOneStep(str1, end)) {
                for (List<String> l : lists) {
                    List<String> t = new ArrayList<>(l);
                    if (!str1.equals(end)) {
                        t.add(end);
                    }
                    if (len == -1) {
                        len = t.size();
                    }
                    if (t.size() == len) {
                        res.add(t);
                    }
                }
            }
            for(String str2 : dict) {
                if (str2.equals(start) || str2.equals(end)) {
                    continue;
                }
                if (lessOneStep(str1, str2)) {
                    List<List<String>> lists2 = map.get(str2);
                    if (lists2 == null) {
                        lists2 = new ArrayList<>();
                        map.put(str2, lists2);
                    }
                    for (List<String> l : lists) {
                        lists2.add(new ArrayList<>(l));
                    }
                    q.offer(str2);
                }
            }
        }
        return res;
    }

    private boolean lessOneStep(String str1, String str2) {
        int diff = 0;
        for (int i = 0; i < str1.length(); i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                diff += 1;
            }
            if (diff > 1) {
                return false;
            }
        }
        return true;
    }
}
