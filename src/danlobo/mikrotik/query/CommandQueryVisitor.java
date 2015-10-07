package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

import java.util.Stack;

public interface CommandQueryVisitor {
    Stack getS();

    void visit(CommandQueryAnd c) throws MikrotikProtocolException;
    void visit(CommandQueryOr c) throws MikrotikProtocolException;
    void visit(CommandQueryNot c) throws MikrotikProtocolException;
    void visit(CommandQueryExpression c) throws MikrotikProtocolException;
}