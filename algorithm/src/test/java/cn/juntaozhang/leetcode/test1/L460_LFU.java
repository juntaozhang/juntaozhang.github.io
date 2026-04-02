package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class L460_LFU {
    @Test
    public void case2() {
        // [[3],[1,1],[2,2],[3,3],[4,4], [4],[3],[2],[1],[5,5],[1],[2],[3],[4],[5]]
        // [     null,null,null,null,null,4, 3,  2,  -1,  null,-1, 2,  3,  -1, 5]
        LFUCache cache = new LFUCache(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        System.out.println(cache.get(4));
        System.out.println(cache.get(3));
        System.out.println(cache.get(2));
        System.out.println(cache.get(1));
        cache.put(5, 5);
        System.out.println(cache.get(1));
        System.out.println(cache.get(2));
        System.out.println(cache.get(3));
        System.out.println(cache.get(4));
        System.out.println(cache.get(5));
    }

    static class LFUCache {
        static class Entry {
            int key;
            int value;
            int freq;

            Entry(int key, int value) {
                this.key = key;
                this.value = value;
                this.freq = 1;
            }

            public void inc() {
                freq++;
            }

            @Override
            public String toString() {
                return "Entry{" +
                        "key=" + key +
                        ", value=" + value +
                        ", freq=" + freq +
                        '}';
            }
        }

        private PriorityQueue<Entry> queue;
        private int capacity;
        private Map<Integer, Entry> map;

        public LFUCache(int capacity) {
            this.queue = new PriorityQueue<>(capacity, (a, b) -> a.freq - b.freq);
            this.capacity = capacity;
            this.map = new HashMap<>();
        }

        public int get(int key) {
            Entry entry = map.get(key);
            if (entry != null) {
                entry.inc();
                queue.remove(entry);
                queue.offer(entry);
                return entry.value;
            }
            return -1;
        }

        public void put(int key, int value) {
            Entry entry = map.get(key);
            if (entry != null) {
                entry.inc();
                entry.value = value;
                queue.remove(entry);
                queue.offer(entry);
            } else {
                if (map.size() >= capacity) {
                    Entry poll = queue.poll();
                    map.remove(poll.key);
                }

                entry = new Entry(key, value);
                map.put(key, entry);
                queue.offer(entry);
            }
        }
    }
}
