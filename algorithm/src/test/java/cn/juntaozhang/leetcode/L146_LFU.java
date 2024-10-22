package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 按照频率淘汰
 *
 * 
 */
public class L146_LFU {
    @Test
    public void case1() {
        LFUCache lRUCache = new LFUCache(2);
        lRUCache.put(1, 1); // 缓存是 {1=1}
        lRUCache.put(2, 2); // 缓存是 {1=1, 2=2}
        lRUCache.get(2);
        lRUCache.get(2);
        lRUCache.get(1);
        lRUCache.put(3, 3);
        System.out.println(lRUCache.get(1));
        System.out.println(lRUCache.get(3));
    }

    static class LFUCache {
        private final int capacity;
        private final Map<Integer, Node> cache = new LinkedHashMap<>();
        private final PriorityQueue<Node> queue = new PriorityQueue<>((o1, o2) -> o1.freq - o2.freq);

        public LFUCache(int capacity) {
            this.capacity = capacity;
        }

        public void put(int k, int v) {
            Node node = cache.get(k);
            if (node == null) {
                // 一定要放到前面，不然新实例 freq很低，进不了queue
                if (cache.size() >= capacity) {
                    Node old = queue.poll();
                    assert old != null;
                    cache.remove(old.key);
                }

                node = new Node(k, v);
                cache.put(k, node);
                queue.offer(node);

            } else {
                queue.remove(node);
                node.freq++;
                node.value = v;
                queue.offer(node);
            }
        }

        public int get(int k) {
            Node node = cache.get(k);
            if (node == null) {
                return -1;
            }
            queue.remove(node);
            node.freq++;
            queue.offer(node);
            return node.value;
        }


        static class Node {
            int key;
            int value;
            int freq;

            Node(int key, int value) {
                this.key = key;
                this.value = value;
                this.freq = 1;
            }
        }
    }
}
