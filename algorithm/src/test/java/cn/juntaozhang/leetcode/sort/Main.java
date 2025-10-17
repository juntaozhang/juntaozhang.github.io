package cn.juntaozhang.leetcode.sort;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String str = input.next();
        input.close();
        Node current = null, root = null;
        for (String s : str.split(",")) {
            Node tmp = new Node(s);
            if (current != null) {
                current.next = tmp;
            } else {
                root = tmp;
            }
            current = tmp;
        }
        Node newRoot = reverse(root);
        current = newRoot;
        String outStr = "";
        while (current != null) {
            outStr += (current.value + ",");
            current = current.next;
        }
        System.out.println(outStr.substring(0, outStr.length() - 1));
    }

    public static Node reverse(Node root) {
        Node current = root;
        Node newRoot = null, tmp;
        while (current != null) {
            tmp = current.next;
            current.next = newRoot;
            newRoot = current;
            current = tmp;
        }

        return newRoot;
    }

    public static class Node {
        String value;
        Node next;

        public Node(String value) {
            this.value = value;
        }
    }
}