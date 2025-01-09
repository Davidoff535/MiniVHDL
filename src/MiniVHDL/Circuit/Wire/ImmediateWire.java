package MiniVHDL.Circuit.Wire;

import java.util.List;

/**
 * Represents a wire with a constant set of boolean values (e.g., "1001").
 */
public class ImmediateWire extends Wire {
    public boolean[] value;

    public ImmediateWire(int width, boolean[] value) {
        super(width);
        this.value = value;
    }

    public ImmediateWire(int width, List<Boolean> values) {
        this(width, BooleanListToArr(values));
    }

    public static boolean[] BooleanListToArr(List<Boolean> list) {
        boolean[] arr = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }


    @Override
    public String getName() {
        StringBuilder name = new StringBuilder();
        for (boolean val : value) {
            name.append(val ? "1" : "0");
        }
        return name.toString();
    }
}
