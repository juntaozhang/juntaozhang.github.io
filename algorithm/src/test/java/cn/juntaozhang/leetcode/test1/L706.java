package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

public class L706 {
    static class MyHashMap {
        private final int capacity;
        private final Entry[] data;

        public MyHashMap() {
            capacity = 10 ^ 6;
            data = new Entry[capacity];
        }

        public void put(int key, int value) {
            int i = hashCode(key);
            if (data[i] == null) {
                data[i] = new Entry(key, value);
            } else {
                Entry n = data[i];
                while (n != null) {
                    if (n.k == key) {
                        n.v = value;
                        return;
                    }
                    n = n.next;
                }
                n = data[i];
                Entry t = n.next;
                Entry c = new Entry(key, value);
                n.next = c;
                c.next = t;
            }
        }

        public int get(int key) {
            int i = hashCode(key);
            if (data[i] == null) {
                return -1;
            } else {
                Entry n = data[i];
                while (n != null) {
                    if (n.k == key) {
                        return n.v;
                    }
                    n = n.next;
                }
            }
            return -1;
        }

        public void remove(int key) {
            int i = hashCode(key);
            if (data[i] != null) {
                if (data[i].k == key) {
                    data[i] = data[i].next;
                    return;
                }
                Entry n1 = data[i];
                Entry n2 = n1.next;
                while (n2 != null) {
                    if (n2.k == key) {
                        n1.next = n2.next;
                        return;
                    }
                    n1 = n2;
                    n2 = n2.next;
                }
            }
        }

        public int hashCode(int key) {
            return ((Integer) key).hashCode() % capacity;
        }


        static class Entry {
            private final int k;
            private int v;
            private Entry next;

            public Entry(int k, int v) {
                this.k = k;
                this.v = v;
            }

            @Override
            public String toString() {
                return "Entry{" +
                        "k=" + k +
                        ", v=" + v +
                        '}';
            }
        }
    }

    @Test
    public void case1() {
        MyHashMap myHashMap = new MyHashMap();
        myHashMap.put(1, 1); // myHashMap 现在为 [[1,1]]
        myHashMap.put(2, 2); // myHashMap 现在为 [[1,1], [2,2]]
        myHashMap.get(1);    // 返回 1 ，myHashMap 现在为 [[1,1], [2,2]]
        myHashMap.get(3);    // 返回 -1（未找到），myHashMap 现在为 [[1,1], [2,2]]
        myHashMap.put(2, 1); // myHashMap 现在为 [[1,1], [2,1]]（更新已有的值）
        myHashMap.get(2);    // 返回 1 ，myHashMap 现在为 [[1,1], [2,1]]
        myHashMap.remove(2); // 删除键为 2 的数据，myHashMap 现在为 [[1,1]]
        myHashMap.get(2);    // 返回 -1（未找到），myHashMap 现在为 [[1,1]]
    }
}
