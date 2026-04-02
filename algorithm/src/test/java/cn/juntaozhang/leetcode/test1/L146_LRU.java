package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class L146_LRU {
    static class LRUCache {
        public static class Entry {
            int key;
            int value;
            Entry before, after;

            public Entry(int key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        private Map<Integer, Entry> cache;
        private int capacity;
        private Entry root;
        private Entry tail;

        public LRUCache(int capacity) {
            this.cache = new HashMap<>();
            this.capacity = capacity;
            this.root = null;
            this.tail = null;
        }

        public void fresh(Entry entry) {
            if (entry == root) {
                return;
            }
            if (root == null) {
                root = entry;
                tail = entry;
                return;
            }
            Entry before = entry.before;
            entry.before = null;
            Entry after = entry.after;
            entry.after = root;

            if (entry == tail) {
                tail = before;
            }
            if (before != null) {
                before.after = after;
            }
            if (after != null) {
                after.before = before;
            }

            root.before = entry;
            root = entry;

        }

        public int get(int key) {
            Entry entry = cache.get(key);
            if (entry != null) {
                fresh(entry);
                return entry.value;
            }
            return -1;
        }

        private void add(int key, int value) {
            Entry entry = new Entry(key, value);
            if (tail == null) {
                tail = entry;
                root = entry;
            } else {
                entry.before = null;
                entry.after = root;
                root.before = entry;
                root = entry;
            }
            cache.put(key, entry);
        }

        public void put(int key, int value) {
            Entry entry = cache.get(key);
            if (entry != null) {
                entry.value = value;
                fresh(entry);
                return;
            }

            if (cache.size() >= capacity) {
                entry = tail.before;
                cache.remove(tail.key);
                entry.after = null;
                tail = entry;
            }
            add(key, value);
        }
    }

    @Test
    public void case2() {
        LRUCache lRUCache = new LRUCache(2);
        lRUCache.put(1, 1); // 缓存是 {1=1}
        lRUCache.put(2, 2); // 缓存是 {1=1, 2=2}
        lRUCache.get(1);    // 返回 1
        lRUCache.put(3, 3); // 该操作会使得关键字 2 作废，缓存是 {1=1, 3=3}
        lRUCache.get(2);    // 返回 -1 (未找到)
        lRUCache.put(4, 4); // 该操作会使得关键字 1 作废，缓存是 {4=4, 3=3}
        lRUCache.get(1);    // 返回 -1 (未找到)
        lRUCache.get(3);    // 返回 3
        lRUCache.get(4);    // 返回 4
    }
}
