package MiniVHDL.Circuit;

import java.util.List;

/**
 * Represents a reusable component definition,
 * which can be instantiated as an {@link Instance} in an {@link Architecture}.
 */
public class Component {
    public String name;
    public List<Port> ports;

    public Component(String name, List<Port> ports) {
        this.name = name;
        this.ports = ports;
    }

    @Override
    public String toString() {
        return "Component{" +
                "name='" + name + '\'' +
                ", ports=" + ports +
                '}';
    }
}
