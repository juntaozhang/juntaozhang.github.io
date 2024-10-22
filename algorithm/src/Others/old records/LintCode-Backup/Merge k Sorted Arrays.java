由Merge k linked list想到，有办法找到每个node的sibling, 而int[]又不行，所以：
自己建立一个class 来存放必要信息
```
/*
Given k sorted integer arrays, merge them into one sorted array.

Have you met this question in a real interview? Yes
Example
Given 3 sorted arrays:

[
  [1, 3, 5, 7],
  [2, 4, 6],
  [0, 8, 9, 10, 11]
]
return [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].

Challenge
Do it in O(N log k).

N is the total number of integers.
k is the number of arrays.
Tags Expand 
Heap Priority Queue
*/

/*
    Thoughts: 12.10.2015
    Since we can't know the node's sibiling, as creating the prirority queue,
    let's create a Node{val, x, y}
    Then it's very similar to merge k sorted lists.
    
    border case: arrays == null. length == 0 //[]return empty list.
*/
    
public class Solution {
    public class Node {
        int val, x, y;
        public Node(int val, int x, int y) {
            this.val = val;
            this.x = x;
            this.y = y;
        }
    }
    
    public List<Integer> mergekSortedArrays(int[][] arrays) {
        List<Integer> rst = new ArrayList<Integer>();
        if (arrays == null || arrays.length == 0) {
            return rst;
        }
        
        PriorityQueue<Node> queue = new PriorityQueue<Node>(arrays.length,
            new Comparator<Node>() {
                public int compare(Node a, Node b){
                    return a.val - b.val;
                }
            }
        );
        
        //init
        for (int i = 0; i < arrays.length; i++) {
            if (arrays[i].length != 0) {
                queue.offer(new Node(arrays[i][0], i, 0));
            }
        }
        
        Node node;
        
        while (!queue.isEmpty()) {
            node = queue.poll();
            rst.add(node.val);
            if (node.y < arrays[node.x].length - 1) {
                queue.offer(new Node(arrays[node.x][node.y + 1], node.x, node.y + 1));
            }   
        }
        
        return rst;

    }
}
















```