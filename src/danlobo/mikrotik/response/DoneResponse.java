package danlobo.mikrotik.response;

import java.util.Map;

public class DoneResponse extends Response {
    Map<String, String> _attrs;

    public DoneResponse(String tag, Map<String, String> attrs) {
        super(tag);
        this._attrs = attrs;
    }

    public Map<String, String> getAttrs() { return _attrs; }

    @Override
    public String toString() {
        String t = "DONE: ";
        if (_attrs != null && _attrs.size() > 0) {
            for(Map.Entry<String, String> entry : _attrs.entrySet()) {
                t += entry.getKey() + "=" + entry.getValue() + ", ";
            }
            t = t.substring(0, t.length() - 2);
        }

        return t;
    }
}
