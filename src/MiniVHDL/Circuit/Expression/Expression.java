package MiniVHDL.Circuit.Expression;

import MiniVHDL.Circuit.Port;
import MiniVHDL.Circuit.Wire.*;

/**
 * An abstract representation of expressions within the circuit.
 * Expressions have a width and provide utility methods such as
 * offset calculation and assignability checks.
 */
public abstract class Expression {
    public int width;

    public Expression(int width) {
        this.width = width;
    }

    public int getIndexOffset() {
        int indexOffset = 0;
        switch (this) {
            case SignalWire signalWire -> indexOffset = signalWire.signal.offset;
            case PortWire portWire -> indexOffset = portWire.port.offset;
            case InstancePortWire instancePortWire -> indexOffset = instancePortWire.port.offset;
            default -> {
            }
        }
        return indexOffset;
    }

    public boolean isAssignable() {
        if (this instanceof Wire) {
            if(this instanceof PortWire portWire) {
                return portWire.port==null || portWire.port.direction== Port.Direction.OUT;
            }else{
                return !(this instanceof ImmediateWire);
            }
        } else if (this instanceof WidthExpression) {
            return ((WidthExpression) this).source.isAssignable();
        } else {
            return false;
        }
    }
}
