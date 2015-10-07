package danlobo.mikrotik.response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TrapResponse extends DoneResponse {

    private static final List<String> categories = Arrays.asList(
            "missing item or command",
            "argument value failure",
            "execution of command interrupted",
            "scripting related failure",
            "general failure",
            "API related failure",
            "TTY related failure",
            "value generated with :return command"
        );

    public TrapResponse(String tag, Map<String, String> attrs) {
        super(tag, attrs);
    }

    public String getMessage() {
        return getAttrs().get("message");
    }

    String getCategory() {
        String cat = getAttrs().get("category");
        if (cat != null) {
            try {
                int icat = Integer.parseInt(cat);
                if (icat >= 0 && icat < categories.size())
                    return categories.get(icat);
                return null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String t = "TRAP: ";
        if (_attrs != null &&  _attrs.size() > 0) {
            for(Map.Entry<String, String> entry : _attrs.entrySet()) {
                t += entry.getKey() + "=" + entry.getValue() + ", ";
            }
            t = t.substring(0, t.length() - 2);
        }
        return t;
    }
}
