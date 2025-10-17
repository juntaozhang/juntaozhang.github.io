package cn.juntaozhang.leetcode;

public class FibonacciTest {
    public static void main(String[] args) {
/*
Fibonacci number
Write a code to get  Fibonacci number of a given number.
Fibonacci numbers => 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144
Sample input: 1
sample output: 0 (if index of fibonacci series starts at 1)
Sample input: 4
sample output: 2 (if index of fibonacci series starts at 1)
*/
        System.out.println(fibonacci2(4));
    }

    public static int fibonacci2(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            int temp = b;
            b = a + b;
            a = temp;
        }
        return b;
    }

    public static int fibonacci(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else {
            return fibonacci(i - 1) + fibonacci(i - 2);
        }
    }
}
