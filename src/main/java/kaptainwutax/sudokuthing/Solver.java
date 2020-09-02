package kaptainwutax.sudokuthing;

import kaptainwutax.sudokuthing.component.Board;
import kaptainwutax.sudokuthing.component.Hint;
import kaptainwutax.sudokuthing.component.Line;
import kaptainwutax.sudokuthing.component.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Solver {

    public static void main(String[] args) {
        for(int n = 0; n < 1; n++) {
            Board board = Board.random(30, 30, new Random(n));

            System.out.println("Answer is " + board.hashCode());
            System.out.println(board.toBinaryString());
            System.out.println("===================================");

            if(!solve(board.map((row, column, oldValue) -> Node.UNKNOWN), board.getRowHints(), board.getColumnHints(), other -> {
                System.out.println("Found solution " + other.hashCode());
                System.out.println(other.toBinaryString());
                System.out.println("===================================");
                return board.equals(other);
            })) {
                System.out.println("Seed " + n + " found no solution!");
            }
        }
    }

    public static boolean solve(int rows, int columns, String hints, Predicate<Board> action) {
        String[] lines = hints.split(Pattern.quote(";"), 2);

        Hint[] rowHints = Arrays.stream(lines[0].split(Pattern.quote(","))).map(String::trim).map(s ->
                new Hint(Arrays.stream(s.split(Pattern.quote(" "))).map(String::trim).mapToInt(Integer::parseInt).toArray())
        ).toArray(Hint[]::new);

        Hint[] columnHints = Arrays.stream(lines[1].split(Pattern.quote(","))).map(s ->
                new Hint(Arrays.stream(s.split(Pattern.quote(" "))).map(String::trim).mapToInt(Integer::parseInt).toArray())
        ).toArray(Hint[]::new);

        return solve(new Board(rows, columns, (row, column) -> Node.UNKNOWN), rowHints, columnHints, action);
    }

    public static boolean solve(Board board, Hint[] rowHints, Hint[] columnHints, Predicate<Board> action) {
        List<List<Line>> rowPermutations = new ArrayList<>();
        List<List<Line>> columnPermutations = new ArrayList<>();

        if(!fillPermutations(board, rowHints, columnHints, rowPermutations, columnPermutations))return false;

        while(true) {
            boolean dirty = false;

            for(int i = 0; i < board.getRowCount(); i++) {
                dirty |= processPermutations(board, rowPermutations.get(i), true, i);
                if(rowPermutations.get(i).isEmpty())return false;
            }

            for(int i = 0; i < board.getColumnCount(); i++) {
                dirty |= processPermutations(board, columnPermutations.get(i), false, i);
                if(columnPermutations.get(i).isEmpty())return false;
            }

            if(!dirty)break;
        }

        if(checkComplete(board))return action.test(board);

        Entry bestEntry = null;

        for(int i = 0; i < board.getRowCount() && (bestEntry == null || bestEntry.count != 1); i++) {
            List<Line> permutations = rowPermutations.get(i);
            if(permutations.size() == 1 && permutations.get(0).equals(board.getRow(i)))continue;

            if(bestEntry == null || permutations.size() < bestEntry.permutations.size()) {
                bestEntry = new Entry(i, true, permutations);
            }
        }

        for(int i = 0; i < board.getColumnCount() && (bestEntry == null || bestEntry.count != 1); i++) {
            List<Line> permutations = columnPermutations.get(i);
            if(permutations.size() == 1 && permutations.get(0).equals(board.getColumn(i)))continue;

            if(bestEntry == null || permutations.size() < bestEntry.permutations.size()) {
                bestEntry = new Entry(i, false, permutations);
            }
        }

        if(bestEntry == null)return false;

        for(int i = 0; i < bestEntry.count; i++) {
            if(solve(bestEntry.applyBoard(board, i), rowHints, columnHints, action))return true;
        }

        return false;
    }

    public static boolean fillPermutations(Board board, Hint[] rowHints, Hint[] columnHints,
                                           List<List<Line>> rowPermutations, List<List<Line>> columnPermutations) {
        for(int i = 0; i < board.getRowCount(); i++) {
            Line.View row = board.getRow(i);
            List<Line> permutations = rowHints[i].getPermutations(board.getColumnCount(), row);
            if(permutations.isEmpty())return false;
            rowPermutations.add(permutations);
        }

        for(int i = 0; i < board.getColumnCount(); i++) {
            Line.View column = board.getColumn(i);
            List<Line> permutations = columnHints[i].getPermutations(board.getRowCount(), column);
            if(permutations.isEmpty())return false;
            columnPermutations.add(permutations);
        }

        return true;
    }

    private static boolean processPermutations(Board board, List<Line> permutations, boolean isRow, int index) {
        Line old = isRow ? board.getRow(index) : board.getColumn(index);

        permutations.removeIf(permutation -> {
            for(int i = 0; i < old.getDimension(); i++) {
                Node v = old.get(i);
                if(v != Node.UNKNOWN && v != permutation.get(i))return true;
            }

            return false;
        });

        if(permutations.size() == 0)return false;

        Line mask = permutations.get(0).copy();

        for(int i = 1; i < permutations.size(); i++) {
            mask.andAndSet(permutations.get(i));
        }

        boolean dirty = false;

        for(int i = 0; i < old.getDimension(); i++) {
            Node m = mask.get(i);
            dirty |= old.get(i) != m;
            old.set(i, m);
        }

        return dirty;
    }

    private static boolean checkComplete(Board board) {
        for(int row = 0; row < board.getRowCount(); row++) {
            for(int column = 0; column < board.getColumnCount(); column++) {
                if(board.get(row, column) == Node.UNKNOWN)return false;
            }
        }

        return true;
    }

    public static class Entry {
        private final int id;
        private final boolean isRow;
        private final List<Line> permutations;

        public final int count;

        public Entry(int id, boolean isRow, List<Line> permutations) {
            this.id = id;
            this.isRow = isRow;
            this.permutations = permutations;

            this.count = this.permutations.size();
        }

        public Board applyBoard(Board board, int i) {
            return this.isRow ? board.withRow(this.id, this.permutations.get(i))
                    : board.withColumn(this.id, this.permutations.get(i));
        }
    }

}
