package danlobo.mikrotik;

import danlobo.mikrotik.exception.MikrotikConnectionException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MikrotikConnectionTest {
    String validMktIp = "your ip";
    String validMktUser = "your user";
    String validMktPass = "your pass";


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @Ignore
    public void testConnectionFailure() throws Exception {
        MikrotikConnection conn = new MikrotikConnection();
        thrown.expect(MikrotikConnectionException.class);
        try {
            //Well, unless you have am open port or a mikrotik emulator running...
            conn.connect("127.0.0.1", 8728);
        } finally {
            conn.close();
        }
    }

    @Test
    @Ignore
    public void testLoginFailure() throws Exception {
        MikrotikConnection conn = new MikrotikConnection();
        try {
            MikrotikSession session = conn.connect(validMktIp, 8728);
            try {
                assertTrue(session.isConnected());

                thrown.expect(MikrotikConnectionException.class);
                session.login("", "");

            } finally {
                session.close();
            }
        } finally {
            conn.close();
        }

        Thread.sleep(1000);
    }

    @Test
    @Ignore
    public void testConnection() throws Exception {
        MikrotikConnection conn = new MikrotikConnection();
        try {
            MikrotikSession session = conn.connect(validMktIp, 8728);
            try {
                assertTrue(session.isConnected());
                session.login(validMktUser, validMktPass);

                List<Map<String, String>> res = session.execute(new Command("/system/routerboard/print"));

                assertNotNull(res);
                assertTrue(res.size() > 0);
            } finally {
                session.close();
            }
        } finally {
            conn.close();
        }

        Thread.sleep(1000);
    }
}