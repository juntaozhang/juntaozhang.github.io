package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L127 {

    static class Entry {

        public String node;
        public int len;

        public Entry(String node, int len) {
            this.node = node;
            this.len = len;
        }

        @Override
        public String toString() {
            return node + "," + len;
        }
    }

    Set<String> vist = new HashSet<>();
    Map<String, List<String>> relations = new HashMap<>();

    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        buildRelation(beginWord);
        for (String w : wordList) {
            buildRelation(w);
        }
        Deque<Entry> q = new ArrayDeque<>();
        q.offerLast(new Entry(beginWord, 1));
        vist.add(beginWord);
        while (!q.isEmpty()) {
            Entry e = q.pollFirst();
            if (e.node.equals(endWord)) {
                return e.len;
            }
            for (String pattern : relations.get(e.node)) {
                if (!vist.contains(pattern)) {
                    vist.add(pattern);
                    for (String next : relations.get(pattern)) {
                        vist.add(next);
                        q.offerLast(new Entry(next, e.len + 1));
                    }
                }

            }
        }

        return 0;
    }


    public void buildRelation(String word) {
        final char[] array = word.toCharArray();
        for (int i = 0; i < word.length(); i++) {
            char bak = array[i];
            array[i] = '*';
            final String pattern = new String(array);
            relations.compute(word, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(pattern);
                return v;
            });

            relations.compute(pattern, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(word);
                return v;
            });
            array[i] = bak;
        }
    }

    public static void main(String[] args) {
        System.out.println(new L127().ladderLength("hit", "cog", Arrays.asList(
                "hot", "dot", "dog", "lot", "log"
        )));
    }
}
