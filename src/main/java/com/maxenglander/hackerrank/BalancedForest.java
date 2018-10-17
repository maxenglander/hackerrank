package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class BalancedForest {
    static class Tuple<First, Second> {
        First first;
        Second second;

        Tuple(First first, Second second) {
            this.first = first;
            this.second = second;
        }

        static <First, Second> Tuple<First, Second> from(First first, Second second) {
            return new Tuple<>(first, second);
        }

        First first() {
            return first;
        }

        Second second() {
            return second;
        }
    }

    static class Tree {
        static class Builder {
            int[][] edges;
            int[] nodes;

            static Builder newBuilder() {
                return new Builder();
            }

            Tree build() {
                if(nodes.length == 0) return Tree.empty();

                final Map<Node.Id, List<Node.Id>> adjacenciesByNodeId = buildAdjacenciesByNodeId(edges);
                final Map<Node.Id, Node> nodesById = buildNodesById(nodes);

                final Node.Id rootNodeId = new Node.Id(0);
                final Node rootNode = nodesById.get(rootNodeId);
                final Tree tree = new Tree(rootNode);

                boolean[] visited = new boolean[nodes.length];
                Arrays.fill(visited, false);

                final Stack<Tuple<Node.Id, List<Node.Id>>> adjacencyStack = new Stack<>();
                adjacencyStack.push(Tuple.from(rootNodeId, adjacenciesByNodeId.get(rootNodeId)));

                while(!adjacencyStack.isEmpty()) {
                    Tuple<Node.Id, List<Node.Id>> entry = adjacencyStack.pop();
                    Node.Id parentId = entry.first();

                    if(!visited[parentId.value()]) {
                        visited[parentId.value()] = true;

                        Node parent = nodesById.get(parentId);
                        List<Node.Id> childIds = entry.second();

                        for(int i = childIds.size() - 1; i >= 0; i--) {
                            Node.Id childId = childIds.get(i);
                            Node childNode = nodesById.get(childId);

                            adjacencyStack.push(Tuple.from(childId, adjacenciesByNodeId.get(childId)));

                            parent.addChild(childNode);
                        }
                    }
                }

                return tree;
            }

            private static Map<Node.Id,List<Node.Id>> buildAdjacenciesByNodeId(int[][] edges) {
                Map<Node.Id, List<Node.Id>> adjacenciesByNodeId = new HashMap<>();

                for(int i = 0; i < edges.length; i++) {
                    int[] edge = edges[i];

                    Node.Id n0 = new Node.Id(edge[0] - 1);
                    Node.Id n1 = new Node.Id(edge[1] - 1);

                    List<Node.Id> list0 = adjacenciesByNodeId.get(n0);
                    List<Node.Id> list1 = adjacenciesByNodeId.get(n1);

                    if(list0 == null) {
                        list0 = new ArrayList<>();
                        adjacenciesByNodeId.put(n0, list0);
                    }

                    if(list1 == null) {
                        list1 = new ArrayList<>();
                        adjacenciesByNodeId.put(n1, list1);
                    }

                    list0.add(n1);

                    /**
                     * Don't add the other side of the adjacency;
                     * this allows us to turn an undirected graph
                     * into a tree.
                     */
                    //list1.add(n0);
                }

                return adjacenciesByNodeId;
            }

            private static Map<Node.Id,Node> buildNodesById(int[] nodes) {
                Map<Node.Id, Node> nodesById = new HashMap<>();

                for(int i = 0; i < nodes.length; i++) {
                    Node.Id nodeId = new Node.Id(i);
                    Node node = new Node(nodeId, nodes[i]);
                    nodesById.put(nodeId, node);
                }

                return nodesById;
            }

            Builder edges(int[][] edges) {
                this.edges = edges;
                return this;
            }

            Builder nodes(int[] nodes) {
                this.nodes = nodes;
                return this;
            }
        }

        static class Node {
            static class Id {
                Integer value;

                Id(Integer value) {
                    this.value = value;
                }

                @Override
                public boolean equals(Object o) {
                    if(o == null) return false;
                    if(!this.getClass().isInstance(o)) return false;
                    Node.Id oId = this.getClass().cast(o);
                    return this.value.equals(oId.value());
                }

                @Override
                public int hashCode() {
                    return this.value.hashCode();
                }

                Integer value() {
                    return value;
                }
            }

            Id id;
            List<Node> children;
            int value;

            Node(Id id, int value) {
                this.id = id;
                this.value = value;
                children = new ArrayList<>();
            }

            void addChild(Node child) {
                children.add(child);
            }

            void addChildren(List<Node> children) {
                this.children.addAll(children);
            }

            List<Node> children() {
                return children;
            }

            Id id() {
                return id;
            }

            int value() {
                return value;
            }
        }

        Node rootNode;

        Tree(Node rootNode) {
            this.rootNode = rootNode;
        }

        static Tree empty() {
            return new Tree(null);
        }

        void print(PrintStream ps) {
            if(this.rootNode == null) {
                ps.println("Tree is empty");
            }

            String indent = "";
            Stack<Node> stack = new Stack<>();
            stack.push(rootNode);

            while(!stack.isEmpty()) {
                Node node = stack.pop();
                List<Node> children = node.children();

                ps.println(indent + "Node: " + node.id().value());
                ps.println(indent + "    Value: " + node.value());
                ps.println(indent + "    Number of children: " + children.size());
                ps.println(indent + "    Child IDs: "
                        + children.stream()
                            .map(Node::id)
                            .map(Node.Id::value)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", ")));

                for(int i = children.size() - 1; i >= 0; i--) {
                    Node child = children.get(i);
                    stack.push(child);
                }
            }
        }
    }

    /**
     * To solve this problem, we will first calculate the sum of
     * all children at each node:
     *
     *       n0: s(n0) = v0 + s(n1) + s(n2) + s(n3)
     *     / | \
     *   n1 n2 n3: s(n3) = v3
     *
     * After that, we need to find three cuts where two of the cuts
     * have the same sum, and the sum of the third cut is less than
     * or equal to the third.
     *
     * Given an array of node values c[], and an array of edges int[][],
     * we can calculate an array of sums s[], where each member s in s[]
     * is the sum of a corresponding member c in c[].
     *
     * Simultaneously, we can build a map m{} so that * m[c] => i[]
     * where each member of i is the node number of a child
     * of m[c].
     *
     */
    static int balancedForest(int[] c, int[][] edges) {
        System.err.println("Balancing forest");
        Tree tree
            = Tree.Builder.newBuilder()
                           .nodes(c)
                           .edges(edges)
                           .build();

        tree.print(System.err);

        System.err.println("");

        return 0;
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int q = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int qItr = 0; qItr < q; qItr++) {
            int n = scanner.nextInt();
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            int[] c = new int[n];

            String[] cItems = scanner.nextLine().split(" ");
            scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

            for (int i = 0; i < n; i++) {
                int cItem = Integer.parseInt(cItems[i]);
                c[i] = cItem;
            }

            int[][] edges = new int[n - 1][2];

            for (int i = 0; i < n - 1; i++) {
                String[] edgesRowItems = scanner.nextLine().split(" ");
                scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

                for (int j = 0; j < 2; j++) {
                    int edgesItem = Integer.parseInt(edgesRowItems[j]);
                    edges[i][j] = edgesItem;
                }
            }

            int result = balancedForest(c, edges);

            bufferedWriter.write(String.valueOf(result));
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        scanner.close();
    }
}
