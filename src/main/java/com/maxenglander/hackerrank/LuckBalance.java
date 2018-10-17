package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Scanner;

public class LuckBalance {
    // Complete the luckBalance function below.
    static int luckBalance(int losableImportantContests, int[][] contests) {
        PriorityQueue<Integer> sortedLuckValuesOfImportantContests = new PriorityQueue<>(Integer::compareTo);
        Integer totalLuck = 0;

        for(int i = 0; i < contests.length; i++) {
            int[] contest = contests[i];
            int luck = contest[0];
            boolean important = contest[1] == 1;

            totalLuck += luck;

            if(important) {
                System.err.println("Adding luck value to priority queue: " + luck);
                sortedLuckValuesOfImportantContests.add(luck);
            }
        }

        System.err.println("Total luck: " + totalLuck);

        int importantContestsToWin = sortedLuckValuesOfImportantContests.size() - losableImportantContests;

        for(int i = 0; i < importantContestsToWin; i++) {
            Integer luck = sortedLuckValuesOfImportantContests.poll();
            System.err.println("Deducting value from total luck: " + luck);
            totalLuck -= (2 * luck);
        }

        while(sortedLuckValuesOfImportantContests.size() > 0) {
            System.err.println("Next item in priority queue: " + sortedLuckValuesOfImportantContests.poll());
        }

        return totalLuck;
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        String[] nk = scanner.nextLine().split(" ");

        int n = Integer.parseInt(nk[0]);

        int k = Integer.parseInt(nk[1]);

        int[][] contests = new int[n][2];

        for (int i = 0; i < n; i++) {
            String[] contestsRowItems = scanner.nextLine().split(" ");
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            for (int j = 0; j < 2; j++) {
                int contestsItem = Integer.parseInt(contestsRowItems[j]);
                contests[i][j] = contestsItem;
            }
        }

        int result = luckBalance(k, contests);

        bufferedWriter.write(String.valueOf(result));
        bufferedWriter.newLine();

        bufferedWriter.close();

        scanner.close();
    }
}
