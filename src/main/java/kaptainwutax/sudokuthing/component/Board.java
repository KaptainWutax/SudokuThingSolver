package kaptainwutax.sudokuthing.component;

import java.util.Random;

public class Board {

    private final Node[][] elements;

    private Board(int rows, int columns) {
        this.elements = new Node[rows][columns];
    }

    public Board(int rows, int columns, Generator generator) {
        this(rows, columns);

        for(int row = 0; row < rows; row++) {
            for(int column = 0; column < columns; column++) {
                this.elements[row][column] = generator.getValue(row, column);
            }
        }
    }

    public static Board random(int rows, int columns, Random random) {
        return new Board(rows, columns, (row, column) -> random.nextBoolean() ? Node.FILLED : Node.EMPTY);
    }

    public int getRowCount() {
        return this.elements.length;
    }

    public int getColumnCount() {
        return this.elements[0].length;
    }

    public boolean isSquare() {
        return this.getRowCount() == this.getColumnCount();
    }

    public Hint[] getRowHints() {
        Hint[] hints = new Hint[this.getRowCount()];

        for(int i = 0; i < this.getRowCount(); i++) {
            Line row = this.getRow(i);
            hints[i] = row.getHint();
        }

        return hints;
    }

    public Hint[] getColumnHints() {
        Hint[] hints = new Hint[this.getColumnCount()];

        for(int i = 0; i < this.getColumnCount(); i++) {
            Line row = this.getColumn(i);
            hints[i] = row.getHint();
        }

        return hints;
    }

    public Generator toGenerator() {
        return this::get;
    }

    public Mapper toMapper() {
        return this.toGenerator().asMapper();
    }

    public Node get(int row, int column) {
        return this.elements[row][column];
    }

    public Board set(int row, int column, Node value) {
        this.elements[row][column] = value;
        return this;
    }

    public Line getRowCopy(int row) {
        return new Line(this.getColumnCount(), i -> this.get(row, i));
    }

    public Line getColumnCopy(int column) {
        return new Line(this.getRowCount(), i -> this.get(i, column));
    }

    public Line.View getRow(int row) {
        return new Line.View(this.getColumnCount(),
                column -> this.get(row, column), (column, value) -> this.set(row, column, value));
    }

    public Line.View getColumn(int column) {
        return new Line.View(this.getRowCount(),
                row -> this.get(row, column), (row, value) -> this.set(row, column, value));
    }

    public Board setRow(int row, Line value) {
        return this.mapRowAndSet(row, (index, oldValue) -> value.get(index));
    }

    public Board setColumn(int column, Line value) {
        return this.mapColumnAndSet(column, (index, oldValue) -> value.get(index));
    }

    public Board withRow(int row, Line value) {
        return this.mapRow(row, value.toMapper());
    }

    public Board withColumn(int column, Line value) {
        return this.mapColumn(column, value.toMapper());
    }

    public Board map(Mapper mapper) {
        return new Board(this.getRowCount(), this.getColumnCount(), (row, column) -> mapper.getNewValue(row, column, this.get(row, column)));
    }

    public Board mapAndSet(Mapper mapper) {
        for(int row = 0; row < this.getRowCount(); row++) {
            for(int column = 0; column < this.getColumnCount(); column++) {
                this.set(row, column, mapper.getNewValue(row, column, this.get(row, column)));
            }
        }

        return this;
    }

    public Board mapRow(int row, Line.Mapper mapper) {
        return new Board(this.getRowCount(), this.getColumnCount(), (row1, column) -> row == row1 ? mapper.getNewValue(column, this.get(row, column)) : this.get(row1, column));
    }

    public Board mapRowAndSet(int row, Line.Mapper mapper) {
        for(int column = 0; column < this.getColumnCount(); column++) {
            this.set(row, column, mapper.getNewValue(column, this.get(row, column)));
        }

        return this;
    }

    public Board mapColumn(int column, Line.Mapper mapper) {
        return new Board(this.getRowCount(), this.getColumnCount(), (row, column1) -> column == column1 ? mapper.getNewValue(row, this.get(row, column)) : this.get(row, column1));
    }

    public Board mapColumnAndSet(int column, Line.Mapper mapper) {
        for(int row = 0; row < this.getRowCount(); row++) {
            this.set(row, column, mapper.getNewValue(row, this.get(row, column)));
        }

        return this;
    }

    public Board copy() {
        return new Board(this.getRowCount(), this.getColumnCount(), this.toGenerator());
    }

    @Override
    public int hashCode() {
        int result = 1;

        for(int i = 0; i < this.getRowCount(); i++) {
            for(int j = 0; j < this.getColumnCount(); j++) {
                result = 31 * result + this.get(i, j).hashCode();
            }
        }

        return this.getRowCount() * 961 + this.getColumnCount() * 31 + result;
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)return true;
        if(!(other instanceof Board))return false;
        Board matrix = (Board)other;
        if(this.getRowCount() != matrix.getRowCount())return false;
        if(this.getColumnCount() != matrix.getColumnCount())return false;

        for(int row = 0; row < this.getRowCount(); row++) {
            for(int column = 0; column < this.getColumnCount(); column++) {
                if(!this.get(row, column).equals(matrix.get(row, column)))return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < this.getRowCount(); i++) {
            sb.append(this.getRow(i).toString()).append(i < this.getRowCount() - 1 ? ", " : "");
        }

        return sb.toString();
    }

    public String toBinaryString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < this.getRowCount(); i++) {
            sb.append("[").append(this.getRow(i).toBinaryString()).append("]").append(i < this.getRowCount() - 1 ? "\n" : "");
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface Generator {
        Node getValue(int row, int column);

        default Mapper asMapper() {
            return (row, column, oldValue) -> this.getValue(row, column);
        }
    }

    @FunctionalInterface
    public interface Mapper {
        Node getNewValue(int row, int column, Node oldValue);

        default Generator asGenerator() {
            return (row, column) -> this.getNewValue(row, column, null);
        }
    }

}
