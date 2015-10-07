package danlobo.mikrotik;

import danlobo.mikrotik.exception.MikrotikConnectionException;
import danlobo.mikrotik.response.DoneResponse;
import danlobo.mikrotik.response.FatalResponse;
import danlobo.mikrotik.response.ReResponse;
import danlobo.mikrotik.response.TrapResponse;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MikrotikConnectionHandler extends IoHandlerAdapter {
    private Map<String, MikrotikResponseListener> _listeners = new ConcurrentHashMap<String, MikrotikResponseListener>();

    public Map<String, MikrotikResponseListener> getListeners() {
        return _listeners;
    }

    public MikrotikResponseListener informInterest(String tag) {
        DefaultMikrotikResponseListener l = new DefaultMikrotikResponseListener(false, true);
        _listeners.put(tag, l);
        return l;
    }

    public MikrotikResponseListener informInterest(String tag, MikrotikResponseListener listener) {
        _listeners.put(tag, listener);
        return listener;
    }

    public void removeInterest(MikrotikResponseListener listener) {
        for(Map.Entry<String, MikrotikResponseListener> entry : _listeners.entrySet()) {
            if (entry.getValue() == listener)
                _listeners.remove(entry.getKey());
        }
    }

    public void removeInterest(String tag) {
        if (_listeners.containsKey(tag))
            _listeners.remove(tag);
    }

    public void dispose() {
        for(Map.Entry<String, MikrotikResponseListener> listener: _listeners.entrySet())
            listener.getValue().complete();
        _listeners.clear();
    }

    @Override
    public void messageReceived(IoSession ioSession, Object obj) throws Exception {
//        System.out.println("message received: " + message)
        if (obj instanceof FatalResponse) {
            FatalResponse message = (FatalResponse) obj;
            for(Map.Entry<String, MikrotikResponseListener> entry : _listeners.entrySet()) {
                entry.getValue().error(new MikrotikConnectionException("fatal: " + message.getMessage()));
            }

        } else if (obj instanceof TrapResponse) {
            TrapResponse message = (TrapResponse) obj;
            if (message.getTag() != null && _listeners.get(message.getTag()) != null) {
                _listeners.get(message.getTag()).error(new MikrotikConnectionException("error: " + message.getMessage()));
                _listeners.remove(message.getTag());
            }
        } else if (obj instanceof ReResponse) {
            ReResponse message = (ReResponse) obj;
            if (message.getTag() != null && _listeners.get(message.getTag()) != null) {
                _listeners.get(message.getTag()).update(message.getAttrs());
            }
        } else if (obj instanceof DoneResponse) {
            DoneResponse message = (DoneResponse) obj;

            if (message.getTag() != null && _listeners.get(message.getTag()) != null) {
                if (message.getAttrs() != null && message.getAttrs().size() > 0)
                    _listeners.get(message.getTag()).update(message.getAttrs());
                _listeners.get(message.getTag()).complete();
                _listeners.remove(message.getTag());
            }
        }
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
//        System.out.println("session created")
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
//        System.out.println("session opened")
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
//        System.out.println("session closed")
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {
        System.out.println("session idle");

        CloseFuture closeFuture = ioSession.close(true);
        closeFuture.await(10000);

        ioSession.getService().dispose(true);
        dispose();
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
//        throwable.printStackTrace();
    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
//        System.out.println("message sent: " + o)
    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {
//        System.out.println("input closed")
    }
}
