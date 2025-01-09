package MiniVHDL.Circuit;

import java.util.Objects;

/**
 * Represents a port definition within an {@link Entity} or {@link Component},
 * indicating its name, direction (IN/OUT), length, and offset.
 */
public class Port {
    public String name;
    public Direction direction;
    public int length;
    public int offset;

    /**
     * Defines the direction of a {@link Port}: either IN (input) or OUT (output).
     */
    public enum Direction {
        IN,
        OUT
    }

    public Port(String name, Direction direction, int length, int offset) {
        this.name = name;
        this.direction = direction;
        this.length = length;
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Port port = (Port) o;
        return length == port.length &&
                Objects.equals(name, port.name) &&
                direction == port.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, direction, length);
    }

    @Override
    public String toString() {
        return "Port{" +
                "name='" + name + '\'' +
                ", direction=" + direction +
                ", length=" + length +
                ", offset=" + offset +
                '}';
    }
}
