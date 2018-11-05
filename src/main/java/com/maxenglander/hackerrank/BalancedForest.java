package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
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

                    return o2Size - o1Size;
                }
            }

            /**
             * Compactness here is defined as the amount that must be
             * added to all trees to ensure they all have the same value.
             * Here, zero is the maximum compactness.
             */
            private static class MostCompact implements Comparator<Forest> {
                @Override
                public int compare(Forest o1, Forest o2) {
                    int o1Compactness = calculateCompactness(o1);
                    int o2Compactness = calculateCompactness(o2);

                    return o1Compactness - o2Compactness;
                }

                static int calculateCompactness(Forest forest) {
                    List<Integer> valuesDesc
                            = forest.getTrees()
                                .stream()
                                .map(Tree.Node::getValue)
                                .sorted(Comparator.reverseOrder())
                                .collect(toList());

                    if(valuesDesc.isEmpty()) return 0;

                    int compactness = 0;
                    int largestNumber = valuesDesc.get(0);

                    for(int i = 1; i < valuesDesc.size(); i++) {
                        int value = valuesDesc.get(i);

                        if(value < largestNumber)
                            compactness += largestNumber - value;
                    }

                    return compactness;
                }
            }

            static Comparator<Forest> is(Predicate<Forest> predicate) {
                return new Is(predicate);
            }

            static Comparator<Forest> moreIsLess() {
                return new MoreTrees();
            }

            static Comparator<Forest> mostCompact() {
                return new MostCompact();
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
            this.trees = trees.stream()
                    .map(Tree.Nodes::newSnapshot)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }

        Forest addTree(Tree.Node node) {
            List<Tree.Node> trees = new ArrayList<>(getTrees());
            trees.add(node);
            return new Forest(trees);
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
        Tree.Checkpoint checkpoint;
        Comparator<Forest> comparator;
        Tree.Cutter cutter;
        Set<Tree.Node.Id> exclusions;
        String indentation;
        int maxCuts;
        int numCuts;
        Forest plan;
        Tree.Node root;

        ForestPlanner(Tree.Checkpoint checkpoint,
                      Comparator<Forest> comparator,
                      Tree.Cutter cutter,
                      Collection<Tree.Node.Id> exclusions,
                      int maxCuts,
                      int numCuts,
                      Tree.Node root) {
            this.checkpoint = checkpoint;
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

            // We're making changes to the tree, so mark a checkpoint
            checkpoint.mark();

            Tuple<Tree.Node, Tree.Node> cut = cutter.cut(root, node);

            List<Forest> plans = makePlans(cut.first(), cut.second());

            // After we finish making all of our plans,
            // we can rollback changes we made to the tree.
            checkpoint.rollback();

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

        static Forest plan(Tree.Checkpoint checkpoint,
                           Comparator<Forest> comparator,
                           Tree.Cutter cutter,
                           Collection<Tree.Node.Id> exclusions,
                           int maxCuts,
                           Tree.Node root) {
            return plan(checkpoint, comparator, cutter, exclusions, maxCuts, 0, root);
        }

        static Forest plan(Tree.Checkpoint checkpoint, Comparator<Forest> comparator,
                           Tree.Cutter cutter,
                           Collection<Tree.Node.Id> exclusions,
                           int maxCuts,
                           int numCuts,
                           Tree.Node root) {
            final ForestPlanner planner
                    = new ForestPlanner(checkpoint, comparator, cutter, exclusions, maxCuts, numCuts, root);
            return planner.plan();
        }

        Forest plan() {
            root.traverse(this);
            return this.plan;
        }

        private List<Forest> makePlans(Tree.Node root, Tree.Node node) {
            ArrayList<Forest> plans = new ArrayList<>();

            plans.add(plan);


            Forest plan0 = new Forest(root, node);

            plans.add(plan0);

            // Can we make additional cuts?
            if(numCuts + 1 < maxCuts) {
                if(root.getValue() > node.getValue()) {
                    Forest plan1 = plan(checkpoint, comparator, cutter,
                            combine(exclusions, node.getId()),
                            maxCuts, numCuts + 1, root).addTree(node);
                    plans.add(plan1);
                }

                if(node.getValue() > root.getValue()) {
                    Forest plan2 = plan(checkpoint, comparator, cutter,
                            Collections.emptyList(), maxCuts,
                            numCuts + 1, node).addTree(root);
                    plans.add(plan2);
                }
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
                    Node.Id nodeId = entry.first();

                    if(visited[nodeId.value()]) continue;
                    else visited[nodeId.value()] = true;

                    Node node = nodeById.get(nodeId);
                    List<Node.Id> childIds = entry.second();

                    for(Node.Id childId : childIds) {
                        if(childId.equals(nodeId)) continue;

                        Node childNode = nodeById.get(childId);

                        // Don't add parent as a child of itself
                        adjacencyStack.push(Tuple.from(childId,
                                childIdsByParentId.get(childId)
                                    .stream()
                                    .filter(id -> !id.equals(nodeId))
                                    .collect(toList())));

                        node.addChild(childNode);
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
                    list1.add(n0);
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

        static class Checkpoint {
            interface Listener {
                void onMark(int mark);
                void onRegister(int mark);
                void onRollback(int mark);
            }

            Set<Listener> listeners;
            int mark;

            Checkpoint() {
                listeners = new HashSet<>();
                mark = 0;
            }

            void mark() {
                mark++;

                for(Listener listener : listeners) {
                    listener.onMark(mark);
                }
            }

            void register(Listener listener) {
                this.listeners.add(listener);
                listener.onRegister(mark);
            }

            void rollback() {
                List<Listener> listeners = new ArrayList<>(this.listeners);

                for(Listener listener : listeners) {
                    listener.onRollback(mark);
                }

                mark--;
            }

            void unregister(Listener listener) {
                this.listeners.remove(listener);
            }
        }

        interface Cutter {
            Tuple<Node, Node> cut(Tree.Node parent, Tree.Node descendent);
        }

        static class Cutters {
            private Cutters() {}

            static class ParentCloningAndChildValueSubtractingCutter implements Cutter {
                @Override
                public Tuple<Node, Node> cut(Node root, Node child) {
                    List<Node> pathToRoot = getPathToRoot(child, root);

                    Node newRoot = cloneAndSubtractPath(pathToRoot, child);

                    return Tuple.from(newRoot, child);
                }

                static Node cloneAndSubtractPath(List<Node> pathToRoot, Node child) {
                    Node previousNode = child;


                    for(Node node : pathToRoot) {
                        int value = node.getValue() - child.getValue();

                        node.setValue(value);
                        if(previousNode == child) {
                            node.removeChild(child);
                        }

                        previousNode = node;
                    }

                    return previousNode;
                }

                static List<Node> getPathToRoot(Node child, Node root) {
                    if(!child.hasParent()) {
                        throw new IllegalArgumentException("Child " + child.toString() + " has no parent, and therefore no path to the root " + root.toString());
                    }

                    Node currentNode = child;
                    List<Node> pathToRoot = new ArrayList<>();

                    while(currentNode.hasParent()) {
                        Node parent = currentNode.getParent();
                        pathToRoot.add(parent);

                        if(parent.getId().equals(root.getId())) {
                            return pathToRoot;
                        }

                        currentNode = parent;
                    }

                    throw new IllegalArgumentException("Child " + child.toString() + " is not a descendant of root " + root.toString()
                            + "; child path = " + pathToRoot.stream().map(Node::toString).collect(joining(" => ")));
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
            Collection<Node> getChildren();
            Id getId();
            Node getParent();
            int getValue();
            boolean hasParent();
            void removeChild(Node previousNode);
            void setParent(Node parent);
            void setValue(int sum);
        }

        static class Nodes {
            private Nodes() {}


            private static class CheckpointNode extends StandardNode implements Checkpoint.Listener {
                int changeCount;
                Checkpoint checkpoint;
                boolean registeredWithCheckpoint;
                Map<Integer, Node> snapshots;

                CheckpointNode(Checkpoint checkpoint, Node.Id id) {
                    super(id);
                    this.checkpoint = checkpoint;
                    changeCount++;
                    registeredWithCheckpoint = false;
                    snapshots = new HashMap<>();
                }

                public void addChild(Node child) {
                    beforeChange();
                    super.addChild(child);
                }

                private void beforeChange() {
                    changeCount++;

                    if(!registeredWithCheckpoint) {
                        checkpoint.register(this);
                        registeredWithCheckpoint = true;
                    }
                }

                @Override
                public void onMark(int mark) {
                    takeSnapshot(mark);
                }

                @Override
                public void onRegister(int mark) {
                    takeSnapshot(mark);
                }

                @Override
                public void onRollback(int mark) {
                    restoreSnapshot(mark);

                    if(this.snapshots.isEmpty()) {
                        checkpoint.unregister(this);
                        registeredWithCheckpoint = false;
                    }

                    changeCount = 0;
                }


                private void restoreSnapshot(int mark) {
                    Node snapshot = snapshots.remove(mark);

                    if(snapshot == null) {
                        return;
                    }

                    setChildren(snapshot.getChildren());
                    setValue(snapshot.getValue());

                    setParent(snapshot.getParent());
                    if(snapshot.hasParent()) {
                        snapshot.getParent().addChild(this);
                    }
                }

                public void removeChild(Node child) {
                    beforeChange();
                    super.removeChild(child);
                }

                public void setParent(Node parent) {
                    beforeChange();
                    super.setParent(parent);
                }

                public void setValue(int value) {
                    beforeChange();
                    super.setValue(value);
                }

                private void takeSnapshot(int mark) {
                    Node snapshot = Nodes.newSnapshot(this);

                    snapshots.put(mark, snapshot);
                }
            }

            private static class SnapshotNode extends StandardNode {
                SnapshotNode(Node node) {
                    super(node);
                }

                @Override
                public void addChild(Node child) {
                    throw new UnsupportedOperationException("Snapshot nodes cannot be changed");
                }

                public void removeChild(Node child) {
                    throw new UnsupportedOperationException("Snapshot nodes cannot be changed");
                }

                @Override
                public void setParent(Node parent) {
                    throw new UnsupportedOperationException("Snapshot nodes cannot be changed");
                }

                @Override
                public void setValue(int value) {
                    throw new UnsupportedOperationException("Snapshot nodes cannot be changed");
                }
            }

            private static class StandardNode implements Node {
                private final Id id;
                private final Set<Node> children;
                private Node parent;
                int value;

                StandardNode(Node node) {
                    this(node.getId(), node.getValue(), node.getChildren(), node.getParent());
                }

                StandardNode(Id id) {
                    this(id, 0);
                }

                StandardNode(Id id, int value) {
                    this(id, value, Collections.emptyList());
                }

                StandardNode(Id id, int value, Collection<Node> children) {
                    this(id, value, children, null);
                }

                StandardNode(Id id, int value, Collection<Node> children, Node parent) {
                    this.id = id;
                    this.value = value;
                    this.children = new HashSet<>(children);
                    this.parent = parent;
                }

                public void addChild(Node child) {
                    child.setParent(this);
                    children.add(child);
                }

                public Collection<Node> getChildren() {
                    return Collections.unmodifiableCollection(children);
                }

                public Id getId() {
                    return id;
                }

                public Node getParent() {
                    return parent;
                }

                public Node getSelf() {
                    return this;
                }

                public int getValue() {
                    return value;
                }

                public boolean hasParent() {
                    return getParent() != null;
                }

                public void removeChild(Node child) {
                    this.children.remove(child);
                    child.setParent(null);
                }

                void setChildren(Collection<Node> children) {
                    this.children.clear();
                    this.children.addAll(children);
                }

                public void setParent(Node parent) {
                    this.parent = parent;
                }

                public void setValue(int value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "node[" + getId().value() + "]{value:" + getValue() + "}";
                }
            }

            static Node newNode(Node.Id id) {
                return new StandardNode(id);
            }

            static Node newNode(Node.Id id, int value) {
                return new StandardNode(id, value);
            }

            static Node newSnapshot(Node node) {
                return new SnapshotNode(node);
            }

            static Node withCheckpoint(Checkpoint checkpoint, Node.Id id) {
                return new CheckpointNode(checkpoint, id);
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

            static class CheckpointRegistrar implements Tree.Transformer {
                Checkpoint checkpoint;

                CheckpointRegistrar(Checkpoint checkpoint) {
                    this.checkpoint = checkpoint;
                }

                @Override
                public Node transform(Node node) {
                    Tree.Node newNode = Tree.Nodes.withCheckpoint(checkpoint, node.getId());

                    for(Tree.Node child : node.getChildren()) {
                        Tree.Node newChild = transform(child);
                        newNode.addChild(newChild);
                    }

                    newNode.setValue(node.getValue());

                    return newNode;
                }
            }

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

            static Transformer withCheckpoint(Checkpoint checkpoint) {
                return new CheckpointRegistrar(checkpoint);
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

                Collection<T> getChildren();

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

                        for (T child : next.getChildren()) {
                            stack.push(child);
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

    private static int balancedForest(int[] c, int[][] edges) {
        Tree.Printer printer = new Tree.Printer(System.err);

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
        Tree.Node sumTree = Tree.Transformers
                .summing()
                .transform(originalTree);

        // As we analyze edges, we'll be making modifications to the tree.
        // We'll take and restore snapshots as we make these modifications.
        Tree.Checkpoint checkpoint = new Tree.Checkpoint();
        Tree.Node snapshottableSumTree = Tree.Transformers
                .withCheckpoint(checkpoint)
                .transform(sumTree);

        //originalTree.traverse(printer);
        //sumTree.traverse(printer);

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
                         .thenComparing(Forest.Comparators.moreIsLess()
                         .thenComparing(Forest.Comparators.mostCompact()));

        // When we make a cut between a parent and descendant node,
        // subtract the value of the the descendant from that of the parent.
        Tree.Cutter cutAndUpdateSum = Tree.Cutters.parentCloningAndChildValueSubtracting();

        System.err.println("Beginning to plan forest");

        // Plan our forest:
        Forest forest = ForestPlanner.plan(
                // Pass in our checkpoint manager
                checkpoint,
                // Pick the largest and most even forest
                comparator,
                // When we cut a branch into two, make sure to update the sums
                cutAndUpdateSum,
                Collections.emptyList(),
                // Make up to two cuts (resulting in up to three trees)
                2,
                // Use our sum tree
                snapshottableSumTree);

        System.err.println("Final forest: " + forest.toString());

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
