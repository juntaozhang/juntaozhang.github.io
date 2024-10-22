package cn.juntaozhang.leetcode.bytedance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Scanner;

public class 数组组成最大数 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String string = scanner.nextLine();
        System.out.println(new 数组组成最大数().generate2(string.substring(1, string.length() - 1).split(",")));
    }


    public String generate2(String[] arr) {
        Arrays.sort(arr, (a, b) -> (a + b).compareTo(b + a));
        return String.join("", arr);
    }

    public String generate(String[] arr) {
        Arrays.sort(arr, (a, b) -> {
                    int i = a.length() - 1, j = b.length() - 1;
                    while (true) {
                        if (a.charAt(i) > b.charAt(j)) {
                            return -1;
                        } else if (a.charAt(i) < b.charAt(j)) {
                            return 1;
                        } else {
                            if (i > 0 && j > 0) {
                                i--;
                                j--;
                            } else if (i == 0 && j > 0) {
                                j--;
                            } else if (i > 0 && j == 0) {
                                i--;
                            } else {
                                break;
                            }
                        }
                    }
                    return 0;
                }
        );
        return String.join("", arr);
    }

    @Test
    public void case1() {
        System.out.println(generate(new String[]{"3", "30", "34", "5", "9"}));
    }
}
