package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L752 {

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

    public int openLock(String[] deadends, String target) {
        Set<String> vist = new HashSet<>(Arrays.asList(deadends));

        Deque<Entry> q = new ArrayDeque<>();
        add2Qeque(vist, q, "0000", 0);

        while (!q.isEmpty()) {
            Entry e = q.pollFirst();
            String node = e.node;
            int len = e.len;
            if (node.equals(target)) {
                return len;
            }
            for (int i = 0; i < 4; i++) {
                StringBuilder sb = new StringBuilder(node);
                sb.setCharAt(i, (char) ((node.charAt(i) - '0' + 10 - 1) % 10 + '0'));
                add2Qeque(vist, q, sb.toString(), len + 1);
                sb.setCharAt(i, (char) ((node.charAt(i) - '0' + 10 + 1) % 10 + '0'));
                add2Qeque(vist, q, sb.toString(), len + 1);
            }
        }
        return -1;
    }

    private void add2Qeque(Set<String> vist, Deque<Entry> q, String node, int len) {
        if (!vist.contains(node)) {
            q.offerLast(new Entry(node, len));
            vist.add(node);
        }
    }

    public static void main(String[] args) {
        System.out.println(new L752().openLock(new String[]{"0000"}, "88888"));
    }


}