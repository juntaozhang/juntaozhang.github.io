package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 
 */
public class MaximumSubarrayDifference {
    @Test
    public void maxDiffSubArrays() {
        System.out.println(maxDiffSubArrays(new int[]{1, 2, -3, -1}));
    }
    @Test
    public void maxTwoSubArrays() {
        ArrayList<Integer> list = new ArrayList<>();
        Collections.addAll(list,-1,-1);
        System.out.println(maxTwoSubArrays(list));
    }

    // max(SUM(A) + SUM(B))
    public int maxTwoSubArrays(ArrayList<Integer> nums) {
        //1,2,-3,1
        //1,|2,-3,1  ->  1,2,-3,|1
        int res = Integer.MIN_VALUE, len = nums.size();
        int[] lmax = new int[len];
        int[] rmax = new int[len];
        int[] local = new int[len];
        lmax[0] = local[0] = nums.get(0);
        for (int i = 1; i < len; i++) {
            local[i] = Math.max(local[i - 1] + nums.get(i), nums.get(i));
            lmax[i] = Math.max(lmax[i - 1], local[i]);
        }
        rmax[len - 1] = local[len - 1] = nums.get(len - 1);
        for (int i = (len - 2); i >= 0; i--) {
            local[i] = Math.max(local[i + 1] + nums.get(i), nums.get(i));
            rmax[i] = Math.max(rmax[i + 1], local[i]);
            res = Math.max(res, rmax[i + 1] + lmax[i]);
        }
        return res;
    }

    // |SUM(A) - SUM(B)|
    public int maxDiffSubArrays(int[] nums) {
        int res = 0, len = nums.length;
        int[] lmax = new int[len];
        int[] lmin = new int[len];
        int[] rmax = new int[len];
        int[] rmin = new int[len];
        int[] localmax = new int[len];
        int[] localmin = new int[len];
        lmin[0] = lmax[0] = localmax[0] = localmin[0] = nums[0];
        for (int i = 1; i < len; i++) {
            localmax[i] = Math.max(localmax[i - 1] + nums[i], nums[i]);
            localmin[i] = Math.min(localmin[i - 1] + nums[i], nums[i]);
            lmin[i] = Math.min(lmin[i - 1], localmin[i]);
            lmax[i] = Math.max(lmax[i - 1], localmax[i]);
        }
        rmax[len - 1] = rmin[len - 1] = localmax[len - 1] = localmin[len - 1] = nums[nums.length - 1];
        for (int i = (len - 2); i >= 0; i--) {
            localmax[i] = Math.max(localmax[i + 1] + nums[i], nums[i]);
            localmin[i] = Math.min(localmin[i + 1] + nums[i], nums[i]);
            rmax[i] = Math.max(rmax[i + 1], localmax[i]);
            rmin[i] = Math.min(rmin[i + 1], localmin[i]);
            res = Math.max(res, Math.max(
                    Math.abs(rmax[i + 1] - lmin[i]),
                    Math.abs(rmin[i + 1] - lmax[i])));
        }
        return res;
    }

    // |Product(A) - Product(B)|
    public int maxDiffProductSubArrays(int[] nums) {
        int res = 0, len = nums.length;
        int[] lmax = new int[len];
        int[] lmin = new int[len];
        int[] llmax = new int[len];
        int[] llmin = new int[len];
        lmin[0] = lmax[0] = llmax[0] = llmin[0] = nums[0];
        for (int i = 1; i < len; i++) {
            if (nums[i] > 0) {
                llmax[i] = Math.max(llmax[i - 1] * nums[i], nums[i]);
                llmin[i] = Math.min(llmin[i - 1] * nums[i], nums[i]);
            } else {
                llmax[i] = Math.max(llmin[i - 1] * nums[i], nums[i]);
                llmin[i] = Math.min(llmax[i - 1] * nums[i], nums[i]);
            }
            lmin[i] = Math.min(lmin[i - 1], llmin[i]);
            lmax[i] = Math.max(lmax[i - 1], llmax[i]);
        }
        int[] rmax = new int[len];
        int[] rmin = new int[len];
        int[] rlmax = new int[len];
        int[] rlmin = new int[len];
        rmax[len - 1] = rmin[len - 1] = rlmax[len - 1] = rlmin[len - 1] = nums[nums.length - 1];
        for (int i = (len - 2); i >= 0; i--) {
            if (nums[i] > 0) {
                rlmax[i] = Math.max(rlmax[i + 1] * nums[i], nums[i]);
                rlmin[i] = Math.min(rlmin[i + 1] * nums[i], nums[i]);
            } else {
                rlmax[i] = Math.max(rlmin[i + 1] * nums[i], nums[i]);
                rlmin[i] = Math.min(rlmax[i + 1] * nums[i], nums[i]);
            }
            rmax[i] = Math.max(rmax[i + 1], rlmax[i]);
            rmin[i] = Math.min(rmin[i + 1], rlmin[i]);
            res = Math.max(res, Math.max(
                    Math.abs(rmax[i + 1] - lmin[i]),
                    Math.abs(lmax[i] - rmin[i + 1])));
        }
        return res;
    }

}
