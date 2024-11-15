package cn.juntaozhang.leetcode.bytedance;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.LinkedList;

public class MoveZeros {
    public void move(int[] arr) {
        int index = 0;

        for (int num : arr) {
            if (num != 0) {
                arr[index++] = num;
            }
        }

        while (index < arr.length) {
            arr[index++] = 0;
        }
    }

    public void move2(LinkedList<Integer> list) {
        int size = list.size();
        LinkedList<Integer> result = new LinkedList<>();
        while (!list.isEmpty()) {
            int num = list.poll();
            if (num != 0) {
                result.addLast(num);
            }
        }
        while (result.size() < size) {
            result.add(0);
        }
        list.addAll(result);
    }

    @Test
    public void test_arr() {
        int[] arr = new int[]{0, 1, 0, 3, 0, 0, 2};
        move(arr);
        StringUtils.print(arr);
    }

    @Test
    public void test_linkedlist() {
        LinkedList<Integer> list = new LinkedList<>();
        list.add(0);
        list.add(1);
        list.add(0);
        list.add(3);
        list.add(0);
        list.add(0);
        list.add(2);
        move2(list);
        StringUtils.print(list);
    }
}
