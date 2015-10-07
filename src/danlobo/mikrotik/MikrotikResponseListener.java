package danlobo.mikrotik;

import java.util.List;
import java.util.Map;

public interface MikrotikResponseListener {
    int inQueue();

    void update(Map<String, String> item);
    void complete();

    Exception getException();

    void error(Exception e);
    List<Map<String, String>> getData();
    boolean isCompleted();
}
