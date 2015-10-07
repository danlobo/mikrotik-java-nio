package danlobo.mikrotik.response;

public abstract class Response {
    private String tag;

    public Response() {

    }

    public String getTag() { return tag; }

    public Response(String tag) {
        this.tag = tag;
    }
}
