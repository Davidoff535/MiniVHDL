package MiniVHDL.Circuit.Wire;

import MiniVHDL.Circuit.*;

/**
 * Represents a wire that corresponds to a {@link Signal} in the circuit.
 */
public class SignalWire extends Wire {
    public Signal signal;

    public SignalWire(int width, Signal signal) {
        super(width);
        this.signal = signal;
    }

    @Override
    public String getName() {
        return signal.name;
    }

    @Override
    public String toString() {
        return "SignalWire{" +
                "signal=" + signal +
                ", width=" + width +
                '}';
    }
}
