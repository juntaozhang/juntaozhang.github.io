package cn.juntaozhang.leetcode.greedy;

import java.util.Deque;
import java.util.LinkedList;

public class L402 {
    public String removeKdigits(String num, int k) {
        Deque<Character> deque = new LinkedList<>();
        for (char c : num.toCharArray()) {
            while (!deque.isEmpty() && deque.peekLast() > c && k > 0) {
                deque.pollLast();
                k--;
            }
            deque.offerLast(c);
        }
        while (!deque.isEmpty() && k > 0) {
            deque.pollLast();
            k--;
        }

        StringBuilder builder = new StringBuilder();
        if (deque.isEmpty()) {
            return "0";
        } else {
            for (Character c : deque) {
                if (c != '0' || builder.length() != 0) {
                    builder.append(c);
                }
            }
        }
        if (builder.length() == 0) {
            builder.append('0');
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println(new L402().removeKdigits("112", 1));
        System.out.println(new L402().removeKdigits("12345264", 4));
        System.out.println(new L402().removeKdigits("12305264", 4));
        System.out.println(new L402().removeKdigits("10200", 1));
        System.out.println(new L402().removeKdigits("10", 1));
    }
}
