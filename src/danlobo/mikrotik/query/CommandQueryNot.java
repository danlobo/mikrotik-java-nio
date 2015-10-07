package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

public class CommandQueryNot implements CommandQuery {
    CommandQuery query;

    public CommandQueryNot(CommandQuery query) {
        this.query = query;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandQueryNot)) return false;

        CommandQueryNot that = (CommandQueryNot) o;

        if (query != that.query) return false;

        return true;
    }

    public int hashCode() {
        return query.hashCode();
    }

    @Override
    public void accept(CommandQueryVisitor visitor) throws MikrotikProtocolException {
        visitor.visit(this);
    }

    public CommandQuery getQuery() {
        return query;
    }
}
