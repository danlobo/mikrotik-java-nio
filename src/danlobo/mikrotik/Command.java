package danlobo.mikrotik;

import danlobo.mikrotik.query.CommandQuery;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class Command {
    private String _path;
    private Map<String, String> _attributes;
    private CommandQuery _query;

    String tag;

    public Command(String path) {
        this(path, new HashMap<String, String>(), null);
    }

    public Command(String path, Map<String, String> attributes) {
        this(path, attributes, null);
    }

    public Command(String path, CommandQuery query) {
        this(path, new HashMap<String, String>(), query);
    }

    public Command(String path, Map<String, String> attributes, CommandQuery query) {
        this._attributes = attributes;
        this._path = path;
        this._query = query;
    }

    public Command(byte[] data) {
        throw new NotImplementedException();
    }

    public String getPath() { return _path; }
    public Map<String, String> getAttributes() { return _attributes; }
    public CommandQuery getQuery() { return _query; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        Command command = (Command) o;

        if (_attributes != command._attributes) return false;
        if (!_path.equals(command._path)) return false;
        if (_query != command._query) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = _path.hashCode();
        result = 31 * result + (_attributes != null ? _attributes.hashCode() : 0);
        result = 31 * result + (_query != null ? _query.hashCode() : 0);
        return result;
    }

    public Map<String, String> mktAttributes() {
        Map<String, String> attrs = new HashMap<String, String>();
        for(Map.Entry<String, String> entry : getAttributes().entrySet()) {
            if (entry.getValue() == null) continue;
            if (entry.getKey().equals(".id")) continue;

            if (entry.getValue().equals("true")) entry.setValue("yes");
            if (entry.getValue().equals("false")) entry.setValue("no");

            attrs.put(entry.getKey(), entry.getValue());
        }
        return attrs;
    }

    @Override
    public String toString() {
        String t = this._path + " ";
        if (getAttributes() != null && getAttributes().size() > 0){
            for(Map.Entry<String, String> entry : getAttributes().entrySet()) {
                t += entry.getKey() + "=" + entry.getValue() + " ";
            }
        }
        if (getQuery() != null)
            t += getQuery().toString();
        return t;
    }

    public String getTag() {
        return tag;
    }
}
