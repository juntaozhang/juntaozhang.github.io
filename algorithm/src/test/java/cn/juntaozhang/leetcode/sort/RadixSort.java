package cn.juntaozhang.leetcode.sort;

import java.util.*;

/**
 * 如何处理负数:
 */
public class RadixSort {
    public static int[] radixSort(int[] arr) {
        if (arr.length == 0) return arr;
        int max = Arrays.stream(arr).max().getAsInt();
        int maxDigitLength = 0;
        while (max != 0) {
            maxDigitLength++;
            max /= 10;
        }
        Map<Integer, List<Integer>> map = new HashMap<>();
        int mod = 1;
        for (int i = 1; i <= maxDigitLength; i++) {
            for (int num : arr) {
                map.compute(num / mod % 10 + 9, (k, v) -> {
                    if (v == null) v = new ArrayList<>();
                    v.add(num);
                    return v;
                });
            }
            for (int k = 0, j = 0; k <= 18; k++) {
                if (map.get(k) != null) {
                    for (Integer num : map.get(k)) {
                        arr[j++] = num;
                    }
                }
            }
            mod *= 10;
            map.clear();
        }
        return arr;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(radixSort(new int[]{
                8, -235, -20, 50, -527, 138, 156
        })));
    }
}
