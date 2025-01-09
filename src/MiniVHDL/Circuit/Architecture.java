package MiniVHDL.Circuit;

import MiniVHDL.Circuit.Expression.*;
import MiniVHDL.Circuit.Wire.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Architecture of an {@link Entity}, holding components, signals,
 * instances, and connections. It also provides functionality to determine
 * if wires (ports/signals/instance ports) are completely driven.
 */
public class Architecture {
    public String name;
    public Entity entity;
    public List<Component> components = new ArrayList<>();
    public List<Signal> signals = new ArrayList<>();
    public List<Instance> instances = new ArrayList<>();
    public List<Connection> connections = new ArrayList<>();

    public Architecture(String name) {
        this.name = name;
    }

    public Wire getWireFromIdent(String ident) {
        for (Port port : entity.ports) {
            if (port.name.equals(ident)) {
                return new PortWire(port.length, port);
            }
        }
        for (Signal signal : signals) {
            if (signal.name.equals(ident)) {
                return new SignalWire(signal.length, signal);
            }
        }
        return null;
    }

    public Component getComponentFromIdent(String ident) {
        for (Component component : components) {
            if (component.name.equals(ident)) {
                return component;
            }
        }
        return null;
    }

    public void addPortConnection(Instance instance, Expression expr, int portIndex) {
        if (expr instanceof PortWire && ((PortWire) expr).port == null) {
            return;
        }
        Port port = instance.component.ports.get(portIndex);
        InstancePortWire instancePortWire = new InstancePortWire(port.length, instance, port);
        if (port.direction == Port.Direction.OUT) {
            connections.add(new Connection(instancePortWire, expr));
        } else {
            connections.add(new Connection(expr, instancePortWire));
        }
    }

    public boolean isAlreadyDriven(Expression expr) {
        int indexFrom = 0;
        int indexTo = expr.width - 1;
        if (expr instanceof WidthExpression widthExpr) {
            indexFrom = widthExpr.offset;
            indexTo = widthExpr.offset + widthExpr.width - 1;
            expr = widthExpr.source;
        }
        for (Connection c : connections) {
            Expression to = c.to;
            int c_indexFrom = 0;
            int c_indexTo = to.width - 1;
            if (c.to instanceof WidthExpression widthExpr) {
                c_indexFrom = widthExpr.offset;
                c_indexTo = widthExpr.offset + widthExpr.width - 1;
                to = widthExpr.source;
            }

            if (indexFrom <= c_indexTo && c_indexFrom <= indexTo) {
                if (to instanceof SignalWire toWire && expr instanceof SignalWire exprWire) {
                    if (toWire.signal == exprWire.signal) {
                        return true;
                    }
                }
                if (to instanceof PortWire toWire && expr instanceof PortWire exprWire) {
                    if (toWire.port == exprWire.port) {
                        return true;
                    }
                }
                if (to instanceof InstancePortWire toWire && expr instanceof InstancePortWire exprWire) {
                    if (toWire.instance == exprWire.instance && toWire.port == exprWire.port) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getUndrivenWires() {
        StringBuilder sb = new StringBuilder();
        for (Port port : entity.ports) {
            if (port.direction == Port.Direction.OUT && isNotCompletelyDriven(new PortWire(port.length, port))) {
                sb.append("Port %s of entity %s\n".formatted(port.name, entity.name));
            }
        }
        for (Signal signal : signals) {
            if (isNotCompletelyDriven(new SignalWire(signal.length, signal))) {
                sb.append("Signal %s of entity %s\n".formatted(signal.name, entity.name));
            }
        }
        for (Instance instance : instances) {
            for (Port port : instance.component.ports) {
                if (port.direction == Port.Direction.IN && isNotCompletelyDriven(new InstancePortWire(port.length, instance, port))) {
                    sb.append("Instance Port %s of instance %s in entity %s\n".formatted(port.name, instance.name, entity.name));
                }
            }
        }
        return sb.toString();
    }

    public boolean isNotCompletelyDriven(Wire expr) {
        List<Integer> notDriven = new ArrayList<>();
        for (int i = 0; i < expr.width; i++) {
            notDriven.add(i);
        }
        for (Connection c : connections) {
            Expression to = c.to;
            if (to == null) {
                continue;
            }
            int c_indexFrom = 0;
            int c_indexTo = to.width - 1;
            if (c.to instanceof WidthExpression widthExpr) {
                c_indexFrom = widthExpr.offset;
                c_indexTo = widthExpr.offset + widthExpr.width - 1;
                to = widthExpr.source;
            }
            int finalC_indexFrom = c_indexFrom;
            int finalC_indexTo = c_indexTo;
            if (to instanceof SignalWire toWire && expr instanceof SignalWire exprWire) {
                if (toWire.signal == exprWire.signal) {
                    notDriven.removeIf(integer -> integer >= finalC_indexFrom && integer <= finalC_indexTo);
                }
            }
            if (to instanceof PortWire toWire && expr instanceof PortWire exprWire) {
                if (toWire.port == exprWire.port) {
                    notDriven.removeIf(integer -> integer >= finalC_indexFrom && integer <= finalC_indexTo);
                }
            }
            if (to instanceof InstancePortWire toWire && expr instanceof InstancePortWire exprWire) {
                if (toWire.instance == exprWire.instance && toWire.port == exprWire.port) {
                    notDriven.removeIf(integer -> integer >= finalC_indexFrom && integer <= finalC_indexTo);
                }
            }
        }
        return !notDriven.isEmpty();
    }


    @Override
    public String toString() {
        return "Architecture{" +
                "name='" + name + '\'' +
                ", components=" + components +
                ", signals=" + signals +
                ", instances=" + instances +
                ", connections=" + connections +
                '}';
    }
}
