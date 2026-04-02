package cn.juntaozhang.leetcode.sort;

import org.junit.Test;

import java.util.Arrays;

public class MyTest {
    @Test
    public void test() {
        sort(new int[]{5, 2, 3, 1, 7, 4, 6});
    }

    public void sort(int[] arr) {
        for (int i = 1; i < arr.length - 1; i++) {
            for (int j = i; j > 0; j--) {
                if (arr[j] < arr[j - 1]) {
                    swap(arr, j, j - 1);
                }
            }
            System.out.println(i + ":" + Arrays.toString(arr));
        }
    }

    public void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
