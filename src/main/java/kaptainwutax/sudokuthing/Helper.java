package kaptainwutax.sudokuthing;

import kaptainwutax.sudokuthing.component.Line;
import kaptainwutax.sudokuthing.component.Node;

import java.util.Arrays;
import java.util.function.Predicate;

public class Helper {

    public static long getPermutationCount(int size, int... hints) {
        int sum = Arrays.stream(hints).sum();
        int trailing = size - sum + 1 - hints.length;
        return Combinatorics.getCombinations(trailing + hints.length, trailing);
    }

    public static void permute(Predicate<Line> action, int size, Line restriction, int... hints) {
        int sum = Arrays.stream(hints).sum();
        int trailing = size - sum + 1 - hints.length;
        int blocks = hints.length;

        Line line = new Line(size, index -> Node.UNKNOWN);

        permuteInternal(new boolean[hints.length + size - sum], blocks, trailing, 0, 0, permutation -> {
            for(int i = 0, p = 0, b = 0; i < permutation.length; i++) {
                boolean state = permutation[i];

                if(!state) {
                    if(restriction.get(p) == Node.FILLED)return true;
                    line.set(p++, Node.EMPTY);
                    continue;
                }

                for(int j = 0; j < hints[b]; j++) {
                    if(restriction.get(p) == Node.EMPTY)return true;
                    line.set(p++, Node.FILLED);
                }

                b++;
            }

            return action.test(line);
        });
    }

    private static boolean permuteInternal(boolean[] line, int blocks, int trailing, int blockId, int index, Predicate<boolean[]> action) {
        if(blockId == blocks)return action.test(line);

        int blocksLeft = blocks - blockId - 1;
        int blockSize = blocks * 2 - 1 + trailing - index - blocksLeft * 2;

        for(int i = 0; i < blockSize; i++) {
            line[i + index] = true;
            if(!permuteInternal(line, blocks, trailing, blockId + 1, i + index + 2, action))return false;
            line[i + index] = false;
        }

        return true;
    }

}
