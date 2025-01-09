package MiniVHDL.Circuit.Expression;

/**
 * Represents an expression that applies a subrange (offset and width) to another {@link Expression}.
 */
public class WidthExpression extends Expression {
    public Expression source;
    public int offset;

    public WidthExpression(int width, Expression source, int offset) {
        super(width);
        this.source = source;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "WidthExpression{" +
                "source=" + source +
                ", offset=" + offset +
                ", width=" + width +
                '}';
    }
}
