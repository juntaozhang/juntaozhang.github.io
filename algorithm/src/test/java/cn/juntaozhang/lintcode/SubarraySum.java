package cn.juntaozhang.lintcode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 */
public class SubarraySum {

    //巧妙利用 sum[i] - sum[j]
    public ArrayList<Integer> subarraySum(int[] nums) {
        int len = nums.length;

        ArrayList<Integer> ans = new ArrayList<Integer>();
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        map.put(0, -1);

        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += nums[i];

            if (map.containsKey(sum)) {
                ans.add(map.get(sum) + 1);
                ans.add(i);
                return ans;
            }

            map.put(sum, i);
        }

        return ans;
    }
}
