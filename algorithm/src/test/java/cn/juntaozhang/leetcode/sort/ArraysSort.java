package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class ArraysSort {

    public static void pairInsertSort(int[] a, int left, int right) {
        /*
         * Skip the longest ascending sequence.
         */
        do {
            if (left >= right) {
                return;
            }
        } while (a[++left] >= a[left - 1]);

        /*
         * Every element from adjoining part plays the role
         * of sentinel, therefore this allows us to avoid the
         * left range check on each iteration. Moreover, we use
         * the more optimized algorithm, so-called pair insertion
         * sort, which is faster (in the context of Quicksort)
         * than traditional implementation of insertion sort.
         */
        for (int k = left; ++left <= right; k = ++left) {
            int a1 = a[k], a2 = a[left];

            if (a1 < a2) {
                a2 = a1;
                a1 = a[left];
            }
            while (a1 < a[--k]) {
                a[k + 2] = a[k];
            }
            a[++k + 1] = a1;

            while (a2 < a[--k]) {
                a[k + 1] = a[k];
            }
            a[k + 1] = a2;
        }
        int last = a[right];

        while (last < a[--right]) {
            a[right + 1] = a[right];
        }
        a[right + 1] = last;
    }

    public static void traditionalInsertSort(int[] a, int left, int right) {
        for (int i = left, j = i; i < right; j = ++i) {
            int ai = a[i + 1];
            while (a[j] > ai) {
                a[j + 1] = a[j];
                if (j-- == left) {
                    break;
                }
            }
            a[j + 1] = ai;
        }
    }

    private static final int MAX_RUN_COUNT = 67;
    private static final int MAX_RUN_LENGTH = 33;

    public static void timSort(int[] a, int left, int right,
                               int[] work, int workBase, int workLen) {
        /*
         * Index run[i] is the start of i-th run
         * (ascending or descending sequence).
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0;
        run[0] = left;


        // Check if the array is nearly sorted
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // ascending
                while (++k <= right && a[k - 1] <= a[k]) ;
            } else if (a[k] > a[k + 1]) { // descending
                while (++k <= right && a[k - 1] >= a[k]) ;
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    int t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                }
            } else { // equal
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        traditionalInsertSort(a, left, right);
                        return;
                    }
                }
            }

            /*
             * The array is not highly structured,
             * use Quicksort instead of merge sort.
             */
            if (++count == MAX_RUN_COUNT) {
                traditionalInsertSort(a, left, right);
                return;
            }
        }

        // Check special cases
        // Implementation note: variable "right" is increased by 1.
        if (run[count] == right++) { // The last run contains one element
            run[++count] = right;
        } else if (count == 1) { // The array is already sorted
            return;
        }

        /*
        根据待归并的子数组总数计算出归并次数，
        如果归并次数是奇数次，那么第一轮归并让 arr 作为辅助数组；
        如果归并次数是偶数次，那么第一轮归并就让 result 作为辅助数组。
        这样就能保证最后一轮归并时，arr 一定是辅助数组，也就是保存归并结果的数组。
        这就是 odd 参数的作用：它记录了归并次数是奇数次还是偶数次。
         */
        // Determine alternation base for merge
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1) ;

        // Use or create temporary array b for merging
        int[] b;                 // temp array; alternates with a
        int ao, bo;              // array offsets from 'left'
        int blen = right - left; // space needed for b
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new int[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // Merging
        // 合并 run 小块
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            /*
                (count & 1) != 0 是用来判断 count 是否是奇数的，它等效于 count % 2 == 1
                如果 count 是奇数，则两两合并之后，一定会出现一个落单的 run 小块。
                此时，将其原封不动的拷贝到 b 数组的尾部，等待下次合并。
             */
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                     b[i + bo] = a[i + ao]
                )
                    ;
                run[++last] = right;
            }
            // 最后，交换 a、b 数组内存地址的引用，继续交替合并：
            int[] t = a;
            a = b;
            b = t;
            int o = ao;
            ao = bo;
            bo = o;
        }
    }

    public static void main(String[] args) {
//        int[] a = new int[]{2, 1};
//        int[] a = new int[]{5, 1, 1, 2, 0, 0};
        int[] a = new int[]{-1, 1, 2, 5, 3, 0, 2, 1};
        timSort(a, 0, a.length - 1, null, 0, 0);
//        pairInsertSort(a, 1, a.length - 1);
//        traditionalInsertSort(a, 0, a.length - 1);
        System.out.println(Arrays.toString(a));
    }
}
