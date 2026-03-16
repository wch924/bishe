package com.chwww924.chwwwBackend.testCode.LRU;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//以数组 intervals 表示若干个区间的集合，其中单个区间为 intervals[i] = [starti, endi] 。请你合并所有重叠的区间，并返回 一个不重叠的区间数组，该数组需恰好覆盖输入中的所有区间 。
public class code__1 {
    public static void main(String []args) {
        int [][]nums = {{4,7},{1,4}};
        System.out.println(merge(nums));
    }
    private static int merge(int[][] nums) {
        Arrays.sort(nums, (a, b) ->a[0] - b[0]);
        int [] cur = nums[0];
        List<int []> merge = new ArrayList<>();
        for(int i = 1;i < nums.length;i++) {
            int[] num = nums[i];
            if(num[0] <= cur[1]) {
                cur[1] = Math.max(cur[1], num[i]);
            }else {
                cur = num;
                merge.add(cur);
            }
        }
        return merge.size();
    }
}
