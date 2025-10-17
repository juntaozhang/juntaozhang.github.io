package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.HashMap;

/**
 * 
 */
public class L146_LRU_cache {
    class LRUCache {
        HashMap<Integer, Entry> map;
        Entry head, tail;
        int capacity;

        class Entry {
            Integer k;
            Integer v;
            Entry before, after;

            public Entry(Integer k, Integer v) {
                this.k = k;
                this.v = v;
            }

            @Override
            public String toString() {
                return k + "=" + v;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Entry t = head;
            while (t != null) {
                sb.append(t.k).append("=").append(t.v).append(",");
                t = t.after;
            }
            sb.append("}").append("  size=" + map.size());
            return sb.toString();
        }

        public LRUCache(int capacity) {
            map = new HashMap<>();
            this.capacity = capacity;
        }

        public int get(int key) {
            Entry v = remove(key);
            if (v == null) {
                return -1;
            }
            put(key, v);
            return v.v;
        }

        public Entry remove(int key) {
            Entry v = map.remove(key);
            if (v == null) {
                return null;
            }
            Entry b = v.before;
            Entry a = v.after;
            if (b != null) {
                b.after = a;
            }
            if (a != null) {
                a.before = b;
            }
            if (v == head) {
                this.head = a;
            }
            if (v == tail) {
                this.tail = b;
            }
            return v;
        }

        public void put(int key, Entry entry) {
            remove(key);
            if (head == null) {
                this.head = this.tail = entry;
                map.put(key, entry);
            } else {
                entry.after = head;
                this.head.before = entry;
                this.head = entry;
                this.head.before = null; // 特别注意
                map.put(key, entry);
            }
            if (this.map.size() > this.capacity) {
                Entry t = this.tail;
                this.tail = t.before;
                this.tail.after = null; // 特别注意
                map.remove(t.k);
            }

        }

        public void put(int key, int value) {
            Entry entry = new Entry(key, value);
            put(key, entry);
        }
    }

    @Test
    public void case1() {
        LRUCache cache = new LRUCache(10);
//        LinkedHashMap cache = new LinkedHashMap(2, 0.75f, true);
//        cache.put(1, 1);
//        cache.put(2, 2);
//        System.out.println(cache.get(1));       // 返回  1
//        cache.put(3, 3);                         // 该操作会使得密钥 2 作废
//        System.out.println(cache.get(2));       // 返回 -1 (未找到)
//        cache.put(4, 4);                        // 该操作会使得密钥 1 作废
//        System.out.println(cache.get(1));       // 返回 -1 (未找到)
//        System.out.println(cache.get(3));       // 返回  3
//        System.out.println(cache.get(4));       // 返回  4
        for (String a : "10,13],[3,17],[6,11],[10,5],[9,10],[13],[2,19],[2],[3],[5,25],[8],[9,22],[5,5],[1,30],[11],[9,12],[7],[5],[8],[9],[4,30],[9,3],[9],[10],[10],[6,14],[3,1],[3],[10,11],[8],[2,14],[1],[5],[4],[11,4],[12,24],[5,18],[13],[7,23],[8],[12],[3,27],[2,12],[5],[2,9],[13,4],[8,18],[1,7],[6],[9,29],[8,21],[5],[6,30],[1,12],[10],[4,15],[7,22],[11,26],[8,17],[9,29],[5],[3,4],[11,30],[12],[4,29],[3],[9],[6],[3,4],[1],[10],[3,29],[10,28],[1,20],[11,13],[3],[3,12],[3,8],[10,9],[3,26],[8],[7],[5],[13,17],[2,27],[11,15],[12],[9,19],[2,15],[3,16],[1],[12,17],[9,1],[6,19],[4],[5],[5],[8,1],[11,7],[5,2],[9,28],[1],[2,2],[7,4],[4,22],[7,24],[9,26],[13,28],[11,26".split("],\\[")) {
            if ("1".equals(a)) {
                System.out.print("=== ");
            }
            String[] b = a.split(",");
            if (b.length == 1) {
                System.out.print("get " + a);
                System.out.println(" => " + cache.get(Integer.parseInt(b[0])));
            } else {
                System.out.print("put " + a);
                cache.put(Integer.valueOf(b[0]), Integer.valueOf(b[1]));
                System.out.println("null");
            }
            System.out.println(cache.toString());
        }

//        cache.put(2, 1);
//        cache.put(1, 1);
//        cache.put(2, 3);
//        cache.put(4, 1);
//        System.out.println(cache.get(1));
//        System.out.println(cache.get(2));
    }
}
