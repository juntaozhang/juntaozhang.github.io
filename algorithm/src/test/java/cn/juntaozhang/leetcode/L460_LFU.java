package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 按照频率淘汰
 *
 *
 */
public class L460_LFU {
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

    // 特别注意： 频率相同，按时间淘汰
    @Test
    public void case2() {
        // [[3],[1,1],[2,2],[3,3],[4,4], [4],[3],[2],[1],[5,5],[1],[2],[3],[4],[5]]
        // [     null,null,null,null,null,4, 3,  2,  -1,  null,-1, 2,  3,  -1, 5]
        LFUCache lRUCache = new LFUCache(3);
        lRUCache.put(1, 1);
        lRUCache.put(2, 2);
        lRUCache.put(3, 3);
        lRUCache.put(4, 4);
        System.out.println(lRUCache.get(4));
        System.out.println(lRUCache.get(3));
        System.out.println(lRUCache.get(2));
        System.out.println(lRUCache.get(1));
        lRUCache.put(5, 5);
        System.out.println(lRUCache.get(1));
        System.out.println(lRUCache.get(2));
        System.out.println(lRUCache.get(3));
        System.out.println(lRUCache.get(4));
        System.out.println(lRUCache.get(5));
    }

    static class LFUCache {
        private final int capacity;
        private final Map<Integer, Node> cache = new LinkedHashMap<>();
        private final PriorityQueue<Node> queue = new PriorityQueue<>((o1, o2) -> {
            int f = o1.freq - o2.freq;
            return f == 0 ? (int) (o1.time - o2.time) : f;
        });

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
                node.time = System.nanoTime();
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
            node.time = System.nanoTime();
            queue.offer(node);
            return node.value;
        }


        public static class Node {
            int key;
            int value;
            int freq;
            long time;

            Node(int key, int value) {
                this.key = key;
                this.value = value;
                this.freq = 1;
                this.time = System.nanoTime();
            }
        }
    }
}
