H

记得要理解： 为什么 i-1天的卖了又买，可以和第 i 天的卖合成一次交易？
   因为每天交易的price是定的。所以卖了又买，等于没卖！这就是可以合并的原因。要对价格敏感啊少年。

Inspired from here:
http://liangjiabin.com/blog/2015/04/leetcode-best-time-to-buy-and-sell-stock.html

局部最优解 vs. 全局最优解：   
   local[i][j] = max(global[i – 1][j – 1] + diff, local[i – 1][j] + diff)
   global[i][j] = max(global[i – 1][j], local[i][j])

local[i][j]和global[i][j]的区别是：local[i][j]意味着在第i天一定有交易（卖出）发生。
   当第i天的价格高于第i-1天（即diff > 0）时，那么可以把这次交易（第i-1天买入第i天卖出）跟第i-1天的交易（卖出）合并为一次交易，即local[i][j]=local[i-1][j]+diff；
   当第i天的价格不高于第i-1天（即diff<=0）时，那么local[i][j]=global[i-1][j-1]+diff，而由于diff<=0，所以可写成local[i][j]=global[i-1][j-1]。
   (Note:在我下面这个solution里面没有省去 +diff）

global[i][j]就是我们所求的前i天最多进行k次交易的最大收益，可分为两种情况：
   如果第i天没有交易（卖出），那么global[i][j]=global[i-1][j]；
   如果第i天有交易（卖出），那么global[i][j]=local[i][j]。



```
/*
Say you have an array for which the ith element is the price of a given stock on day i.

Design an algorithm to find the maximum profit. You may complete at most k transactions.

Example
Given prices = [4,4,6,1,1,4,2,5], and k = 2, return 6.

Note
You may not engage in multiple transactions at the same time 
(i.e., you must sell the stock before you buy again).

Challenge
O(nk) time.

Tags Expand 
Dynamic Programming
*/

/*
	Thoughts: http://liangjiabin.com/blog/2015/04/leetcode-best-time-to-buy-and-sell-stock.html
	local[i][j] = max(global[i – 1][j – 1] , local[i – 1][j] + diff). WHY????
	global[i][j] = max(global[i – 1][j], local[i][j])
	
*/
class Solution {
    public int maxProfit(int k, int[] prices) {
    	if (prices == null || prices.length < 2 || k <= 0) {
    		return 0;
    	}
    	if (k >= prices.length) {
    		int profit = 0;
    		for (int i = 1; i < prices.length; i++) {
    			if (prices[i] > prices[i - 1]) {
    				profit += prices[i] - prices[i - 1];
    			}
    		}
    		return profit;
    	}
    	int[][] local = new int[prices.length][k + 1];
    	int[][] global = new int[prices.length][k + 1];
    	for (int i = 1; i < prices.length; i++) {
    		int diff = prices[i] - prices[i - 1];
    		for (int j = 1; j <= k; j++) {
    			local[i][j] = Math.max(global[i-1][j-1] + diff, local[i - 1][j] + diff);
    			global[i][j] = Math.max(global[i-1][j], local[i][j]);
    		}
    	}
    	return global[prices.length - 1][k];
    }
};

```