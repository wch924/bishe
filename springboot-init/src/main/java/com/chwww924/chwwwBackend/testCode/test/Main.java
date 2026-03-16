package com.chwww924.chwwwBackend.testCode.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String M = in.nextLine();
        String N = in.nextLine();
        int[] arr = new int[128];
        int count = 0;
        for(char ch: N.toCharArray()) {
            if(arr[ch] == 0) {
                count++;
                arr[ch]++;
            }
        }
        int[] window = new int[128];
        int left = 0, right = 0, valid = 0;
        int minlen = Integer.MAX_VALUE;
        int start = 0;
        while(right < M.length()) {
            char c = M.charAt(right++);
            if (arr[c] > 0) {
                window[c]++;
                if (window[c] == arr[c]) {
                    valid++;
                }
            }
            while(valid == count) {
                if(right - left < minlen) {
                    minlen = right - left;
                    start = left;
                }
                char d = M.charAt(left++);
                if(arr[d] > 0) {
                    if(window[d] == arr[d]) {
                        valid--;
                    }
                }
            }
        }
        if(minlen == Integer.MAX_VALUE) {
            System.out.println("");
        } else {
            System.out.println(M.substring(start, start + minlen));
        }
    }
    public boolean checkOrder (int[] order) {
        // write code here
        Map<Integer,Integer> map = new HashMap<>();
        for(int num: order) {
            if(map.get(num) > 2) {
                return false;
            }
            map.put(num, map.getOrDefault(num, 0)+1);
        }
        return true;
    }
}
