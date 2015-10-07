package danlobo.mikrotik.response;

import java.util.Map;

public class ReResponse extends DoneResponse {
    public ReResponse(String tag, Map<String, String> attrs) {
        super(tag, attrs);
    }

    @Override
    public String toString() {
        String t = "RE: ";
        if (_attrs != null &&  _attrs.size() > 0) {
            for(Map.Entry<String, String> entry : _attrs.entrySet()) {
                t += entry.getKey() + "=" + entry.getValue() + ", ";
            }
            t = t.substring(0, t.length() - 2);
        }
        return t;
    }
}
