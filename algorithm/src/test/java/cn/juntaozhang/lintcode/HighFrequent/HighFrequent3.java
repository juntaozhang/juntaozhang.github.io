package cn.juntaozhang.lintcode.HighFrequent;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class HighFrequent3 {
    public List<String> findMissingRanges(int[] nums, int lower, int upper) {
        List<String> res = new ArrayList<>();
        addRange(res, (long) lower, (long) nums[0] - 1);
        for (int i = 1; i < nums.length - 1; i++) {
            addRange(res, (long) nums[i] + 1L, (long) nums[i + 1] - 1);
        }
        addRange(res, (long) nums[nums.length - 1] + 1, (long) upper);
        return res;
    }

    public void addRange(List<String> res, long start, long end) {
        if (start < end) {
            res.add(start + "->" + end);
        } else if (start == end) {
            res.add(start + "");
        }
    }


}
