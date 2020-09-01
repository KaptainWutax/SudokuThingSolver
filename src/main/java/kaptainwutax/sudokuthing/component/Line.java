package kaptainwutax.sudokuthing.component;

import java.util.Arrays;

public class Line {

    private final Node[] elements;

    protected Line(int dimension) {
        this.elements = new Node[dimension];
    }

    public Line(int dimension, Generator generator) {
        this(dimension);

        for(int i = 0; i < this.elements.length; i++) {
            this.elements[i] = generator.getValue(i);
        }
    }

    public Line(Node... elements) {
        this.elements = elements;
    }

    public int getDimension() {
        return this.elements.length;
    }

    public Generator toGenerator() {
        return this::get;
    }

    public Mapper toMapper() {
        return this.toGenerator().asMapper();
    }

    public Node get(int index) {
        return this.elements[index];
    }

    public Line set(int index, Node value) {
        this.elements[index] = value;
        return this;
    }

    public Node[] getElements() {
        Node[] elements = new Node[this.getDimension()];

        for(int i = 0; i < this.getDimension(); i++) {
            elements[i] = this.get(i);
        }

        return elements;
    }

    public Line map(Mapper mapper) {
        return new Line(this.getDimension(), index -> mapper.getNewValue(index, this.get(index)));
    }

    public Line mapAndSet(Mapper mapper) {
        for(int i = 0; i < this.getDimension(); i++) {
            this.set(i, mapper.getNewValue(i, this.get(i)));
        }

        return this;
    }

    public boolean matches(Line mask) {
        for(int i = 0; i < mask.getDimension(); i++) {
            Node node = mask.get(i);
            if(node == Node.UNKNOWN)continue;
            else if(node != this.get(i))return false;
        }

        return true;
    }

    public Line copy() {
        return new Line(this.getDimension(), this.toGenerator());
    }

    @Override
    public int hashCode() {
        return this.getDimension() * 31 + Arrays.hashCode(this.getElements());
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)return true;
        if(!(other instanceof Line))return false;
        Line vector = (Line)other;
        if(this.getDimension() != vector.getDimension())return false;

        for(int i = 0; i < this.getDimension(); i++) {
            if(!this.get(i).equals(vector.get(i)))return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.getElements());
    }

    public String toBinaryString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < this.getDimension(); i++) {
            sb.append(this.get(i) == Node.UNKNOWN ? "-" : this.get(i) == Node.FILLED ? "X" : "-");
        }

        return sb.toString();
    }

    public static class View extends Line {
        private final int dimension;
        private final Generator getter;
        private final Setter setter;

        public View(int dimension, Generator getter, Setter setter) {
            super((Node[])null);
            this.dimension = dimension;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getDimension() {
            return this.dimension;
        }

        @Override
        public Node get(int index) {
            return this.getter.getValue(index);
        }

        @Override
        public Line set(int index, Node value) {
            this.setter.set(index, value);
            return this;
        }

        @FunctionalInterface
        public interface Setter {
            void set(int index, Node value);
        }
    }

    @FunctionalInterface
    public interface Generator {
        Node getValue(int index);

        default Mapper asMapper() {
            return (index, oldValue) -> this.getValue(index);
        }
    }

    @FunctionalInterface
    public interface Mapper {
        Node getNewValue(int index, Node oldValue);

        default Generator asGenerator() {
            return index -> this.getNewValue(index, null);
        }
    }

}
