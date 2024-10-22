package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class L105 {
    Map<Integer, Integer> inorderMap = new HashMap<>();

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        for (int i = 0; i < preorder.length; i++) {
            inorderMap.put(inorder[i], i);
        }
        return buildNode(0, preorder.length - 1, preorder);
    }

    public TreeNode buildNode(int start, int end, int[] preorder) {
        if (start == end) {
            return new TreeNode(preorder[start]);
        }
        int j = inorderMap.get(preorder[start]);
        int split = -1;
        for (int i = start + 1; i <= end; i++) {
            if (inorderMap.get(preorder[i]) > j) {
                split = i;
                break;
            }
        }
        System.out.println((start + 1) + " " + split + " " + end);
        TreeNode left = null;
        TreeNode right = null;
        if (split != -1) {
            right = buildNode(split, end, preorder);
        } else {
            left = buildNode(start + 1, end, preorder);
        }
        if (start + 1 <= split - 1) {
            left = buildNode(start + 1, split - 1, preorder);
        }
        // System.out.println((left==null?"":left.val)+ " " + preorder[start]+" "+right==null?"":right.val);
        return new TreeNode(preorder[start], left, right);
    }

    @Test
    public void case1() {
        buildTree(
                new int[]{3,9,1,20,15,7},
                new int[]{1,9,3,15,20,7}
        );
    }

    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
}
