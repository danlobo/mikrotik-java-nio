package danlobo.mikrotik;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class DefaultMikrotikResponseListener implements MikrotikResponseListener {
    private LinkedBlockingQueue<Map<String, String>> _data;
    private boolean _completed = false;
    private boolean notifyOnUpdate;
    private boolean notifyOnComplete;
    private Exception _exception;

    public DefaultMikrotikResponseListener(boolean notifyOnUpdate, boolean notifyOnComplete) {
        this.notifyOnUpdate = notifyOnUpdate;
        this.notifyOnComplete = notifyOnComplete;
        _data = new LinkedBlockingQueue<Map<String, String>>(100000);
    }

    @Override
    public int inQueue() {
        return _data.size();
    }

    @Override
    public void update(Map<String, String> item) {
        _data.offer(item);
        if (notifyOnUpdate)
            synchronized (this) {
                notify();
            }
    }

    @Override
    public void complete() {
        _completed = true;
        if (notifyOnComplete)
            synchronized (this) {
                notify();
            }
    }

    public List<Map<String, String>> getData() {
        List<Map<String, String>> d = new ArrayList<Map<String, String>>();
        if (_data.isEmpty())
            return d;
        while (!_data.isEmpty())
            try {
                d.add(_data.poll(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        return d;
    }

    public boolean isCompleted() {
        return _completed;
    }

    @Override
    public Exception getException() { return _exception; }

    @Override
    public void error(Exception e) {
        _exception = e;
        complete();
    }
}
