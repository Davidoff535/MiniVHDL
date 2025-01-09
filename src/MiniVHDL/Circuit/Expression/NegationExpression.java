package MiniVHDL.Circuit.Expression;

/**
 * Represents a logical negation (NOT) of a single expression.
 */
public class NegationExpression extends Expression {
    public Expression expression;

    public NegationExpression(int width, Expression expression) {
        super(width);
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "NegationExpression{" +
                "expression=" + expression +
                ", width=" + width +
                '}';
    }
}
