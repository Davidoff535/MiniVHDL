package MiniVHDL.Circuit.Wire;

import MiniVHDL.Circuit.Expression.*;

/**
 * An abstract class to represent bottom-level expressions (like ports, signals) in the circuit.
 * Wires have a width and a name, which derived classes implement.
 */
public abstract class Wire extends Expression {
    public Wire(int width) {
        super(width);
    }

    public abstract String getName();
}
