package kaptainwutax.sudokuthing;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LongPack {

    private static final long MASK_5 = (1L << 5) - 1;

    private final int capacity;
    private final long[] packs;

    public LongPack(int capacity) {
        if(capacity <= 0) {
            throw new IndexOutOfBoundsException();
        }

        this.capacity = capacity;
        this.packs = new long[((this.capacity - 1) >>> 5) + 1];
    }

    public int size() {
        return this.capacity;
    }

    public boolean get(int index) {
        return (this.packs[index >>> 5] >>> (index & MASK_5) & 1) == 1;
    }

    public LongPack set(int index, boolean value) {
        long v = 1L << (index & MASK_5);
        if(value)this.packs[index >>> 5] |= v;
        else this.packs[index >>> 5] &= ~v;
        return this;
    }

    @Override
    public String toString() {
        return IntStream.range(0, this.size()).mapToObj(i -> this.get(i) ? "1" : "0").collect(Collectors.joining());
    }

}
