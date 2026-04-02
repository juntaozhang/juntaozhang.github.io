package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * https://leetcode.cn/problems/design-hashset/
 * L705. 设计哈希集合
 */
public class HashDesignTest {
    public static class MyHashSet {
        private LinkedList[] table;
        private int capacity;

        public MyHashSet() {
            capacity = 5;
            table = new LinkedList[capacity];
        }

        public void rehash(int newCapacity) {
            if (newCapacity < capacity) {
                return;
            }
            LinkedList[] oldTable = table;
            table = new LinkedList[newCapacity];
            for (LinkedList linkedList : oldTable) {
                if (linkedList != null) {
                    for (Object key : linkedList) {
                        add((Integer) key);
                    }
                }
            }
        }

        public int hash(int key) {
            return key % capacity;
        }

        public void add(int key) {
            int hashCode = hash(key);
            LinkedList set = table[hashCode];
            if (set == null) {
                set = new LinkedList<>();
                table[hashCode] = set;
            } else {
                if (set.contains(key)) {
                    return;
                }
            }
            set.add(key);
        }

        public void remove(int key) {
            int hashCode = hash(key);
            LinkedList set = table[hashCode];
            if (set != null) {
                set.remove((Object) key);// 利用 Java 单分派
            }
        }

        public boolean contains(int key) {
            int hashCode = hash(key);
            LinkedList set = table[hashCode];
            if (set == null) {
                return false;
            } else {
                return set.contains(key);
            }
        }
    }

    @Test
    public void case1() {
        MyHashSet myHashSet = new MyHashSet();
        myHashSet.add(1);      // set = [1]
        myHashSet.add(2);      // set = [1, 2]
        myHashSet.contains(1); // 返回 True
        myHashSet.contains(3); // 返回 False ，（未找到）
        myHashSet.add(2);      // set = [1, 2]
        myHashSet.contains(2); // 返回 True
        myHashSet.remove(2);   // set = [1]
        myHashSet.contains(2); // 返回 False ，（已移除）
        System.out.println(myHashSet);
    }
}
