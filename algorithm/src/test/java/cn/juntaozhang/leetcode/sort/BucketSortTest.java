package cn.juntaozhang.leetcode.sort;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.Arrays;

public class BucketSortTest {

    public int maximumGap(int[] nums) {
        if (nums.length < 2) return 0;
        int max = Arrays.stream(nums).max().getAsInt();
        int min = Arrays.stream(nums).min().getAsInt();
        int d = Math.max(1, (max - min) / (nums.length - 1));
        int size = (max - min) / d + 1;
        int[][] buckets = new int[size][2];
        for (int[] bucket : buckets) {
            Arrays.fill(bucket, -1);
        }
        for (int num : nums) {
            int t = (num - min) / d;
            if (buckets[t][0] == -1) {
                buckets[t][0] = buckets[t][1] = num;
            } else {
                buckets[t][0] = Math.min(buckets[t][0], num);
                buckets[t][1] = Math.max(buckets[t][1], num);
            }
        }
        int preMin = -1;
        int ans = 0;
        for (int[] bucket : buckets) {
            if (bucket[0] == -1) {
                continue;
            }
            if (preMin != -1) {
                ans = Math.max(ans, bucket[0] - preMin);
            }
            preMin = bucket[1];
        }
        return ans;
    }

    @Test
    public void case1() {
        System.out.println(maximumGap(new int[]{3}));
        System.out.println(maximumGap(new int[]{3, 8}));
        System.out.println(maximumGap(new int[]{3, 7, 9, 8}));
    }


    public void sort(int[] arr) {
        if (arr == null || arr.length < 2) return;
        StringUtils.print(arr);
        boolean swapped;
        for (int end = arr.length - 1; end > 0; end--) {
            swapped = false;
            for (int i = 0; i < end; i++) {
                if (arr[i] > arr[i + 1]) {
                    swap(arr, i, i + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
            StringUtils.print2(arr);
        }
    }

    public void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    @Test
    public void case2() {
        sort(new int[]{3, 7, 9, 8, 1, 2, 1});
    }

}
