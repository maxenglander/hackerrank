package com.maxenglander.hackerrank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class CrosswordPuzzle {
    static class Answer {
        private final Block block;
        private final String word;

        private Answer(Block block, String word) {
            this.block = block;
            this.word = word;
        }

        Block getBlock() {
            return block;
        }

        Optional<Character> getCharacterAt(Coordinate coordinate) {
            if(!block.intersectsCoordinate(coordinate))
                return Optional.empty();

            Vector origin = block.getOrigin();
            Direction direction = origin.getDirection();

            int offset = 0;

            if(direction.equals(Direction.ACROSS)) {
                offset = coordinate.getX() - origin.getCoordinate().getX();
            } else {
                offset = coordinate.getY() - origin.getCoordinate().getY();
            }

            return Optional.of(word.charAt(offset));
        }

        String getWord() {
            return word;
        }

        static Answer from(Block block, String word) {
            return new Answer(block, word);
        }
    }

    static class Block {
        static class Builder {
            private Vector origin;
            private int length;

            Builder(Vector origin) {
                this.origin = origin;
                length = 1;
            }

            Block build() {
                return Block.from(origin, length);
            }

            Direction getDirection() {
                return origin.getDirection();
            }

            int getLength() {
                return length;
            }

            Vector getOrigin() {
                return origin;
            }

            Builder append() {
                length++;
                return this;
            }

            Builder prepend() {
                if(getDirection().equals(Direction.ACROSS))
                    origin = Vector.from(
                            Coordinate.at(
                                    getOrigin().getCoordinate().getX() - 1,
                                    getOrigin().getCoordinate().getY()),
                            getOrigin().getDirection());
                else
                    origin = Vector.from(
                            Coordinate.at(
                                    getOrigin().getCoordinate().getX(),
                                    getOrigin().getCoordinate().getY() - 1),
                            getOrigin().getDirection());

                length++;

                return this;
            }

            static Builder newBuilder(Coordinate coordinate, Direction direction) {
                return new Builder(Vector.from(coordinate, direction));
            }
        }

        private static final Map<Vector, Map<Integer, Block>> CACHE = new HashMap<>();

        private final Vector origin;
        private final int length;

        private Block(Vector origin, int length) {
            this.origin = origin;
            this.length = length;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!getClass().isInstance(o)) return false;
            Block ow = getClass().cast(o);
            return getOrigin().equals(ow.getOrigin()) && getLength() == ow.getLength();
        }

        int getLength() {
            return length;
        }

        Vector getOrigin() {
            return origin;
        }

        @Override
        public int hashCode() {
            return 7 + getOrigin().hashCode() + getLength();
        }

        boolean intersectsLine(Direction otherDirection, int position) {
            Vector origin = getOrigin();
            Coordinate coordinate = origin.getCoordinate();
            Direction direction = origin.getDirection();

            if(direction.equals(otherDirection)) return false;

            int start = direction == Direction.ACROSS
                    ? coordinate.getX()
                    : coordinate.getY();
            int end = start + getLength() - 1;

            return start <= position && end >= position;
        }

        boolean intersectsBlock(Block otherBlock) {
            Vector otherOrigin = otherBlock.getOrigin();
            Coordinate otherCoordinate = otherOrigin.getCoordinate();
            Direction otherDirection = otherOrigin.getDirection();

            int position = otherDirection.equals(Direction.DOWN)
                    ? otherCoordinate.getX()
                    : otherCoordinate.getY();

            return intersectsLine(otherDirection, position);
        }

        Optional<Coordinate> intersectsBlockAt(Block otherBlock) {
            Vector otherOrigin = otherBlock.getOrigin();
            Coordinate otherCoordinate = otherOrigin.getCoordinate();
            Direction otherDirection = otherOrigin.getDirection();

            Vector origin = getOrigin();
            Coordinate coordinate = origin.getCoordinate();

            if(!intersectsBlock(otherBlock))
                return Optional.empty();

            Coordinate intersection;
            if(otherDirection.equals(Direction.ACROSS)) {
                intersection = Coordinate.at(coordinate.getX(), otherCoordinate.getY());
            } else {
                intersection = Coordinate.at(otherCoordinate.getX(), coordinate.getY());
            }

            return Optional.of(intersection);
        }

        boolean intersectsCoordinate(Coordinate coordinate) {
            Vector origin = getOrigin();
            Direction direction = origin.getDirection();

            if(direction.equals(Direction.ACROSS)) {
                return intersectsLine(Direction.DOWN, coordinate.getX());
            } else {
                return intersectsLine(Direction.ACROSS, coordinate.getY());
            }
        }

        @Override
        public String toString() {
            return "Block{origin=" + getOrigin().toString() + ";length=" + getLength() + "}";
        }
        static Block from(Vector origin, int length) {
            return CACHE.computeIfAbsent(origin, o -> new HashMap<>())
                        .computeIfAbsent(length, l -> new Block(origin, length));
        }
    }

    static class Board {
        static class Builder {
            private final Set<Coordinate> cells;
            private final Map<Vector, Block.Builder> blockBuildersByVector;
            private final int height;
            private final int width;

            Builder(int height, int width) {
                cells = new HashSet<>();
                blockBuildersByVector = new HashMap<>();
                this.height = height;
                this.width = width;
            }

            Builder addCell(Coordinate c) {
                if(cells.contains(c)) return this;

                cells.add(c);

                for(Coordinate d : new Coordinate[] {
                        Coordinate.at(c.getX() - 1, c.getY()),
                        Coordinate.at(c.getX() + 1, c.getY()),
                        Coordinate.at(c.getX(), c.getY() - 1),
                        Coordinate.at(c.getX(), c.getY() + 1)
                }) {
                    if(!cells.contains(d)) continue;

                    Direction direction = c.getY() == d.getY()
                            ? Direction.ACROSS
                            : Direction.DOWN;

                    Vector vectorC = Vector.from(c, direction);
                    Vector vectorD = Vector.from(d, direction);

                    Block.Builder blockBuilder;
                    if(!blockBuildersByVector.containsKey(vectorD)) {
                        blockBuilder = Block.Builder.newBuilder(d, direction);
                        blockBuildersByVector.put(vectorD, blockBuilder);
                    } else {
                        blockBuilder = blockBuildersByVector.get(vectorD);
                    }

                    if(c.isBefore(d, direction))
                        blockBuilder.prepend();
                    else
                        blockBuilder.append();

                    blockBuildersByVector.put(vectorC, blockBuilder);
                }

                return this;
            }

            Board build() {
                Map<Vector, Block> blockByOrigin = new HashMap<>();
                Map<Coordinate, Set<Block>> blocksByCell = new HashMap<>();
                Map<Integer, Set<Block>> blocksByLength = new HashMap<>();
                Map<Block, Map<Coordinate, Block>> intersectionsByBlock = new HashMap<>();

                for(Coordinate cell : cells) {
                    Set<Block> blocks = new HashSet<>();

                    for(Vector vector : new Vector[] {
                        Vector.from(cell, Direction.ACROSS),
                        Vector.from(cell, Direction.DOWN)
                    }) {
                        if(!blockBuildersByVector.containsKey(vector)) continue;

                        Block.Builder blockBuilder = blockBuildersByVector.get(vector);
                        Vector origin = blockBuilder.getOrigin();

                        Block block = blockByOrigin
                                .computeIfAbsent(origin, ov -> blockBuilder.build());
                        blocksByLength
                                .computeIfAbsent(block.getLength(), l -> new HashSet<>())
                                .add(block);
                        blocks.add(block);
                    }

                    for(Block block : blocks) {
                        blocksByCell.computeIfAbsent(cell, c -> new HashSet<>())
                                    .add(block);

                        Set<Block> intersectingBlocks = blocks.stream()
                                .filter(b -> !b.equals(block))
                                .collect(toSet());

                        for(Block intersectingBlock : intersectingBlocks) {
                            Coordinate intersection = block.intersectsBlockAt(intersectingBlock).get();

                            intersectionsByBlock
                                    .computeIfAbsent(block, w -> new HashMap<>())
                                    .put(intersection, intersectingBlock);
                        }
                    }
                }

                return new Board(
                        blocksByCell,
                        blocksByLength,
                        intersectionsByBlock);
            }

            static Builder newBuilder(int height, int width) {
                return new Builder(height, width);
            }
        }

        private final Map<Coordinate, Set<Block>> blocksByCell;
        private final Map<Integer, Set<Block>> blocksByLength;
        private final Map<Block, Map<Coordinate, Block>> intersectionsByBlock;

        Board(Map<Coordinate, Set<Block>> blocksByCell,
              Map<Integer, Set<Block>> blocksByLength,
              Map<Block, Map<Coordinate, Block>> intersectionsByBlock) {
            this.blocksByCell = blocksByCell;
            this.blocksByLength = blocksByLength;
            this.intersectionsByBlock = intersectionsByBlock;
        }

        public Collection<Block> findBlocksAtCell(Coordinate cell) {
            return blocksByCell.getOrDefault(cell, Collections.emptySet());
        }

        Collection<Block> findBlocksOfLength(int length) {
            return Collections.unmodifiableCollection(
                    blocksByLength.getOrDefault(length, Collections.emptySet()));
        }

        Map<Coordinate, Block> findIntersectingBlocks(Block block) {
            return intersectionsByBlock.getOrDefault(block, Collections.emptyMap());
        }
    }

    static class Coordinate {
        private static final Map<Integer, Map<Integer, Coordinate>> CACHE = new HashMap<>();

        private final int x;
        private final int y;

        private Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        static Coordinate at(int x, int y) {
            return CACHE.computeIfAbsent(x, _x -> new HashMap<>())
                        .computeIfAbsent(y, _y -> new Coordinate(x, y));
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!getClass().isInstance(o)) return false;
            Coordinate oc = getClass().cast(o);
            return getX() == oc.getX() && getY() == oc.getY();
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        @Override
        public int hashCode() {
            return (getX() + ":" + getY()).hashCode();
        }

        boolean isBefore(Coordinate c, Direction direction) {
            if(direction.equals(Direction.ACROSS))
                return getX() < c.getX();
            return getY() < c.getY();
        }

        @Override
        public String toString() {
            return "Coordinate{x=" + getX() + ";y=" + getY() + "}";
        }
    }

    enum Direction {
        ACROSS,
        DOWN;
    }

    static class Permutator<T> implements Iterator<List<T>> {
        private T head;
        private int index;
        private List<T> original;
        private Permutator<T> subPermutator;

        Permutator(T[] input) {
            this(asList(input));
        }

        Permutator(List<T> input) {
            index = 0;
            original = input;
            updateHeadAndSubPermutator();
        }

        @Override
        public boolean hasNext() {
            if(head == null) {
                return index == 0;
            }

            return subPermutator.hasNext();
        }

        @Override
        public List<T> next() {
            if(head == null && index == 0) {
                index++;
                return emptyList();
            }

            List<T> permutation = new ArrayList<>();
            permutation.add(head);
            permutation.addAll(subPermutator.next());

            if(!subPermutator.hasNext()) {
                index++;
                updateHeadAndSubPermutator();
            }

            return permutation;
        }

        private void updateHeadAndSubPermutator() {
            if (index < original.size()) {
                head = original.get(index);

                List<T> tail = new ArrayList<>(original);
                tail.remove(index);
                subPermutator = new Permutator<>(tail);
            }  else {
                head = null;
                subPermutator = null;
            }
        }

    }

    static class Puzzle {
        static class Solution {
            private final Board board;
            private final Map<Block, Answer> answerByBlock;

            Solution(Board board, Map<Block, Answer> answerByBlock) {
                this.board = board;
                this.answerByBlock = answerByBlock;
            }

            public char charAt(Coordinate cell) {
                Collection<Block> blocks = board.findBlocksAtCell(cell);

                if(blocks.isEmpty())
                    throw new IllegalArgumentException("There are no blocks at cell");

                for(Block block : blocks) {
                    if(!answerByBlock.containsKey(block))
                        throw new IllegalArgumentException("There are no answers for block");

                    Answer answer = answerByBlock.get(block);
                    Optional<Character> maybeChar = answer.getCharacterAt(cell);

                    if(!maybeChar.isPresent())
                        throw new IllegalArgumentException("Cell is not within answer");

                    return maybeChar.get();
                }

                throw new IllegalArgumentException("Failed to find character at cell");
            }
        }

        static class Solver {
            /**
             * Finds the first empty block on the board into which
             * the word fits without conflicts with answers in
             * intersecting blocks.
             */
            private static Optional<Answer> findAnswer(Map<Block, Answer> answersByBlock, Board board, String word) {
                Collection<Block> blocks = board.findBlocksOfLength(word.length());

                for(Block block : blocks) {
                    if(answersByBlock.containsKey(block)) continue;

                    Answer answer = Answer.from(block, word);

                    if(proposeAnswer(answersByBlock, board, answer)) {
                        return Optional.of(answer);
                    }
                }

                return Optional.empty();
            }

            /**
             * Returns true if the proposed answer has zero conflicts
             * with other committed answers, otherwise returns false.
             */
            private static boolean proposeAnswer(Map<Block, Answer> answersByBlock, Board board, Answer proposedAnswer) {
                Map<Coordinate, Block> intersections = board.findIntersectingBlocks(proposedAnswer.getBlock());

                for(Map.Entry<Coordinate, Block> intersection : intersections.entrySet()) {
                    Coordinate intersectingCoordinate = intersection.getKey();
                    Block intersectingBlock = intersection.getValue();

                    if(!answersByBlock.containsKey(intersectingBlock)) continue;

                    Answer intersectingAnswer = answersByBlock.get(intersectingBlock);

                    if(!proposedAnswer.getCharacterAt(intersectingCoordinate)
                            .equals(intersectingAnswer.getCharacterAt(intersectingCoordinate))) {
                        return false;
                    }
                }

                return true;
            }

            static Optional<Puzzle.Solution> solve(Board board, String[] words) {
                Permutator<String> wordsPermutator = new Permutator<>(words);

                while(wordsPermutator.hasNext()) {
                    List<String> permutation = wordsPermutator.next();
                    System.err.println("Attempting to solve with permutation: "
                            + permutation.stream().collect(joining(",")));
                    Optional<Puzzle.Solution> maybeSolution = solveOnce(board, permutation);
                    if(maybeSolution.isPresent()) return maybeSolution;
                }

                System.err.println("Failed to find any solution");

                return Optional.empty();
            }

            private static Optional<Puzzle.Solution> solveOnce(
                                      Board board,
                                      Collection<String> words) {
                Map<Block, Answer> answerByBlock = new HashMap<>();

                for(String word : words) {
                    Optional<Answer> maybeAnswer = findAnswer(answerByBlock, board, word);
                    if(!maybeAnswer.isPresent()) {
                        return Optional.empty();
                    }
                    Answer answer = maybeAnswer.get();
                    answerByBlock.put(answer.getBlock(), answer);
                }

                return Optional.of(new Puzzle.Solution(board, answerByBlock));
            }
        }
    }

    static class Vector {
        private static final Map<Coordinate, Map<Direction, Vector>> CACHE = new HashMap<>();

        private final Coordinate coordinate;
        private final Direction direction;

        private Vector(Coordinate coordinate, Direction direction) {
            this.coordinate = coordinate;
            this.direction = direction;
        }

        Coordinate getCoordinate() {
            return coordinate;
        }

        Direction getDirection() {
            return direction;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) return false;
            if(!getClass().isInstance(o)) return false;
            Vector ov = getClass().cast(o);
            return getCoordinate().equals(ov.getCoordinate())
                    && getDirection().equals(ov.getDirection());
        }

        @Override
        public int hashCode() {
            return 13 + getCoordinate().hashCode() + getDirection().hashCode();
        }

        @Override
        public String toString() {
            return "Vector{coordinate=" + getCoordinate().toString() + ";direction=" + getDirection().toString() + "}";
        }

        static Vector from(Coordinate coordinate, Direction direction) {
            return CACHE.computeIfAbsent(coordinate, c -> new HashMap<>())
                        .computeIfAbsent(direction, d -> new Vector(coordinate, direction));
        }
    }

    static Optional<Board> buildBoard(String[] crossword) {
         if(       crossword == null
                || crossword.length == 0
                || crossword[0] == null)
            return Optional.empty();

        int height = crossword.length;
        int width = crossword[0].length();

        Board.Builder boardBuilder = Board.Builder.newBuilder(height, width);

        for(int y = 0; y < crossword.length; y++) {
            String line = crossword[y];

            for(int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);

                if(c != '-') continue;

                boardBuilder.addCell(Coordinate.at(x, y));
            }
        }

        Board board = boardBuilder.build();

        return Optional.of(board);
    }

    static String[] crosswordPuzzle(String[] crossword, String wordString) {
        System.err.println("Building crossword puzzle board");

        Optional<Board> maybeBoard = buildBoard(crossword);
        if(!maybeBoard.isPresent())
            return new String[0];

        Board board = maybeBoard.get();
        String[] words = wordString.split(";");

        System.err.println("Solving puzzle");
        Optional<Puzzle.Solution> maybeSolution = Puzzle.Solver.solve(board, words);
        
        if(!maybeSolution.isPresent()) {
            return new String[0];
        }
        
        Puzzle.Solution solution = maybeSolution.get();

        System.err.println("Turning solution into string array");

        return solutionToStringArray(crossword, solution);
    }

    private static String[] solutionToStringArray(String[] crossword, Puzzle.Solution solution) {
        String[] stringArray = new String[crossword.length];

        StringBuilder sb = new StringBuilder();

        for(int y = 0; y < crossword.length; y++) {
            String line = crossword[y];

            for(int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);

                if(c != '-') {
                    sb.append(c);
                } else {
                    sb.append(solution.charAt(Coordinate.at(x, y)));
                }
            }

            stringArray[y] = sb.toString();
            sb.setLength(0);
        }

        return stringArray;
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        String[] crossword = new String[10];

        for (int i = 0; i < 10; i++) {
            String crosswordItem = scanner.nextLine();
            crossword[i] = crosswordItem;
        }

        String words = scanner.nextLine();

        String[] result = crosswordPuzzle(crossword, words);

        System.err.println("Printing solution");

        for (int i = 0; i < result.length; i++) {
            bufferedWriter.write(result[i]);

            if (i != result.length - 1) {
                bufferedWriter.write("\n");
            }
        }

        bufferedWriter.newLine();

        bufferedWriter.close();

        scanner.close();
    }
}
