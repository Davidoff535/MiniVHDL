package MiniVHDL.Circuit.Expression;

/**
 * Represents a binary operation (like AND, OR, XOR, etc.) between two sub-expressions.
 */
public class OperationExpression extends Expression {
    public Expression left;
    public Expression right;
    public Operation operation;

    /**
     * Defines supported operations for {@link OperationExpression}, such as AND, OR, XOR, etc.
     */
    public enum Operation {
        XOR,
        AND,
        OR,
        NAND,
        NOR,
        XNOR,
        CAT
    }

    public OperationExpression(int width, Expression left, Expression right, Operation operation) {
        super(width);
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "OperationExpression{" +
                "left=" + left +
                ", right=" + right +
                ", operation=" + operation +
                ", width=" + width +
                '}';
    }
}
