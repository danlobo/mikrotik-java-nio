package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

public class CommandQueryAnd implements CommandQuery {

    private CommandQuery a;
    private CommandQuery b;

    public CommandQueryAnd(CommandQuery a, CommandQuery b) {
        this.a = a;
        this.b = b;
    }

    public CommandQuery getA() { return a; }
    public CommandQuery getB() { return b; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandQueryAnd)) return false;

        CommandQueryAnd that = (CommandQueryAnd) o;

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
}
