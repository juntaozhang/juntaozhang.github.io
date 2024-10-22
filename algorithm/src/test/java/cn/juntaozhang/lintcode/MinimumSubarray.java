package cn.juntaozhang.lintcode;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

/**
 * 
 */
public class MinimumSubarray {
  public int minSubArray(ArrayList<Integer> nums) {
    int sum = 0, min = Integer.MAX_VALUE;
    for (int num : nums) {
      sum += num;
      min = Math.min(sum, min);
      if (sum > 0) {
        sum = 0;
      }
    }
    return min;
  }
  @Test
  public void minSubArray() {
    System.out.println(minSubArray(Lists.newArrayList(1, -1, -2, 1)));
  }
}
