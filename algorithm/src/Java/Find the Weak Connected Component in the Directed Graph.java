M

Identify这是个union-find问题还挺巧妙。    
看到了weak component的形式： 一个点指向所有，那么所有的点都有一个公共的parent，然后就是要找出这些点。    

为何不能从一个点出发，比如A，直接print它所有的neighbors呢？     
	不行，如果轮到了B点，那因为是directed,它也不知道A的情况，也不知道改如何继续加，或者下手。    

所以，要把所有跟A有关系的点，或者接下去和A的neighbor有关系的点，都放进union-find里面，让这些点有Common parents.     

最后output的想法：    
做一个 map <parent ID, list>。    
之前我们不是给每个num都存好了parent了嘛。    
每个num都有个parent, 然后不同的parent就创造一个不同的list。   
最后，把Map里面所有的list拿出来就好了。    

```
/*
Find the number Weak Connected Component in the directed graph. 
Each node in the graph contains a label and a list of its neighbors. 
(a connected set of a directed graph is a subgraph in which 
	any two vertices are connected by direct edge path.)

Example
Given graph:

A----->B  C
 \     |  | 
  \    |  |
   \   |  |
    \  v  v
     ->D  E <- F
Return {A,B,D}, {C,E,F}. Since there are two connected component which are {A,B,D} and {C,E,F}

Note
Sort the element in the set in increasing order

Tags Expand 
Union Find
*/

/*
Thoughts:
When constructing the dataset before running the method, I guess DirectedGraphNode is contructed in a 
way that one node shots to neighbors, but may not have neibors shooting back.
Then, there is a parent-child relationship, where we can use union-find

[ idea is correct. Need to implement with proper union-find methods. 
	(Implementation here: http://www.jiuzhang.com/solutions/find-the-weak-connected-component-in-the-directed-graph/)
	1. for loop to construct: Map<childNode, parentNode>
	2. Create Map<rootNode, List<nodes>>.
	3. Find(node) return root, and add this node to the rootNode's list
]

In NineChapter's definition:
I. UnionFind class takes HashSet, and makes maps of <child, parent> relatioinship. 
	1. However, in UnionFind constructor, first step is just init <self, self>
	2. Find method on a target element's root parent. If itself is root parent, then parent should = map.get(parent)
	3. Union method, if find(x) and find(y) are different, map them as child vs. parent.

II. In main method:
	1.	Create that HashSet for UnionFind.
	2.	Use Find methods to tacke all parent vs neighbors
	3. 	Use union to map out the relationship between parent's root and each neighbor's root.
	OKAY, so now the map<child,parent> should be done, saved within UnionFind.

III. Generate results
	For each element in HashSet, find their root, and add to that root list

Note:
Be careful about the in generateRst method: looking for the root
*/

/**
 * Definition for Directed graph.
 * class DirectedGraphNode {
 *     int label;
 *     ArrayList<DirectedGraphNode> neighbors;
 *     DirectedGraphNode(int x) { label = x; neighbors = new ArrayList<DirectedGraphNode>(); }
 * };
 */
public class Solution {
	class UnionFind {
		HashMap<Integer, Integer> map;
		//Constructor:
		UnionFind(HashSet<Integer> set) {
			map = new HashMap<Integer, Integer>();
			for (int num : set) {
				map.put(num, num);
			}
		}
		//Find:
		//Root parent should have itself as child in map<child,parent>
		int find(int x) {
			int parent = map.get(x);
			while (parent != map.get(parent)) {
				parent = map.get(parent);
			}
			return parent;
		}
		void union(int x, int y) {
			int findX = find(x);
			int findY = find(y);
			if (findX != findY) {
				map.put(findX, findY);
			}
		}
	}
	public List<List<Integer>> generateRst (List<List<Integer>> rst, UnionFind uf, HashSet<Integer> set) {
       	
    	HashMap<Integer, List<Integer>> listMap = new HashMap<Integer, List<Integer>>();
    	for (int num : set) {
    		int rootParent = uf.find(num);//uf.map.get(num);
    		if (!listMap.containsKey(rootParent)) {
    			listMap.put(rootParent, new ArrayList<Integer>());
    		} 
    		//Add num into its correct set (meaning its root ancestor)
    		listMap.get(rootParent).add(num);
    	}

    	for (List<Integer> list: listMap.values()) {
    		Collections.sort(list);
    		rst.add(list);
    	}
    	return rst;
    }

    public List<List<Integer>> connectedSet2(ArrayList<DirectedGraphNode> nodes) {
    	List<List<Integer>> rst = new ArrayList<List<Integer>>();
    	if (nodes == null || nodes.size() == 0) {
    		return rst;
    	}
    	HashSet<Integer> set = new HashSet<Integer>();
    	for (DirectedGraphNode node : nodes) {
    		set.add(node.label);
    		for (DirectedGraphNode neighbor : node.neighbors) {
    			set.add(neighbor.label);
    		}
    	}
    	UnionFind uf = new UnionFind(set);

    	//find and union: construct the map<child,parent> structure
    	for (DirectedGraphNode node : nodes) {
    		for (DirectedGraphNode neighbor : node.neighbors) {
    			uf.union(node.label, neighbor.label);
    		}
    	}
    	return generateRst(rst, uf, set);
    }

    
}











/*
	Can we do the following for find() ? Inspired by the union-find implemented with int[]
	Sort of recurisvely trying to  get the parent orign, instead of using a while loop?
	I guess it's doable.
*/
//Root parent should have itself as child in map<child,parent>
int find(int x) {
	int parent = map.get(x);
	if (parent != map.get(parent)) {
		parent = map.get(parent);
		map.put(x, parent);
	}
	return parent;
}







```