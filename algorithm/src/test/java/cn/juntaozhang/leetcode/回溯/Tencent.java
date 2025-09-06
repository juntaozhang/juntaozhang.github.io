package cn.juntaozhang.leetcode.回溯;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tencent {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        List<String> result = new ArrayList<>();
        char[] chars = input.toCharArray();
        permute(chars, 0, result);
        System.out.println(result);
    }

    private static void permute(char[] chars, int start, List<String> result) {
        // 1 2 | 3 start = 2
        // 1 | 2 3 start = 1
        // | 1 2 3 start = 0
        if (start == chars.length - 1) {
            result.add(new String(chars));
            return;
        }
        for (int i = start; i < chars.length; i++) {
            swap(chars, start, i);
            /*
               1 2 3 |  start = 3
             */
            /*
                1  2 | 3 start = 2
                1  3 | 2 start = 2
             */
            /*
                1 | 2 3 start = 1
                2 | 1 3 start = 1
                3 | 2 1 start = 1
             */
            permute(chars, start + 1, result);
            swap(chars, start, i);
        }
    }

    private static void swap(char[] chars, int i, int j) {
        if (i == j) return;
        char t = chars[i];
        chars[i] = chars[j];
        chars[j] = t;
    }
}
