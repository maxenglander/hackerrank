package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MagicSquareForming {
    static class Square {
        int[][] values;

        Square(int[][] values) {
            this.values = values;
        }

        int computeDistance(Square other) {
            int distance = 0;

            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    distance += Math.abs(getCell(i, j) - other.getCell(i, j));
                }
            }

            return distance;
        }

        int getCell(int x, int y) {
            return values[x][y];
        }

        static Square from(int... input) {
            int[][] values = new int[3][3];

            for(int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    values[i][j] = input[(3*i)+j];
                }
            }

            return new Square(values);
        }
    }
    static Square argsToSquare(String[] args) {
        int[][] s = new int[3][3];


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int sItem = Integer.parseInt(args[(3 * i) + j]);
                s[i][j] = sItem;
            }
        }

        return new Square(s);
    }

    static Square scannerToSquare(Scanner scanner) {

        int[][] s = new int[3][3];

        for (int i = 0; i < 3; i++) {
            String[] sRowItems = scanner.nextLine().split(" ");
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            for (int j = 0; j < 3; j++) {
                int sItem = Integer.parseInt(sRowItems[j]);
                s[i][j] = sItem;
            }
        }

        return new Square(s);
    }

    static List<Square> MagicSquares = Arrays.asList(
        Square.from(
            6,1,8,
            7,5,3,
            2,9,4
        ),
        Square.from(
            8,1,6,
            3,5,7,
            4,9,2
        ),
        Square.from(
            2,9,4,
            7,5,3,
            6,1,8
        ),
        Square.from(
            4,9,2,
            3,5,7,
            8,1,6
        ),
        Square.from(
            6,7,2,
            1,5,9,
            8,3,4
        ),
        Square.from(
            2,7,6,
            9,5,1,
            4,3,8
        ),
        Square.from(
            4,3,8,
            9,5,1,
            2,7,6
        ),
        Square.from(
            8,3,4,
            1,5,9,
            6,7,2
        )
    );


    static int formingMagicSquare(Square square) {
        int minDistance = Integer.MAX_VALUE;

        for(Square other : MagicSquares) {
            int distance = square.computeDistance(other);

            if(distance < minDistance)
                minDistance = distance;
        }

        return minDistance;
    }

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        final Square square;

        if(args.length > 0) {
            square = argsToSquare(args);
        } else {
            Scanner scanner = new Scanner(System.in);
            square = scannerToSquare(scanner);
        }

        int result = formingMagicSquare(square);

        bufferedWriter.write(String.valueOf(result));
        bufferedWriter.newLine();

        bufferedWriter.close();
    }
}
