package danlobo.mikrotik;

import danlobo.mikrotik.response.TrapResponse;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class MikrotikKeepAliveMessageFactory implements KeepAliveMessageFactory {
    private List<Integer> keepAliveIds;
    private Random random;

    public MikrotikKeepAliveMessageFactory() {
        random = new Random();
        keepAliveIds = new ArrayList<Integer>();
    }

    @Override
    public boolean isRequest(IoSession ioSession, Object o) {
        if (o instanceof Command) {
            Command c = (Command) o;
            boolean found = false;
            for(int i : keepAliveIds)
                if (c.getPath().equals("/" + i)) {
                    found = true;
                    break;
                }
            return found;
        }
        return false;
    }

    @Override
    public boolean isResponse(IoSession ioSession, Object o) {
        if (o instanceof TrapResponse) {
            TrapResponse r = (TrapResponse) o;

            int found = -1;
            for(int i : keepAliveIds) {
                if (r.getMessage().equals("no such command or directory (" + i + ")")) {
                    found = i;
                    break;
                }
            }
            int fidx = keepAliveIds.indexOf(found);

            if (fidx >= 0)
                keepAliveIds.remove(fidx);

            return fidx >= 0;
        }
        return false;
    }

    @Override
    public Object getRequest(IoSession ioSession) {
        int currKeepAliveId = random.nextInt();
        keepAliveIds.add(currKeepAliveId);
        return new Command("/" + currKeepAliveId);
    }

    @Override
    public Object getResponse(IoSession ioSession, Object o) {
        return o;
    }
}
