package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

public class CommandQueryOr implements CommandQuery {
    CommandQuery a;
    CommandQuery b;

    public CommandQueryOr(CommandQuery a, CommandQuery b) {
        this.b = b;
        this.a = a;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandQueryOr)) return false;

        CommandQueryOr that = (CommandQueryOr) o;

        if (!a.equals(that.a)) return false;
        if (!b.equals(that.b)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = a.hashCode();
        result = 31 * result + b.hashCode();
        return result;
    }

    @Override
    public void accept(CommandQueryVisitor visitor) throws MikrotikProtocolException {
        visitor.visit(this);
    }

    public CommandQuery getA() {
        return a;
    }

    public CommandQuery getB() {
        return b;
    }
}
