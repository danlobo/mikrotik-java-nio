package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

public interface CommandQuery {
    void accept(CommandQueryVisitor visitor) throws MikrotikProtocolException;
    boolean equals(Object o);
    int hashCode();
}