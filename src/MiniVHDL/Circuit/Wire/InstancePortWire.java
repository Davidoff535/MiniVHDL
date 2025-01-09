package MiniVHDL.Circuit.Wire;

import MiniVHDL.Circuit.*;

/**
 * Represents a wire that refers to a port belonging to an {@link Instance} of a {@link Component}.
 */
public class InstancePortWire extends Wire {
    public Instance instance;
    public Port port;

    public InstancePortWire(int width, Instance instance, Port port) {
        super(width);
        this.instance = instance;
        this.port = port;
    }

    @Override
    public String getName() {
        return instance.name + "_" + port.name;
    }

    @Override
    public String toString() {
        return "InstancePortWire{" +
                "instance=" + instance +
                ", port=" + port +
                ", width=" + width +
                '}';
    }
}
