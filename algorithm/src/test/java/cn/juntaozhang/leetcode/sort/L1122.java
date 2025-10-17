package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class L1122 {
    public static int[] relativeSortArray(int[] arr1, int[] arr2) {
        if (arr1.length == 0) {
            return arr1;
        }
        int max = Arrays.stream(arr1).max().getAsInt();
        int[] res = new int[arr1.length];
        int[] count = new int[max + 1];
        for (int num : arr1) {
            count[num]++;
        }
        int i = 0;
        for (int x : arr2) {
            for (int j = 0; j < count[x]; j++) {
                res[i++] = x;
            }
            count[x] = 0;
        }

        for (int x = 0; x <= max; x++) {
            for (int j = 0; j < count[x]; j++) {
                res[i++] = x;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(
                relativeSortArray(
                        new int[]{2, 3, 1, 3, 2, 4, 6, 7, 9, 2, 19},
                        //        0  1  2
                        new int[]{2, 1, 4, 3, 9, 6}
                )
        ));
        System.out.println(Arrays.toString(relativeSortArray(new int[]{2}, new int[]{2})));
    }
}
