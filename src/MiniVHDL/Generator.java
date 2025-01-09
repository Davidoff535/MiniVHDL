package MiniVHDL;
import MiniVHDL.Circuit.*;
import MiniVHDL.Circuit.Expression.*;
import MiniVHDL.Circuit.Wire.*;


import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Generator} class is responsible for generating FIRRTL code
 * from a given MiniVHDL {@link Circuit}. It translates circuit components,
 * connections, and expressions into FIRRTL format, ensuring correctness
 * and compliance with FIRRTL syntax.
 */
class Generator {
    StringBuilder program;
    int depth;

    /**
     * Generates FIRRTL code for the specified circuit and top-level module.
     *
     * @param circuitName the name of the top-level module in the circuit.
     * @param circuit     the {@link Circuit} object representing the MiniVHDL design.
     * @return a string containing the FIRRTL representation of the circuit.
     */
    public String generate(String circuitName, Circuit circuit) {
        circuitName=circuitName.toLowerCase();
        program = new StringBuilder();
        resolveIndexedAssignment(circuit);
        depth = 0;
        append("circuit %s :".formatted(circuitName));
        depth++;
        for (Entity entity : circuit.entities) {
            generateEntity(entity);
        }
        depth--;
        return program.toString();
    }

    /**
     * Generates the FIRRTL representation for a specific {@link Entity}.
     *
     * @param entity the entity to be translated into FIRRTL.
     */
    private void generateEntity(Entity entity) {
        append("module %s :".formatted(entity.name));
        depth++;
        for (Port port : entity.ports) {
            if (port.direction == Port.Direction.IN) {
                append("input %s : UInt<%d>".formatted(port.name, port.length));
            } else if (port.direction == Port.Direction.OUT) {
                append("output %s : UInt<%d>".formatted(port.name, port.length));
            }
        }
        Architecture architecture = entity.architecture;
        program.append("\n");

        for (Signal signal : architecture.signals) {
            append("wire %s : UInt<%d>".formatted(signal.name, signal.length));
        }
        program.append("\n");

        for (Instance instance : architecture.instances) {
            append("inst %s of %s".formatted(instance.name, instance.component.name));
        }
        program.append("\n");

        for (Connection connection : architecture.connections) {
            append("%s<=%s".formatted(serializeExpression(connection.to), serializeExpression(connection.from)));
        }

        depth--;
    }

    /**
     * Serializes a given {@link Expression} into its FIRRTL representation.
     *
     * @param expression the expression to be serialized.
     * @return a string containing the FIRRTL representation of the expression.
     */
    private String serializeExpression(Expression expression) {
        StringBuilder sb = new StringBuilder();
        switch (expression) {
            case NegationExpression expr -> sb.append("not(%s)".formatted(serializeExpression(expr.expression)));
            case OperationExpression expr -> {
                String operands = "%s,%s".formatted(serializeExpression(expr.left), serializeExpression(expr.right));
                OperationExpression.Operation op = expr.operation;
                if (op == OperationExpression.Operation.OR) {
                    sb.append("or(%s)".formatted(operands));
                } else if (op == OperationExpression.Operation.AND) {
                    sb.append("and(%s)".formatted(operands));
                } else if (op == OperationExpression.Operation.XOR) {
                    sb.append("xor(%s)".formatted(operands));
                } else if (op == OperationExpression.Operation.CAT) {
                    sb.append("cat(%s)".formatted(operands));
                } else if (op == OperationExpression.Operation.NOR) {
                    sb.append("not(or(%s))".formatted(operands));
                } else if (op == OperationExpression.Operation.NAND) {
                    sb.append("not(and(%s))".formatted(operands));
                } else if (op == OperationExpression.Operation.XNOR) {
                    sb.append("not(xor(%s))".formatted(operands));
                }
            }
            case WidthExpression expr -> {
                String serializedSource = serializeExpression(expr.source);
                if (expr.width <= expr.source.width) {
                    sb.append("bits(%s,%d,%d)".formatted(serializedSource, expr.offset + expr.width - 1, expr.offset));
                } else {
                    if (expr.width > 1) {
                        sb.append("cat(".repeat(expr.width - 1));
                        sb.append(serializedSource).append(", ");
                        sb.append((serializedSource + "), ").repeat(expr.width - 2));
                        sb.append(serializedSource).append(")");
                    }
                }
            }
            case PortWire wire -> sb.append(wire.port.name);
            case SignalWire wire -> sb.append(wire.signal.name);
            case InstancePortWire wire -> sb.append(wire.instance.name).append(".").append(wire.port.name);
            case ImmediateWire wire -> {
                StringBuilder val = new StringBuilder();
                for (int i = 0; i < wire.width; i++) {
                    val.append(wire.value[i] ? "1" : "0");
                }
                sb.append("UInt<%d>(\"b%s\")".formatted(wire.width, val));
            }
            case null, default -> {
            }
        }
        return sb.toString();
    }

    /**
     * Resolves assignments to indexed wires in the circuit since FIRRTL does not support this.
     * <p>
     * This process creates intermediate 1-bit wires for each bit of the indexed wire.
     * These intermediate wires are then concatenated and assigned to the target wire.
     * </p>
     *
     * @param circuit the {@link Circuit} containing the connections to be resolved.
     */
    private void resolveIndexedAssignment(Circuit circuit) {
        for (Entity entity : circuit.entities) {
            Architecture architecture = entity.architecture;
            int i = 0;
            while (i < architecture.connections.size()) {
                Connection connection = architecture.connections.get(i);
                if (connection.to instanceof WidthExpression expr) {
                    Wire wire = (Wire) expr.source;
                    String prefix = getUniquePrefix(architecture, wire.getName());
                    List<Signal> tempSignals = new ArrayList<>();
                    for (int j = 0; j < wire.width; j++) {
                        Signal signal = new Signal(prefix + j, 1, 0);
                        architecture.signals.add(signal);
                        tempSignals.add(signal);
                    }
                    replaceAllAssignments(architecture, wire, tempSignals);
                    Expression from = new SignalWire(1, tempSignals.getFirst());
                    for (int j = 1; j < tempSignals.size(); j++) {
                        from = new OperationExpression(j + 1, new SignalWire(1, tempSignals.get(j)), from, OperationExpression.Operation.CAT);
                    }
                    architecture.connections.add(new Connection(from, wire));
                    i = 0;
                    continue;
                }
                i++;
            }
        }
    }

    /**
     * Replaces all assignments in the architecture to a given wire with temporary signals.
     *
     * @param architecture the {@link Architecture} to modify.
     * @param to           the {@link Wire} to replace.
     * @param tempSignals  the temporary {@link Signal} objects to use for replacement.
     */
    private void replaceAllAssignments(Architecture architecture, Wire to, List<Signal> tempSignals) {
        int i = 0;
        while (i < architecture.connections.size()) {
            Connection connection = architecture.connections.get(i);
            if (connection.to instanceof WidthExpression expr) {
                Wire wire = (Wire) expr.source;
                if (wire.getName().equals(to.getName())) {
                    architecture.connections.remove(i);
                    if(expr.width==1){
                        architecture.connections.add(new Connection(connection.from, new SignalWire(1, tempSignals.get(expr.offset))));
                    }else{
                        for(int j=0;j<expr.width;j++){
                            Expression newTo =new WidthExpression(1,connection.from,j);
                            architecture.connections.add(new Connection(newTo, new SignalWire(1, tempSignals.get(j+expr.offset))));
                        }
                    }

                    continue;
                }
            }
            i++;
        }
    }

    /**
     * Generates a unique prefix for a signal or wire within the given architecture.
     *
     * @param architecture the {@link Architecture} to check for uniqueness.
     * @param prefix       the base prefix to modify.
     * @return a unique prefix string.
     */
    private String getUniquePrefix(Architecture architecture, String prefix) {
        do {
            prefix = "_" + prefix;
        } while (!checkIfPrefixIsUnique(architecture, prefix));
        return prefix;
    }

    /**
     * Checks whether a given prefix for a signal or wire is unique within the architecture.
     *
     * @param architecture the {@link Architecture} to check.
     * @param prefix       the prefix to check.
     * @return {@code true} if the prefix is unique; {@code false} otherwise.
     */
    private boolean checkIfPrefixIsUnique(Architecture architecture, String prefix) {
        for (Port port : architecture.entity.ports) {
            if (port.name.startsWith(prefix)) {
                return false;
            }
        }
        for (Instance instance : architecture.instances) {
            if (instance.name.startsWith(prefix)) {
                return false;
            }
        }
        for (Signal signal : architecture.signals) {
            if (signal.name.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Appends a line to the generated FIRRTL program, respecting the current indentation depth.
     *
     * @param line the line to append.
     */
    private void append(String line) {
        program.append("  ".repeat(depth));
        program.append(line).append("\n");
    }
}
