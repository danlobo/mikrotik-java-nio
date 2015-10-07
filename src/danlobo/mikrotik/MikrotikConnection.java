package danlobo.mikrotik;

import danlobo.mikrotik.codec.CommandCodecFactory;
import danlobo.mikrotik.exception.MikrotikConnectionException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;

public class MikrotikConnection implements Closeable {
    private NioSocketConnector _connector;
    private MikrotikSessionConfig _sessionConfig;

    public MikrotikConnection() {
        this._sessionConfig = new MikrotikSessionConfig();
    }

    public MikrotikSessionConfig getSessionConfig(){
        return _sessionConfig;
    }

    public MikrotikSession connect(String host, int port) throws MikrotikConnectionException {
        return connect(new InetSocketAddress(host, port));
    }

    public MikrotikSession connect(SocketAddress sa) throws MikrotikConnectionException {
        NioSocketConnector connector = setupConnector();
        setupFilters(connector);
        IoSession session = establishConnection(connector, sa);
        return createMktSession(session, connector);
    }

    public MikrotikSession connect(String host, int port, Certificate cert, PrivateKey pk) throws MikrotikConnectionException {
        return connect(new InetSocketAddress(host, port), cert, pk);
    }

    public MikrotikSession connect(SocketAddress sa, Certificate cert, PrivateKey pk) throws MikrotikConnectionException {
        NioSocketConnector connector = setupConnector();
        try {
            setupFilters(connector, cert, pk);
        } catch (Exception e) {
            throw new MikrotikConnectionException("Error setting up filters: ", e);
        }

        IoSession session = establishConnection(connector, sa);
        return createMktSession(session, connector);
    }

    private MikrotikSession createMktSession(IoSession session, NioSocketConnector connector) {
        MikrotikSession mktSession = null;
        if (session != null) {
            mktSession = new MikrotikSession(connector, session, _sessionConfig);
        }
        return mktSession;
    }

    private static IoSession establishConnection(NioSocketConnector connector, SocketAddress sa) throws MikrotikConnectionException {
        ConnectFuture future = connector.connect(sa);
        try {
            future.await(10000);
        } catch (InterruptedException e) {
            throw new MikrotikConnectionException("timeout", e);
        }
        return future.getSession();
    }

    private static void setupFilters(NioSocketConnector connector, Certificate cert, PrivateKey pk) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        char[] password = "test".toCharArray();

        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(null, password);

        KeyStore kks = KeyStore.getInstance("JKS");
        kks.load(null, password);

        if (cert != null)
            tks.setCertificateEntry("cert", cert);

        if (pk != null) {
            Certificate[] certs = new Certificate[ 1 ];
            certs[0] = cert;
            kks.setKeyEntry("myKey", pk, password, certs);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        tmf.init(tks);
        kmf.init(kks, password);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SslFilter filter = new SslFilter(ctx, true);
        filter.setUseClientMode(true);

        connector.getFilterChain().addLast("ssl", filter);
        setupFilters(connector);
    }

    private static void setupFilters(NioSocketConnector connector) {
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CommandCodecFactory(true)));
        connector.getFilterChain().addLast("keepalive", new KeepAliveFilter(new MikrotikKeepAliveMessageFactory()));
//        connector.getFilterChain().addLast("logger", new LoggingFilter());

        connector.setHandler(new MikrotikConnectionHandler());
    }

    private NioSocketConnector setupConnector() {
        NioSocketConnector connector = new NioSocketConnector();
//        connector.setConnectTimeoutMillis(10000);
        connector.getSessionConfig().setTcpNoDelay(true);
        connector.getSessionConfig().setReuseAddress(true);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10000);
        connector.getSessionConfig().setReceiveBufferSize(64 * 1024);
        connector.getSessionConfig().setSendBufferSize(64 * 1024);
        connector.getSessionConfig().setReadBufferSize(64 * 1024);
//        connector.getSessionConfig().setWriteTimeout(10000)

        _connector = connector;

        return connector;
    }

    @Override
    public void close() {
        for(Map.Entry<java.lang.Long,org.apache.mina.core.session.IoSession> entry : _connector.getManagedSessions().entrySet()) {
            IoSession session = entry.getValue();
            if (session.isConnected()) {
                CloseFuture future = session.close(true);
                if (future != null)
                    try {
                        future.await(3000);
                    } catch (InterruptedException e) {
//                      e.printStackTrace()
                    }
                session.getService().dispose(false);
            }
        }

        if (_connector != null)
        _connector.dispose(false);
    }
}
