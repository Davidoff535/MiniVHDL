package MiniVHDL.Circuit;

/**
 * Represents an instance of a {@link Component} within an {@link Architecture}.
 * Each instance has a name and a corresponding component to define its ports.
 */
public class Instance {
    public String name;
    public Component component;

    public Instance(String name, Component component) {
        this.name = name;
        this.component = component;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", component=" + component +
                '}';
    }
}
