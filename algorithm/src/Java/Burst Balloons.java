其实会做之后挺好想的一个DP。
dp[i][j] =  balloons i~j 之间的sum. 然后找哪个点开始burst? 设为x。
For loop 所有的点作为x， 去burst。
每次burst都切成了三份：左边可以recusive 求左边剩下的部分的最大值 + 中间3项相乘 + 右边递归下去求最大值。


这个是momorization, 而不纯是DP
因为recursive了，其实还是搜索，但是memorize了求过的值，节省了Processing
```
/*
Given n balloons, indexed from 0 to n-1. Each balloon is painted with a number on it represented by array nums. 
You are asked to burst all the balloons. If the you burst balloon i you will get nums[left] * nums[i] * nums[right] coins. 
Here left and right are adjacent indices of i. After the burst, the left and right then becomes adjacent.

Find the maximum coins you can collect by bursting the balloons wisely.

Note: 
(1) You may imagine nums[-1] = nums[n] = 1. They are not real therefore you can not burst them.
(2) 0 ≤ n ≤ 500, 0 ≤ nums[i] ≤ 100

Example:

Given [3, 1, 5, 8]

Return 167

    nums = [3,1,5,8] --> [3,5,8] -->   [3,8]   -->  [8]  --> []
   coins =  3*1*5      +  3*5*8    +  1*3*8      + 1*8*1   = 167
Credits:
Special thanks to @peisi for adding this problem and creating all test cases.

Hide Company Tags Google
Show Tags
Divide and Conquer Dynamic Programming


*/

/*
	Thoughts: as seen in dicussion. Build DP.
	State:
	dp[i][j]: the number of max coins can collect between i and j.
		For a position x in [i,j], where to burst it? So this goes into a divide and conquer method.
		Burst at x, track the sum, and record the max into dp[i][j]
	Function:
		dp[i][j] = Math.max(dp[i][j], DP(i, x-1) + nums[x-1]*nums[x]*nums[x+1] + DP(x+1, j))
	Init:
		create dp[n+2][n+2].  (from 0 to n+1)
		dp[0][1] = 1;
		dp[n][n+1] = 1;
	Return:	
		dp[1][n]

	DP(int i, int j, int[][] dp)

	Need to redo that nums.
*/


public class Solution {
	int[][] dp;
	int[] values;
    public int maxCoins(int[] nums) {
        if (nums == null || nums.length == 0) {
        	return 0;
        }
        int n = nums.length;
        dp = new int[n + 2][n + 2];

        //Initialize new array
        values = new int[n + 2];
        values[0] = values[n + 1] = 1;
        for (int i = 1; i < n + 1; i++) {
        	values[i] = nums[i - 1];
        }
       
        return DP(1, n);
    }

    public int DP(int i, int j){
    	if (dp[i][j] > 0) {//momorization
    		return dp[i][j];
    	}
    	for (int x = i; x <= j; x++) {
    		dp[i][j] = Math.max(dp[i][j], DP(i, x - 1) + values[i-1]*values[x]*values[j+1] + DP(x + 1, j));
    	}
    	return dp[i][j];
    }
}

/*
	用了recursive + memorization, 但是也可以用传统的DP，比如：
	for (int length = 1; length < n; length++) [
        for (int = 0; i < n-1; i++)  {
            j = i + length; 
            if length == 1:
                dp[i][j] = A[i] * A[j] + A[i]
            else:
                dp[i][j] = max {}
        }
    }

*/



/*
	My Thought: TOO COMPLEX. Should go with the easy DP approach. Also, using a hashMap to trach all the patterns,
	this might not be applicable: because as the integer array's length goes up, there will be too many possible
	combinations to store in hashamp.
	Burst each balloon, and DFS into each branch, calcualte the sum + each balloon-burst's product.
	Also, use a HahsMap<"Value combination", max value>. to reduce the # of re-calculation.
	convert nums into string, and in DFS, we don't even need bakc-tracking
	helper(list, sum)


	Thoughts:http://www.cnblogs.com/grandyang/p/5006441.html
	dp[i,j]: burst range [i~j]'s max coins.

*/
```
