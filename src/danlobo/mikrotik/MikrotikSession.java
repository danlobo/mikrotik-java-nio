package danlobo.mikrotik;

import danlobo.mikrotik.exception.MikrotikConnectionException;
import danlobo.mikrotik.exception.MikrotikProtocolException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MikrotikSession implements Closeable {
    private NioSocketConnector connector;
    private IoSession session;
    private MikrotikConnectionHandler handler;
    private int tag;
    private MikrotikSessionConfig sessionConfig;
    private ConcurrentHashMap<String, LinkedBlockingQueue<Map<String, String>>> data;

    protected MikrotikSession(NioSocketConnector connector, IoSession session, MikrotikSessionConfig config) {
        this.session = session;
        this.connector = connector;
        this.handler = (MikrotikConnectionHandler) connector.getHandler();
        this.sessionConfig = config;
        this.data = new ConcurrentHashMap<String, LinkedBlockingQueue<Map<String, String>>>();
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    public InetAddress getRemoteAddress() {
        return ((InetSocketAddress) this.session.getRemoteAddress()).getAddress();
    }

    public void login(String username, String password) throws MikrotikConnectionException, MikrotikProtocolException {
        List<Map<String, String>> response = null;
        for(int i = 0; i < 3; i++) {
            response = _execute(new Command("/login"));
            if (response != null && response.size() > 0) {
                break;
            } else {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (response == null || response.size() == 0) {
            throw new MikrotikConnectionException("Could not login: empty response");
        }


        String ret = response.get(0).get("ret");
        if (ret == null)
            throw new MikrotikConnectionException("Could not login: hash not informed");

        String challenge = md5(password, ret);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("name", username);
        attrs.put("response", "00" + challenge);

        _execute(new Command("/login", attrs));
    }

    static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < ba.length; i++)
            str.append(String.format("%02x", ba[i]));
        return str.toString();
    }

    static byte[] fromHexString(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hex.length(); i+=2) {
            baos.write((char) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        return baos.toByteArray();
    }

    public static String md5(String s, String challenge) throws MikrotikProtocolException {
        MessageDigest algorithm = null;
        try {
            algorithm = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new MikrotikProtocolException("MD5 digest algorithm not found");
        }
        algorithm.update((byte) 0x00);
        algorithm.update(s.getBytes());
        algorithm.update(fromHexString(challenge));
        return toHexString(algorithm.digest());
    }

    public void cancel(String tag) throws Exception {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("tag", tag);
        execute(new Command("/cancel", attrs));
    }

    public List<Map<String, String>> execute(Command cmd) throws Exception {
        String tag = _executeAsync(cmd);
        MikrotikResponseListener listener = getListenerByTag(tag);
        if (listener == null)
            throw new MikrotikProtocolException("invalid tag");

        try
        {
            synchronized(listener) {
                if (listener.isCompleted())
                    return listener.getData();

                listener.wait(sessionConfig.responseTimeout);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (listener.getException() != null)
            throw listener.getException();

        if (!listener.isCompleted())
                throw new MikrotikConnectionException("timeout");

        return listener.getData();
    }

    public String executeAsync(Command cmd) throws MikrotikConnectionException {
        DefaultMikrotikResponseListener dml = new DefaultMikrotikResponseListener(false, true);
        return _executeAsync(cmd, dml);
    }

    public MikrotikResponseListener getListenerByTag(String tag) {
        return this.handler.getListeners().get(tag);
    }

    public String executeAsync(Command cmd, MikrotikResponseListener listener) throws MikrotikConnectionException {
        return _executeAsync(cmd, listener);
    }

    private List<Map<String, String>> _execute(Command cmd) throws MikrotikConnectionException {
        if (!session.isConnected())
            throw new MikrotikConnectionException("not connected");

        String t = String.valueOf(++tag);
        cmd.tag = t;
        MikrotikResponseListener listener = this.handler.informInterest(cmd.tag);
        try {
            WriteFuture futureWrite = session.write(cmd);
            futureWrite.await(sessionConfig.responseTimeout);

            if (!futureWrite.isDone())
                throw new MikrotikConnectionException("request timeout");
            if (!futureWrite.isWritten())
                throw new MikrotikConnectionException("could not write to buffer");

            synchronized (listener) {
                if (listener.isCompleted())
                    return listener.getData();

                listener.wait(sessionConfig.responseTimeout);
            }

            if (listener.getException() != null)
                throw new MikrotikConnectionException(listener.getException().getMessage(), listener.getException());
            if (!listener.isCompleted())
                throw new MikrotikConnectionException("response timeout");
        } catch (InterruptedException e) {
            throw new MikrotikConnectionException("timeout", e);
        } finally {
            this.handler.removeInterest(t);
        }
        return listener.getData();
    }

    private String _executeAsync(Command cmd) throws MikrotikConnectionException {
        MikrotikResponseListener dml = new DefaultMikrotikResponseListener(false, true);
        return _executeAsync(cmd, dml);
    }

    private String _executeAsync(Command cmd, MikrotikResponseListener listener) throws MikrotikConnectionException {
        if (!session.isConnected())
            throw new MikrotikConnectionException("not connected");

        String t = String.valueOf(++tag);
        cmd.tag = t;

        this.handler.informInterest(t, listener);
        session.write(cmd);
        return t;
    }

    @Override
    public void close() {
        CloseFuture closeFuture = this.session.close(false);
        try {
            closeFuture.await(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.session.getService().dispose(false);
        this.handler.dispose();
    }

    void disposeListener(MikrotikResponseListener listener) {
        this.handler.removeInterest(listener);
    }
}
