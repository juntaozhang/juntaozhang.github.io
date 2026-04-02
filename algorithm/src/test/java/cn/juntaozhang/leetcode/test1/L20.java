package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class L20 {
    public boolean isValid(String s) {
        LinkedList<Character> stack = new LinkedList<>();
        stack.push(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            char r = s.charAt(i);
            if (stack.isEmpty()) {
                stack.push(r);
                continue;
            }
            char l = stack.peek();
            if ((l == '(' && r == ')') || (l == '[' && r == ']') || (l == '{' && r == '}')) {
                stack.pop();
            } else {
                stack.push(r);
            }
        }
        return stack.isEmpty();
    }

    @Test
    public void case1() {
        Assert.assertFalse(isValid("()[]{}"));
    }

    @Test
    public void case2() {
        Assert.assertFalse(isValid("()[{]}"));
    }
}
