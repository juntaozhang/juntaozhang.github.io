package cn.juntaozhang.leetcode.sort;

import org.junit.Test;

import java.util.*;

/**
 * https://leetcode.cn/problems/top-k-frequent-elements/submissions/
 *
 * @author juntzhang
 */
public class L347_前K个高频元素 {

    @Test
    public void testBigHeap() {
        // 大根堆 big heap
        PriorityQueue<Integer> queue = new PriorityQueue<>((o1, o2) -> o2.compareTo(o1));
        /*
         ==> add 5
                10                   10                     10
             6      9     ===>    6      9     ===>      6      9
           1   4  3   7         1   4  3   7           5   4  3   7
                              5                      1

         ==> poll 10
                 10                  1                    9
              6      9    ===>    6      9     ===>    6      7
            5   4  3   7        5   4  3   7         5   4  3   1
          1
         */
        queue.add(1);
        queue.add(3);
        queue.add(4);
        queue.add(6);
        queue.add(7);
        queue.add(9);
        queue.add(10);
        queue.add(5);

        System.out.println("poll:" + queue.poll()); // 10

        int[] result = new L347_前K个高频元素().topKFrequent2(new int[]{1, 6, 6, 1, 4, 6, 1, 2, 2}, 2);
        for (int i : result) {
            System.out.println(i);
        }
    }

    @Test
    public void case1() {
        int[] result = topKFrequent(new int[]{1, 1, 1, 2, 2, 3, 3, 3, 3, 3}, 2);
        for (int i : result) {
            System.out.println(i);
        }
    }

    public int[] topKFrequent2(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int n : nums) {
            map.put(n, map.getOrDefault(n, 0) + 1);
        }
        List<Integer> list = new ArrayList<>(map.keySet());
        // n * log(n)
        list.sort((o1, o2) -> map.get(o2) - map.get(o1));
        int[] result = new int[k];
        for (int i = 0; i < k; i++) {
            Integer num = list.get(i);
            if (num != null) {
                result[i] = num;
            }
        }
        return result;
    }

    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int n : nums) {
            freqMap.put(n, freqMap.getOrDefault(n, 0) + 1);
        }

        PriorityQueue<Integer> queue = new PriorityQueue<>((i1, i2) -> freqMap.get(i2) - freqMap.get(i1));
        int[] result = new int[k];
        for (Integer key : freqMap.keySet()) {
            queue.offer(key);
        }
        for (int i = 0; i < k; i++) {
            Integer num = queue.poll();
            if (num != null) {
                result[i] = num;
            }
        }
        return result;
    }
}
