package danlobo.mikrotik.exception;

public class MikrotikConnectionException extends Exception {
    public MikrotikConnectionException(String msg) {
        super(msg);
    }
    public MikrotikConnectionException(String msg, Exception e) {
        super(msg, e);
    }
}
