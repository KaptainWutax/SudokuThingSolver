package kaptainwutax.sudokuthing;

import kaptainwutax.sudokuthing.component.Board;
import kaptainwutax.sudokuthing.component.Hint;
import kaptainwutax.sudokuthing.component.Line;
import kaptainwutax.sudokuthing.component.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class Solver {

    public static void main(String[] args) {
        //layout: [13990, 12482, 13724, 27119, 27454, 209, 12445, 31769, 24088, 480, 23973, 8943, 21815, 31556, 8284]
        Board board = new Board(15, 15, (row, column) -> Node.UNKNOWN);
        String hints = "";
        //================================================================================================================//

        /*
        String[] lines = hints.split(Pattern.quote(";"), 2);

        Hint[] rowHints = Arrays.stream(lines[0].split(Pattern.quote(","))).map(String::trim).map(s ->
            new Hint(Arrays.stream(s.split(Pattern.quote(" "))).map(String::trim).mapToInt(Integer::parseInt).toArray())
        ).toArray(Hint[]::new);

        Hint[] columnHints = Arrays.stream(lines[1].split(Pattern.quote(","))).map(s ->
            new Hint(Arrays.stream(s.split(Pattern.quote(" "))).map(String::trim).mapToInt(Integer::parseInt).toArray())
        ).toArray(Hint[]::new);*/

        Hint[] rowHints = new Hint[] {
                new Hint(2, 1, 1, 2, 2),
                new Hint(1, 2, 2),
                new Hint(3, 2, 1, 2),
                new Hint(4, 4, 1, 2),
                new Hint(5, 2, 1, 2),
                new Hint(1, 1, 2),
                new Hint(1, 3, 1, 2),
                new Hint(1, 2, 5),
                new Hint(2, 4, 1),
                new Hint(4),
                new Hint(1, 1, 1, 2, 3, 1),
                new Hint(4, 3, 1, 1),
                new Hint(3, 2, 1, 1, 1, 1),
                new Hint(1, 1, 2, 4),
                new Hint(3, 1, 1)
        };

        Hint[] columnHints = new Hint[] {
                new Hint(1, 3, 3),
                new Hint(2, 2, 2),
                new Hint(1, 3, 1, 5),
                new Hint(3, 3, 1, 1),
                new Hint(1, 5, 1, 1),
                new Hint(1, 2, 4),
                new Hint(1, 1, 1, 1, 1, 2),
                new Hint(4, 2, 3),
                new Hint(3, 2, 2),
                new Hint(1, 1, 1, 1, 1),
                new Hint(1, 1, 2, 1, 1),
                new Hint(2, 2, 1, 1),
                new Hint(3, 3, 1, 2),
                new Hint(5, 2, 1, 2),
                new Hint(2, 2, 1, 2)
        };

        solve(board, rowHints, columnHints);
    }

    public static void solve(Board board, Hint[] rowHints, Hint[] columnHints) {
        List<Entry> entries = new ArrayList<>();

        for(int n = 0; n < 1; n++) {
            boolean dirty = false;

            for(int i = 0; i < board.getColumnCount(); i++) {
                Line column = board.getColumn(i);
                List<Line> permutations = columnHints[i].getPermutations(board.getRowCount(), column);
                if(permutations.size() == 0)return;
                dirty |= processPermutations(board, permutations, false, i);
            }

            for(int i = 0; i < board.getRowCount(); i++) {
                Line.View row = board.getRow(i);
                List<Line> permutations = rowHints[i].getPermutations(board.getColumnCount(), row);
                if(permutations.size() == 0)return;
                dirty |= processPermutations(board, permutations, true, i);
            }

            if(dirty)n--;
        }

        if(checkComplete(board))return;

        for(int i = 0; i < board.getColumnCount(); i++) {
            Line column = board.getColumn(i);
            List<Line> permutations = columnHints[i].getPermutations(board.getRowCount(), column);
            if(permutations.size() == 1 && permutations.get(0).equals(column))continue;
            entries.add(new Entry(i, false, permutations));
        }

        for(int i = 0; i < board.getRowCount(); i++) {
            Line.View row = board.getRow(i);
            List<Line> permutations = rowHints[i].getPermutations(board.getColumnCount(), row);
            if(permutations.size() == 1 && permutations.get(0).equals(row))continue;
            entries.add(new Entry(i, true, permutations));
        }

        entries.sort(Comparator.comparingInt(value -> value.count));

        for(Entry entry: new ArrayList<>(entries)) {
            for(int i = 0; i < entry.count; i++) {
                solve(entry.applyBoard(board, i), rowHints, columnHints);
            }
        }
    }

    private static boolean processPermutations(Board board, List<Line> permutations, boolean isRow, int index) {
        if(permutations.size() == 0)return false;
        Line mask = permutations.get(0).copy();

        for(int i = 1; i < permutations.size(); i++) {
            mask.andAndSet(permutations.get(i));
        }

        Line old = isRow ? board.getRow(index) : board.getColumn(index);
        boolean dirty = false;

        for(int i = 0; i < old.getDimension(); i++) {
            Node oldValue = old.get(i);

            if(oldValue != mask.get(i)) {
                old.set(i, mask.get(i));
                dirty = true;
            }
        }

        return dirty;
    }

    private static boolean checkComplete(Board board) {
        for(int row = 0; row < board.getRowCount(); row++) {
            for(int column = 0; column < board.getColumnCount(); column++) {
                if(board.get(row, column) == Node.UNKNOWN)return false;
            }
        }

        System.out.println(board.toBinaryString());
        System.out.println("============================================" + board.hashCode());
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
