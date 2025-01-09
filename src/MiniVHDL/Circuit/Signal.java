package MiniVHDL.Circuit;

/**
 * Represents a signal definition within an {@link Architecture}, having a name,
 * bit-length, and an offset for indexing.
 */
public class Signal {
    public String name;
    public int length;
    public int offset;

    public Signal(String name, int length, int offset) {
        this.name = name;
        this.length = length;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", offset=" + offset +
                '}';
    }
}
