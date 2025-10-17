package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class MaximumProductSubarray {
  public int maxProduct(int[] nums) {
    int res = nums[0];
    int max[] = new int[nums.length];
    int min[] = new int[nums.length];
    max[0] = min[0] = nums[0];
    for (int i = 1; i < nums.length; i++) {
      max[i] = min[i] = nums[i];
      if (nums[i] > 0) {
        max[i] = Math.max(max[i], max[i - 1] * nums[i]);
        min[i] = Math.min(min[i], min[i - 1] * nums[i]);
      } else {
        max[i] = Math.max(max[i], min[i - 1] * nums[i]);
        min[i] = Math.min(min[i], max[i - 1] * nums[i]);
      }
      res = Math.max(max[i], res);
    }
    return res;
  }

  public int maxProduct2(int[] nums) {
    int res, max, min;
    min = max = res = nums[0];
    for (int i = 1; i < nums.length; i++) {
      if (nums[i] > 0) {
        max = Math.max(nums[i], max * nums[i]);
        min = Math.min(nums[i], min * nums[i]);
      } else {
        int t = Math.min(nums[i], max * nums[i]);
        max = Math.max(nums[i], min * nums[i]);
        min = t;
      }
      res = Math.max(max, res);
    }
    return res;
  }

  @Test
  public void maxProduct() {
    System.out.println(maxProduct2(new int[]{-4,-3,-2}));
  }
}
