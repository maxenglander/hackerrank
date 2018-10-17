package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

public class MarkAndToys {
    static class Input {
        int amountToSpend;
        int[] toyPrices;

        Input(int amountToSpend, int[] prices) {
            this.amountToSpend = amountToSpend;
            this.toyPrices = prices;
        }
    }

    // Complete the maximumToys function below.
    static int maximumToys(int[] prices, int k) {
        Arrays.sort(prices);

        int maxToys = 0, spent = 0;

        for(int i = 0; i < prices.length; i++) {
            spent += prices[i];

            if(spent > k) break;

            maxToys++;
        }

        return maxToys;
    }

    static Input argsToInput(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = Integer.parseInt(args[1]);

        int[] prices = new int[n];
        for(int i = 0; i < n; i++) {
            prices[i] = Integer.parseInt(args[2 + i]);
        }

        return new Input(k, prices);
    }

    static Input streamToInput(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        String[] nk = scanner.nextLine().split(" ");

        int n = Integer.parseInt(nk[0]);

        int k = Integer.parseInt(nk[1]);

        int[] prices = new int[n];

        String[] pricesItems = scanner.nextLine().split(" ");
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int i = 0; i < n; i++) {
            int pricesItem = Integer.parseInt(pricesItems[i]);
            prices[i] = pricesItem;
        }

        scanner.close();

        return new Input(k, prices);
    }

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        Input input;
        if(args.length > 0) {
            input = argsToInput(args);
        } else {
            input = streamToInput(System.in);
        }

        int result = maximumToys(input.toyPrices, input.amountToSpend);

        bufferedWriter.write(String.valueOf(result));
        bufferedWriter.newLine();

        bufferedWriter.close();
    }
}
