package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class L225 {
    static class MyStack {
        int[] data;
        int i;

        public MyStack() {
            data = new int[100];
            i = -1;
        }

        public void push(int x) {
            data[++i] = x;
        }

        public int pop() {
            if(empty()) {
                return -1;
            }
            return data[i--];
        }

        public int top() {
            return data[i];
        }

        public boolean empty() {
            return i == -1;
        }
    }

    @Test
    public void case1() {
        MyStack myStack = new MyStack();
        myStack.push(1);
        myStack.push(2);
        System.out.println(myStack.top()); // 返回 2
        System.out.println(myStack.pop()); // 返回 2
        System.out.println(myStack.empty()); // 返回 False
        System.out.println(myStack.pop()); // 返回 1
        System.out.println(myStack.empty()); // 返回 True
    }

}
