package MiniVHDL.Circuit;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the top-level MiniVHDL circuit structure which contains a collection of {@link Entity} objects.
 * Provides utility methods for retrieving entities by name and checking undriven wires.
 */
public class Circuit {
    public List<Entity> entities = new ArrayList<>();

    public Entity getEntityByName(String name) {
        for (Entity e : entities) {
            if (e.name.equals(name))
                return e;
        }
        return null;
    }

    public String getUndrivenWires() {
        StringBuilder sb = new StringBuilder();
        for (Entity e : entities) {
            String undriven = e.architecture.getUndrivenWires();
            if (!undriven.isEmpty()) {
                sb.append(undriven).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Circuit{" +
                "entities=" + entities +
                '}';
    }
}




