package com.maxenglander.hackerrank;

import java.io.InputStream;
import java.util.Scanner;

public class CTCIBubbleSort {
    static int[] argsToIntArray(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] a = new int[n];

        for(int i = 0; i < args.length - 1; i++) {
            a[i] = Integer.parseInt(args[i+1]);
        }

        return a;
    }

    static int[] streamToIntArray(InputStream stream) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        int[] a = new int[n];

        String[] aItems = scanner.nextLine().split(" ");
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int i = 0; i < n; i++) {
            int aItem = Integer.parseInt(aItems[i]);
            a[i] = aItem;
        }

        return a;
    }

    // Complete the countSwaps function below.
    static void countSwaps(int[] a) {
        int passSwaps = 0,
            totalSwaps = 0;

        do {
            passSwaps = 0;

            for(int i = 0; i < a.length - 1; i++) {
                int left =  a[i],
                    right = a[i + 1];

                if(left > right) {
                    a[i] = right;
                    a[i + 1] = left;

                    passSwaps++;
                }
            }

            totalSwaps += passSwaps;
        } while(passSwaps > 0);

        System.out.println("Array is sorted in " + totalSwaps + " swaps.");
        System.out.println("First Element: " + a[0]);
        System.out.println("Last Element: " + a[a.length - 1]);
    }

    public static void main(String[] args) {
        final int[] intArray;

        if(args.length > 0) {
            intArray = argsToIntArray(args);
        } else {
            intArray = streamToIntArray(System.in);
        }

        countSwaps(intArray);
    }
}
