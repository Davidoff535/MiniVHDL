package MiniVHDL.Circuit;

import java.util.List;

/**
 * Represents a VHDL Entity, which includes a name, a list of {@link Port} objects,
 * and an associated {@link Architecture}.
 */
public class Entity {
    public String name;
    public List<Port> ports;
    public Architecture architecture;

    public Entity(String name, List<Port> ports) {
        this.name = name;
        this.ports = ports;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", ports=" + ports +
                ", architecture=" + architecture +
                '}';
    }
}
