package kaptainwutax.sudokuthing.component;

public enum Node {

    UNKNOWN, EMPTY, FILLED;

    public int getId() {
        return this.ordinal() - 1;
    }

}
