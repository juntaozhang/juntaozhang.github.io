package cn.juntaozhang.leetcode;

import java.util.LinkedList;

public class Solution {
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    private static TreeNode getNode(String val) {
        if (val.equals("null")) {
            return null;
        }
        return new TreeNode(Integer.valueOf(val));
    }

    public static TreeNode deserialize(String data) {
        String s = data.substring(1, data.length() - 1);
        if (s.length() > 0) {
            String[] arr = s.split(",");
            LinkedList<TreeNode> q = new LinkedList<>();
            TreeNode root = getNode(arr[0]);
            TreeNode parent = root;
            boolean isLeft = true;
            for (int i = 1; i < arr.length; i++) {
                TreeNode n = getNode(arr[i]);
                if (isLeft) {
                    parent.left = n;
                } else {
                    parent.right = n;
                }
                isLeft = !isLeft;
                if (n != null) {
                    q.offerLast(n);
                }
                if (isLeft) {
                    parent = q.pollFirst();
                }

            }
            return root;
        }
        return null;
    }


    int max = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        if (root == null) {
            return 0;
        }
        maxPathSum0(root);
        return max;
    }
    /*
            -10,
            9,   20,
                15  7
    */

    private int maxPathSum0(TreeNode n) {
        if (n == null) {
            return 0;
        }
        int l = Math.max(maxPathSum0(n.left), 0);
        int r = Math.max(maxPathSum0(n.right), 0);
        int sum = l + r + n.val;
        max = Math.max(max, sum);
        return Math.max(l, r) + n.val;
    }

    public static void main(String[] args) {
        new Solution().maxPathSum(deserialize("[-10,9,20,null,null,15,7]"));
    }

}