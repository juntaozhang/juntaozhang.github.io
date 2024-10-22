这题目标为简单。但是做了很久。
最后分析出来：
每个数位的数字，都是基于这个数字之前越过的战壕...好吧，意思是，跳过了多少种可能。
对，就用4，2，1举例。网上大家都用这个例子。不行，我要换一个。

换【6，5，2】吧。我们找6，5，2是permudation里面的第几个。
正常排序，也就是permutation的第一个，应该是【2，5，6】
如果要从首位，2，变成6，要跨过多少条尸体呢？
很简单，高中就学过，重点来了：小于6的数字有多少个呢？（2，5）.然后每个数字变成head，都有各自的变化可能，而每个数字打头，都有(n-1)!种可能。明显了吧，把每个（n-1）!加起来。　注意（ｎ－１）意思就是出去开头的数字（２、５），后面有多少个，有序排列一下有多少情况，不就是（ｎ－１）！嘛。
	这一步，公式推出来就是很简单的 (有几个小于6的数字呀) ×（出去ｈｅａｄ剩下有多少个数字）！

以上	种种，都是为了把６推上皇位，而牺牲的条数。

那么把６推上去以后，还有接下去的呢。

接下去要看５，２.
６确定，后面ｐｅｒｍｕｄａｔｉｏｎ可变的情况有可能是【６，５，２】，那还可能是【６，２，５】呢。

方法一样了。
看ｇｉｖｅｎ　数组的第二位５，算它接下去：
１.　有几个数字小于５呢？
２.　除去５，还有几个数字可以　ｆａｃｔｏｒｉａｌ呢？
３. 一样的。第一步就结果乘以第二步。

接下去要看最后一个元素2了。
一样的想法。


6,5,2全看过了以后咋办。
加起来呗。
加起来，就是【6，5，2】上位，所踏过的所有小命啊！

我这解释太生动了。因为耗费了好长时间思考...
```
/*
Given a permutation which contains no repeated number, find its index in all the permutations of these numbers, which are ordered in lexicographical order. The index begins at 1.

Example
Given [1,2,4], return 1.
*/

/*
	Thoughts:
	Given some thoughts online: 
	Take one example 4,2,1 (totally reversed, worse case)
	Assume array length = n
	1. If sorted, it should be 1,2,4. In permutation, when does 4 appear? It appears after 1xx,2xx. That is, it appears after all of the smaller ones show up.
	2. For each number smaller than 4 in later indexes, each of them all have (n-1)! mutations. n -1 is actaully: current 0-based index of 4.
	Therefore, for head number 4, there are:
		#ofSmallerNumber * (curr 0-base index)!
	3. For loop on each num, calculate
		SUM {#ofSmallerNumber * (index i)!}
	4. To store #ofSmallerNum, put hashmap<num, index>. For example, for number 4, with index 2: There are 2 numbers smaller than 4, which are 1 and 2.

	O(n^2)
*/

public class Solution {

    public long permutationIndex(int[] A) {
    	if (A == null || A.length == 0) {
    		return 0;
    	}
    	HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    	int n = A.length;
    	long rst = 0;
    	//O(n^2)
    	//Count: in A, after A[i], how many smaller nums are left behind.
    	for (int i = 0; i < n; i++) {
    	    int count = 0;
    		for (int j = i + 1; j < n; j++) {
    			if (A[j] < A[i]) {
    				count++;
    			}
    		}
    		map.put(A[i], count);
    	}
    	//O(n^2)
    	for (int i = 0; i < n; i++) {
    		long fact = 1;
    		for (int j = (n - i - 1); j >= 1; j--) {
    			fact *= j;
    		}
    		rst += map.get(A[i]) * fact;
    	}
    	return rst + 1;
    }
}
```