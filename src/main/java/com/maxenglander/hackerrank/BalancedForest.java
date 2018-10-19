package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class BalancedForest {
    static class Forest {
        static final Forest EMPTY = new Forest();

        static class Comparators {
            private Comparators() {}

            private static class Is implements Comparator<Forest> {
                private final Predicate<Forest> predicate;

                Is(Predicate<Forest> predicate) {
                    this.predicate = predicate;
                }

                @Override
                public int compare(Forest o1, Forest o2) {
                    boolean o1Test = predicate.test(o1);
                    boolean o2Test = predicate.test(o2);

                    if(o1Test && !o2Test) {
                        return -1;
                    }

                    if(o2Test && !o1Test) {
                        return 1;
                    }

                    return 0;
                }
            }

            private static class MoreTrees implements Comparator<Forest> {
                @Override
                public int compare(Forest o1, Forest o2) {
                    int o1Size = o1.getTrees().size();
                    int o2Size = o2.getTrees().size();

                    if(o1Size > o2Size) {
                        return -1;
                    }
                    if(o2Size > o1Size) {
                        return 1;
                    }

                    return 0;
                }
            }

            static Comparator<Forest> is(Predicate<Forest> predicate) {
                return new Is(predicate);
            }

            static Comparator<Forest> moreIsLess() {
                return new MoreTrees();
            }
        }

        static class Predicates {
            private Predicates() {}

            private static class AtLeastNEqualToLargest implements Predicate<Forest> {
                private final int minEqualToLargest;

                AtLeastNEqualToLargest(int minEqualToLargest) {
                    this.minEqualToLargest = minEqualToLargest;
                }

                @Override
                public boolean test(Forest forest) {
                    if (forest.isEmpty()) return true;

                    List<Integer> valuesDesc = forest.getTrees().stream()
                            .map(Tree.Node::getValue)
                            .sorted(Comparator.reverseOrder())
                            .collect(toList());

                    int largest = valuesDesc.get(0);
                    int equalToLargest = 0;

                    for (int i = 1; i < valuesDesc.size(); i++) {
                        int value = valuesDesc.get(i);

                        if (value == largest) {
                            equalToLargest++;
                        }
                    }

                    return equalToLargest >= minEqualToLargest;
                }
            }

            private static class AtLeastNTrees implements Predicate<Forest> {
                private final int minTrees;

                AtLeastNTrees(int minTrees) {
                    this.minTrees = minTrees;
                }

                @Override
                public boolean test(Forest forest) {
                    return forest.getTrees().size() >= minTrees;
                }
            }

            private static class AtMostNSmallerThanLargest implements Predicate<Forest> {
                private final int minSmallerThanLargest;

                AtMostNSmallerThanLargest(int minSmallerThanLargest) {
                    this.minSmallerThanLargest = minSmallerThanLargest;
                }

                @Override
                public boolean test(Forest forest) {
                    if (forest.isEmpty()) return true;

                    List<Integer> valuesDesc = forest.getTrees().stream()
                            .map(Tree.Node::getValue)
                            .sorted(Comparator.reverseOrder())
                            .collect(toList());

                    int largest = valuesDesc.get(0);
                    int smallerThanLargest = 0;

                    for (int i = 1; i < valuesDesc.size(); i++) {
                        int value = valuesDesc.get(i);

                        if (value < largest) {
                            smallerThanLargest++;
                        }
                    }

                    return smallerThanLargest <= minSmallerThanLargest;
                }
            }

            private static class AtMostNTrees implements Predicate<Forest> {
                private final int maxTrees;

                AtMostNTrees(int maxTrees) {
                    this.maxTrees = maxTrees;
                }

                @Override
                public boolean test(Forest forest) {
                    return forest.getTrees().size() <= maxTrees;
                }
            }

            static Predicate<Forest> atLeastNEqualToLargest(int n) {
                return new AtLeastNEqualToLargest(n);
            }

            static Predicate<Forest> atLeastNTrees(int n) {
                return new AtLeastNTrees(n);
            }

            static Predicate<Forest> atMostNTrees(int n) {
                return new AtMostNTrees(n);
            }

            static Predicate<Forest> atMostNSmallerThanLargest(int n) {
                return new AtMostNSmallerThanLargest(n);
            }
        }

        private final List<Tree.Node> trees;

        Forest() {
            this(Collections.emptyList());
        }

        Forest(Tree.Node... trees) {
            this(Arrays.asList(trees));
        }

        Forest(List<Tree.Node> trees) {
            this.trees = Collections.unmodifiableList(trees);
        }

        Forest addTree(Tree.Node node) {
            List<Tree.Node> newForest = new ArrayList<>(getTrees());
            newForest.add(node);
            return new Forest(newForest);
        }

        Collection<Tree.Node> getTrees() {
            return trees;
        }

        boolean isEmpty() {
            return getTrees().isEmpty();
        }

        @Override
        public String toString() {
           return "Tree(" + getTrees().stream().map(Tree.Node::toString).collect(joining(",")) + ")";
        }
    }

    static class ForestPlanner implements Tree.Traversal.Visitor<Tree.Node> {
        Comparator<Forest> comparator;
        Tree.Cutter cutter;
        Set<Tree.Node.Id> exclusions;
        String indentation;
        int maxCuts;
        int numCuts;
        Forest plan;
        Tree.Node root;

        ForestPlanner(Comparator<Forest> comparator,
                      Tree.Cutter cutter,
                      Collection<Tree.Node.Id> exclusions,
                      int maxCuts,
                      int numCuts,
                      Tree.Node root) {
            this.comparator = comparator;
            this.cutter = cutter;
            this.exclusions = new HashSet<>(exclusions);
            this.indentation
                    = IntStream.range(0, numCuts)
                        .mapToObj(i -> "   ").collect(joining());
            this.maxCuts = maxCuts;
            this.numCuts = numCuts;
            this.plan = new Forest(root);
            this.root = root;
        }

        public Tree.Traversal.Control accept(Tree.Node node) {
            if(numCuts > maxCuts) {
                plan = Forest.EMPTY;
                return Tree.Traversal.Control.HALT;
            }

            if(exclusions.contains(node.getId())) {
                return Tree.Traversal.Control.SKIP;
            }

            if(root.getId().equals(node.getId())) {
                return Tree.Traversal.Control.CONTINUE;
            }


            List<Forest> plans = makePlans(node);

            plan = plans.stream()
                    .min(comparator)
                    .get();

            return Tree.Traversal.Control.CONTINUE;
        }

        private static Collection<Tree.Node.Id> combine(Collection<Tree.Node.Id> exclusions, Tree.Node.Id id) {
            Collection<Tree.Node.Id> newC = new ArrayList<>(exclusions);
            newC.add(id);
            return newC;
        }

        static Forest plan(Comparator<Forest> comparator,
                           Tree.Cutter cutter,
                           Collection<Tree.Node.Id> exclusions,
                           int maxCuts,
                           Tree.Node root) {
            return plan(comparator, cutter, exclusions, maxCuts, 0, root);
        }

        static Forest plan(Comparator<Forest> comparator,
                           Tree.Cutter cutter,
                           Collection<Tree.Node.Id> exclusions,
                           int maxCuts,
                           int numCuts,
                           Tree.Node root) {
            final ForestPlanner planner
                    = new ForestPlanner(comparator, cutter, exclusions, maxCuts, numCuts, root);
            return planner.plan();
        }

        Forest plan() {
            root.traverse(this);
            return this.plan;
        }

        private List<Forest> makePlans(Tree.Node node) {
            ArrayList<Forest> plans = new ArrayList<>();

            plans.add(plan);

            Tree.Node rootWithCut = cutter.cut(root, node);
            Forest plan0 = new Forest(rootWithCut, node);
            plans.add(plan0);

            // Can we make additional cuts?
            if(numCuts + 1 < maxCuts) {
                Forest plan1 = plan(comparator, cutter,
                        combine(exclusions, node.getId()),
                        maxCuts, numCuts + 1, rootWithCut).addTree(node);
                plans.add(plan1);

                Forest plan2 = plan(comparator, cutter,
                        Collections.emptyList(), maxCuts,
                        numCuts + 1, node).addTree(rootWithCut);
                plans.add(plan2);
            }

            return plans;
        }

    }

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

            Tree.Node build() {
                if(nodes.length == 0) return null;

                final Map<Node.Id, List<Node.Id>> childIdsByParentId = buildParentIdToChildIdMap(edges);
                final Map<Node.Id, Node> nodeById = buildNodesById(nodes);

                final Node.Id rootNodeId = new Node.Id(0);
                final Node rootNode = nodeById.get(rootNodeId);

                boolean[] visited = new boolean[nodes.length];
                Arrays.fill(visited, false);

                final Stack<Tuple<Node.Id, List<Node.Id>>> adjacencyStack = new Stack<>();
                adjacencyStack.push(Tuple.from(rootNodeId, childIdsByParentId.get(rootNodeId)));

                while(!adjacencyStack.isEmpty()) {
                    Tuple<Node.Id, List<Node.Id>> entry = adjacencyStack.pop();
                    Node.Id parentId = entry.first();

                    if(visited[parentId.value()]) continue;

                    visited[parentId.value()] = true;

                    Node parent = nodeById.get(parentId);
                    List<Node.Id> childIds = entry.second();

                    for(int i = childIds.size() - 1; i >= 0; i--) {
                        Node.Id childId = childIds.get(i);
                        Node childNode = nodeById.get(childId);

                        adjacencyStack.push(Tuple.from(childId, childIdsByParentId.get(childId)));

                        parent.addChild(childNode);
                    }
                }

                return rootNode;
            }

            static Map<Node.Id,List<Node.Id>> buildParentIdToChildIdMap(int[][] edges) {
                Map<Node.Id, List<Node.Id>> childIdsByParentId = new HashMap<>();

                for(int[] edge : edges) {
                    Node.Id n0 = new Node.Id(edge[0] - 1);
                    Node.Id n1 = new Node.Id(edge[1] - 1);

                    List<Node.Id> list0 = childIdsByParentId.get(n0);
                    List<Node.Id> list1 = childIdsByParentId.get(n1);

                    if(list0 == null) {
                        list0 = new ArrayList<>();
                        childIdsByParentId.put(n0, list0);
                    }

                    if(list1 == null) {
                        list1 = new ArrayList<>();
                        childIdsByParentId.put(n1, list1);
                    }

                    list0.add(n1);

                    /**
                     * Don't add the other side of the adjacency;
                     * this allows us to turn an undirected graph
                     * into a tree.
                     */
                    //list1.add(n0);
                }

                return childIdsByParentId;
            }

            static Map<Node.Id,Node> buildNodesById(int[] nodes) {
                Map<Node.Id, Node> nodeById = new HashMap<>();

                for(int i = 0; i < nodes.length; i++) {
                    Node.Id nodeId = new Node.Id(i);
                    Node node = Nodes.newNode(nodeId, nodes[i]);
                    nodeById.put(nodeId, node);
                }

                return nodeById;
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

        interface Cutter {
            Node cut(Tree.Node parent, Tree.Node descendent);
        }

        static class Cutters {
            private Cutters() {}

            static class ParentCloningAndChildValueSubtractingCutter implements Cutter {
                @Override
                public Node cut(Node parent, Node descendent) {
                    Node clone = Nodes.newNode(parent);
                    clone.setValue(clone.getValue() - descendent.getValue());
                    return clone;
                }
            }

            static Cutter parentCloningAndChildValueSubtracting() {
                return new ParentCloningAndChildValueSubtractingCutter();
            }
        }

        interface Node extends Traversal.Traversable<Node> {
            class Id {
                private final Integer value;

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

            void addChild(Node child);
            List<Node> getChildren();
            Id getId();
            int getValue();
            void setValue(int sum);
        }

        static class Nodes {
            private Nodes() {}

            private static class StandardNode implements Node {
                private final Id id;
                private final List<Node> children;
                int value;

                StandardNode(Node node) {
                    this(node.getId(), node.getValue(), node.getChildren());
                }

                StandardNode(Id id) {
                    this(id, 0);
                }

                StandardNode(Id id, int value) {
                    this(id, value, new ArrayList<>());
                }

                StandardNode(Id id, int value, List<Node> children) {
                    this.id = id;
                    this.value = value;
                    this.children = children;
                }

                public void addChild(Node child) {
                    children.add(child);
                }

                public List<Node> getChildren() {
                    return children;
                }

                public Id getId() {
                    return id;
                }

                public Node getSelf() {
                    return this;
                }

                public int getValue() {
                    return value;
                }

                public void setValue(int value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "node[" + getId().value() + "]{value:" + getValue() + "}";
                }
            }

            static Node newNode(Node node) {
                return new StandardNode(node);
            }

            static Node newNode(Node.Id id) {
                return new StandardNode(id);
            }

            static Node newNode(Node.Id id, int value) {
                return new StandardNode(id, value);
            }
        }

        static class Printer implements Traversal.Visitor<Node> {
            private final PrintStream printStream;

            Printer(PrintStream printStream) {
                this.printStream = printStream;
            }

            @Override
            public Traversal.Control accept(Node node) {
                printStream.println(
                        "Node:" + node.toString()
                   + "; children" + node.getChildren()
                                .stream()
                                .map(Node::toString)
                                .collect(joining(",")));

                return Traversal.Control.CONTINUE;
            }
        }

        interface Transformer {
            Node transform(Node node);
        }

        static class Transformers {
            private Transformers() {}

            static class Summator implements Tree.Transformer {
                @Override
                public Tree.Node transform(Tree.Node node) {
                    int sum = node.getValue();

                    Tree.Node newNode = Tree.Nodes.newNode(node.getId());

                    for(Tree.Node child : node.getChildren()) {
                        Tree.Node newChild = transform(child);
                        sum += newChild.getValue();
                        newNode.addChild(newChild);
                    }

                    newNode.setValue(sum);

                    return newNode;
                }
            }

            static Transformer summing() {
                return new Summator();
            }
        }

        static class Traversal {
            enum Control {
                CONTINUE,
                HALT,
                SKIP
            }

            interface Traversable<T extends Traversable<T>> {

                List<T> getChildren();

                T getSelf();

                default void traverse(Visitor<T> visitor) {
                    Stack<T> stack = new Stack<>();
                    stack.push(this.getSelf());

                    while (!stack.isEmpty()) {
                        T next = stack.pop();

                        Control control = visitor.accept(next);

                        switch (control) {
                            case HALT:
                                return;
                            case SKIP:
                                continue;
                        }

                        for (int i = next.getChildren().size() - 1; i >= 0; i--) {
                            stack.push(next.getChildren().get(i));
                        }
                    }
                }
            }


            interface Visitor<T> {
                Control accept(T node);
            }
        }

        private Tree() {}
    }

    static int balancedForest(int[] c, int[][] edges) {
        // Create tree
        Tree.Node originalTree = Tree.Builder.newBuilder()
                .nodes(c)
                .edges(edges)
                .build();

        if(originalTree == null) return -1;

        // Get the root of the tree and transform it so that
        // The value of each node is a sum of it's pre-transformation
        // value plus the sum of the post-transformation values of
        // each of its children.
        Tree.Node sumTree = Tree.Transformers.summing().transform(originalTree);

        // Create a balance evaluator
        Predicate<Forest> balanced
                =      Forest.Predicates.atLeastNTrees(2)
                  .and(Forest.Predicates.atMostNTrees(3))
                  .and(Forest.Predicates.atLeastNEqualToLargest(1))
                  .and(Forest.Predicates.atMostNSmallerThanLargest(1));


        // Use a forest comparator that gives us the kind of forest we want.
        // First build our comparator components.
        Comparator<Forest> comparator
                = Forest.Comparators.is(balanced)
                         .thenComparing(Forest.Comparators.moreIsLess());

        // When we make a cut between a parent and descendant node,
        // subtract the value of the the descendant from that of the parent.
        Tree.Cutter cutAndUpdateSum = Tree.Cutters.parentCloningAndChildValueSubtracting();

        // Plan our forest:
        Forest forest = ForestPlanner.plan(
                // Pick the largest and most even forest
                comparator,
                // When we cut a branch into two, make sure to update the sums
                cutAndUpdateSum,
                Collections.emptyList(),
                // Make up to two cuts (resulting in up to three trees)
                2,
                // Use our sum tree
                sumTree);

        if(!balanced.test(forest)) {
            return -1;
        }

        List<Integer> valuesDesc = forest.getTrees()
                .stream()
                .map(Tree.Node::getValue)
                .sorted(Comparator.reverseOrder())
                .collect(toList());

        int largestValue  = valuesDesc.get(0);

        if(valuesDesc.size() == 2) {
            return largestValue;
        } else if(valuesDesc.size() == 3) {
            int smallestValue  = valuesDesc.get(2);
            return largestValue - smallestValue;
        } else {
            return -1;
        }
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
