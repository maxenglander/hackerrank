package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RoadsAndLibraries {

    /**
     * The cities need to be collected into "groups".
     * Two cities are in the same "group" if they can reach
     * each other either directly or through another city in
     * the same group.
     *
     */
    static long roadsAndLibraries(int n, int c_lib, int c_road, int[][] cities) {
        /**
         * If the cost of a library is less than the cost of a road,
         * just build a library in each city;
         */
        if(c_lib < c_road) {
            long totalCost = (long) c_lib * n;
            return totalCost;
        }

        Map<Integer, List<Integer>> adjacenciesByCity = buildAdjacenciesByCity(cities);
        boolean[] visits = buildVisits(n);
        long totalCost = 0;

        for(int city = 0; city < n; city++) {
            if(!visits[city]) {
                totalCost += c_lib;
                totalCost += c_road * countAdjacencies(city, adjacenciesByCity, visits);
            }
        }

        return totalCost;
    }

    private static Map<Integer, List<Integer>> buildAdjacenciesByCity(int[][] roads) {
        Map<Integer, List<Integer>> adjacenciesByCity = new HashMap<>();

        for(int i = 0; i < roads.length; i++) {
            int[] road = roads[i];

            int city0 = road[0] - 1;
            int city1 = road[1] - 1;

            List<Integer> adjacencies0 = adjacenciesByCity.getOrDefault(city0, new ArrayList<>());
            adjacencies0.add(city1);
            adjacenciesByCity.put(city0, adjacencies0);

            List<Integer> adjacencies1 = adjacenciesByCity.getOrDefault(city1, new ArrayList<>());
            adjacencies1.add(city0);
            adjacenciesByCity.put(city1, adjacencies1);
        }

        return adjacenciesByCity;
    }

    private static boolean[] buildVisits(int numberOfCities) {
        boolean[] visits = new boolean[numberOfCities];
        Arrays.fill(visits, false);
        return visits;
    }

    private static int countAdjacencies(int city, Map<Integer, List<Integer>> adjacenciesByCity, boolean[] visits) {
        visits[city] = true;

        int count = 0;

        List<Integer> adjacencies = adjacenciesByCity.get(city);
        if (adjacencies != null) {
            for (int adjacency : adjacencies) {
                if (!visits[adjacency])
                    count += 1 + countAdjacencies(adjacency, adjacenciesByCity, visits);
            }
        }

        return count;
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int q = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int qItr = 0; qItr < q; qItr++) {
            String[] nmC_libC_road = scanner.nextLine().split(" ");

            int n = Integer.parseInt(nmC_libC_road[0]);

            int m = Integer.parseInt(nmC_libC_road[1]);

            int c_lib = Integer.parseInt(nmC_libC_road[2]);

            int c_road = Integer.parseInt(nmC_libC_road[3]);

            int[][] cities = new int[m][2];

            for (int i = 0; i < m; i++) {
                String[] citiesRowItems = scanner.nextLine().split(" ");
                scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

                for (int j = 0; j < 2; j++) {
                    int citiesItem = Integer.parseInt(citiesRowItems[j]);
                    cities[i][j] = citiesItem;
                }
            }

            long result = roadsAndLibraries(n, c_lib, c_road, cities);

            bufferedWriter.write(String.valueOf(result));
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        scanner.close();
    }
}
