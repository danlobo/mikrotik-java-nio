package danlobo.mikrotik.response;

public class FatalResponse extends Response {
    String message;

    public FatalResponse(String message) {
        super();
        this.message = message;
    }

    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "FATAL: " + message;
    }
}
