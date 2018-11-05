package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class CTCIRecursiveStaircase {
    static class StaircaseClimbPermutationCounter {
        static class Statistics {
            int complexity;
        }

        private final int staircaseHeight;
        private final int[] stepsAtATime;

        StaircaseClimbPermutationCounter(int staircaseHeight, int[] stepsAtATime) {
            this.staircaseHeight = staircaseHeight;
            this.stepsAtATime = Arrays.copyOf(stepsAtATime, stepsAtATime.length);
            Arrays.sort(stepsAtATime);
        }

        /**
         *
         */
        int count() {
            Map<Integer, Integer> memo = new HashMap<>();
            Statistics stats = new Statistics();

            int count = count(staircaseHeight, stepsAtATime, memo, stats);

            System.err.println(
                    "Steps at a time = " + IntStream.of(stepsAtATime)
                            .mapToObj(String::valueOf)
                            .collect(joining(","))
                    + "; staircase height " + staircaseHeight
                    + "; complexity = " + stats.complexity);

            return count;
        }

        private static int count(int staircaseHeight, int[] stepsAtATime, Map<Integer, Integer> memo, Statistics stats) {
            int count = 0;

            for (int firstStep : stepsAtATime) {
                int remainingSteps = staircaseHeight - firstStep;

                if (remainingSteps == 0)
                    count += 1;
                else if (remainingSteps > 0) {
                    if (memo.containsKey(remainingSteps))
                        count += memo.get(remainingSteps);
                    else {
                        stats.complexity += 1;
                        count += count(remainingSteps, stepsAtATime, memo, stats);
                    }
                }
            }

            memo.put(staircaseHeight, count);

            return count;
        }
    }

    // Complete the stepPerms function below.
    static int stepPerms(int n) {
        StaircaseClimbPermutationCounter counter
                = new StaircaseClimbPermutationCounter(n, new int[]{ 1, 2, 3 });

        return counter.count();
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int s = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int sItr = 0; sItr < s; sItr++) {
            int n = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            int res = stepPerms(n);

            bufferedWriter.write(String.valueOf(res));
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        scanner.close();
    }
}
