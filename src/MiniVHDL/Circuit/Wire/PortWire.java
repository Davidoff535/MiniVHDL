package MiniVHDL.Circuit.Wire;

import MiniVHDL.Circuit.*;

/**
 * Represents a wire that refers to an {@link Port} of an {@link Entity}.
 */
public class PortWire extends Wire {
    public Port port;


    public PortWire(int width, Port port) {
        super(width);
        this.port = port;
    }

    @Override
    public String getName() {
        return port.name;
    }

    @Override
    public String toString() {
        return "PortWire{" +
                "port=" + port +
                ", width=" + width +
                '}';
    }
}
