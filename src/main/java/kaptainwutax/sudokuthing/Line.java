package kaptainwutax.sudokuthing;

import java.util.Arrays;
import java.util.function.Predicate;

public class Line {

    public static long getPermutations(int size, int... hints) {
        int sum = Arrays.stream(hints).sum();
        int trailing = size - sum + 1;
        System.out.println(trailing + " choose " + (trailing - hints.length));
        return Combinatorics.getCombinations(trailing, trailing - hints.length);
    }

    public static void permute(Predicate<LongPack> action, int size, int... hints) {
        int sum = Arrays.stream(hints).sum();
        int trailing = size - sum + 1 - hints.length;
        int blocks = hints.length;

        permuteInternal(new boolean[hints.length + size - sum], blocks, trailing, 0, 0, permutation -> {
            LongPack line = new LongPack(size);

            for(int i = 0, p = 0, b = 0; i < permutation.length; i++) {
                boolean state = permutation[i];

                if(!state) {
                    line.set(p++, false);
                    continue;
                }

                for(int j = 0; j < hints[b]; j++)line.set(p++, true);
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
