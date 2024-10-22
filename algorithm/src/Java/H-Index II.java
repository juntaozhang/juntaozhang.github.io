H-index的一个优化。
binary search
```
/*
Follow up for H-Index: What if the citations array is sorted in ascending order? 
Could you optimize your algorithm?
Hint:

Expected runtime complexity is in O(log n) and the input is sorted.

Hide Company Tags Facebook
Hide Tags Binary Search
Hide Similar Problems (M) H-Index

*/

/*
	citations[0,1,3,5,6]
	look for a h, where x = N-h, arr[x] >= h
	h is from right ot left.
	We want to find smallest x that has arr[x] >= n-x
	binary search:
		start,mid,end
		if match, keep going left until not able to
	O(nLogN)
*/

public class Solution {
    public int hIndex(int[] citations) {
        if (citations == null || citations.length == 0) {
        	return 0;
        }
        int n = citations.length;
        int start = 0;
        int end = n - 1;
        int mid;
        while (start + 1 < end) {
        	mid = start + (end - start)/2;
        	if (citations[mid] == n - mid) {
        		if (mid - 1 >= 0 && citations[mid - 1] == n - (mid-1)) {
        			end = mid;
        		} else {
        			return n - mid;// that is n - x
        		}
        	} else if (citations[mid] < n - mid) {
        		start = mid;
        	} else {
        		end = mid;
        	}
        }
        if (citations[start] >= n - start) {
        	return n - start;
        } else if (citations[end] >= n - end) {
        	return n - end;
        }
        return 0;
    }
}
```