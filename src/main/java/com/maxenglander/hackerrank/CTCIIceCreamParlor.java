package com.maxenglander.hackerrank;

import java.util.*;

public class CTCIIceCreamParlor {

    static void printFlavors(int flavorIdOne, int flavorIdTwo) {
        int firstFlavor = flavorIdOne < flavorIdTwo ? flavorIdOne : flavorIdTwo;
        int secondFlavor = flavorIdOne < flavorIdTwo ? flavorIdTwo: flavorIdOne;

        System.out.println(firstFlavor + " " + secondFlavor);
    }

    // Complete the whatFlavors function below.
    static void whatFlavors(int[] costs, int money) {
        Map<Integer, Integer> costToIds = new HashMap();

        for(int i = 0; i < costs.length; i++) {
            int cost = costs[i];
            int id = i + 1;


            int remainder = money - cost;

            if(costToIds.containsKey(remainder)) {
                printFlavors(id, costToIds.get(remainder));
                return;
            } else {
                costToIds.put(cost, id);
            }
        }
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int t = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int tItr = 0; tItr < t; tItr++) {
            int money = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            int n = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            int[] cost = new int[n];

            String[] costItems = scanner.nextLine().split(" ");
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            for (int i = 0; i < n; i++) {
                int costItem = Integer.parseInt(costItems[i]);
                cost[i] = costItem;
            }

            whatFlavors(cost, money);
        }

        scanner.close();
    }
}
