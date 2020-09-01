package kaptainwutax.sudokuthing.component;

import kaptainwutax.sudokuthing.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hint {

    private final int[] values;
    private final int sum;

    public Hint(int... values) {
        this.values = values;
        this.sum = Arrays.stream(this.values).sum();
    }

    public int[] getValues() {
        return this.values;
    }

    public boolean isValid(int size) {
        for(int value: this.values)if(value <= 0)return false;
        if(this.sum + this.values.length - 1 > size)return false;
        return true;
    }

    public boolean isEmptyLine(int size) {
        return this.values.length == 0;
    }

    public boolean isFreeLine(int size) {
        return this.sum + this.values.length - 1 == size;
    }

    public long getPermutationCount(int size) {
        return Helper.getPermutationCount(size, this.values);
    }

    public List<Line> getPermutations(int size, Line restriction) {
        List<Line> permutations = new ArrayList<>();

        Helper.permute(line -> {
            permutations.add(line.copy());
            return true;
        }, size, restriction, this.values);

        return permutations;
    }

}
