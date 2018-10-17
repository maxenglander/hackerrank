package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class BalancedBrackets {
    static Map<Character, Character> OPEN_TO_CLOSE_BRACES = new HashMap<>();
    static {
        OPEN_TO_CLOSE_BRACES.put('[', ']');
        OPEN_TO_CLOSE_BRACES.put('(', ')');
        OPEN_TO_CLOSE_BRACES.put('{', '}');
    }

    static boolean areComplementaryBraces(Character openBrace, Character closingBrace) {
        return closingBrace == OPEN_TO_CLOSE_BRACES.get(openBrace);
    }

    // Complete the isBalanced function below.
    static String isBalanced(String s) {
        Stack<Character> openBraces =  new Stack<>();

        for(int i = 0; i < s.length(); i++) {
            Character brace = s.charAt(i);

            if(isOpenBrace(brace))
                openBraces.push(brace);
            else {
                if(openBraces.size() == 0 || !areComplementaryBraces(openBraces.pop(), brace))
                    return "NO";
            }
        }

        if(openBraces.size() > 0)
            return "NO";

        System.err.println("Balanced");
        return "YES";
    }

    static boolean isOpenBrace(Character c) {
        return OPEN_TO_CLOSE_BRACES.containsKey(c);
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int t = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int tItr = 0; tItr < t; tItr++) {
            String s = scanner.nextLine();

            String result = isBalanced(s);

            bufferedWriter.write(result);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        scanner.close();
    }
}
