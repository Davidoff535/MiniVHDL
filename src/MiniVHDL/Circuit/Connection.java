package MiniVHDL.Circuit;

import MiniVHDL.Circuit.Expression.*;

public class Connection {
    public Expression from;
    public Expression to;

    public Connection(Expression from, Expression to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
