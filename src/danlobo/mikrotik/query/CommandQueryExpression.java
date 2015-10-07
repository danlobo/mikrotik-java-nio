package danlobo.mikrotik.query;

import danlobo.mikrotik.exception.MikrotikProtocolException;

public class CommandQueryExpression implements CommandQuery {
    String operand;
    CommandQueryOperation operation;
    String value;

    public CommandQueryExpression(String operand, CommandQueryOperation op, String value) {
        this.operand = operand;
        this.operation = op;
        this.value = value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandQueryExpression)) return false;

        CommandQueryExpression that = (CommandQueryExpression) o;

        if (!operand.equals(that.operand)) return false;
        if (operation != that.operation) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = operand.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public void accept(CommandQueryVisitor visitor) throws MikrotikProtocolException {
        visitor.visit(this);
    }

    public CommandQueryOperation getOperation() {
        return operation;
    }

    public String getOperand() {
        return operand;
    }

    public String getValue() {
        return value;
    }
}
