package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L433 {

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

    public int minMutation(String start, String end, String[] bank) {
        Deque<Entry> q = new ArrayDeque<>();
        q.offerLast(new Entry(start, 0));
        vist.add(start);
        while (!q.isEmpty()) {
            Entry e = q.pollFirst();
            if (e.node.equals(end)) {
                return e.len;
            }
            for (String next : matchs(e.node, bank)) {
                vist.add(next);
                q.offerLast(new Entry(next, e.len + 1));
            }
        }

        return -1;
    }

    public List<String> matchs(String node, String[] bank) {
        List<String> result = new ArrayList<>();
        for (String n : bank) {
            if (match(node, n) && !vist.contains(n)) {
                result.add(n);
            }
        }
        return result;
    }

    public boolean match(String n1, String n2) {
        int len = n1.length();
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (n1.charAt(i) == n2.charAt(i)) {
                count++;
            }
        }
        return count >= len - 1;
    }

    public static void main(String[] args) {
        String node = "0000";
        for (int i = 0; i < 4; i++) {
            String next = node.substring(0, i) + (char) ((node.charAt(i) - '0' + 10 - 1) % 10 + '0') + node
                    .substring(i + 1, 4);
            next = node.substring(0, i) + (char) ((node.charAt(i) - '0' + 10 + 1) % 10 + '0') + node
                    .substring(i + 1, 4);
            System.out.println(next);
        }

//        System.out.println(new L433().minMutation("AACCGGTT", "AAACGGTA", new String[]{
//                "AACCGGTA", "AACCGCTA", "AAACGGTA"
//        }));

    }
}
