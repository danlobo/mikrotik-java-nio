package danlobo.mikrotik;

import danlobo.mikrotik.fakes.FakeIoSession;
import danlobo.mikrotik.response.TrapResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MikrotikKeepAliveMessageFactoryTest {

    @Test
    public void testPing() throws Exception {
        FakeIoSession s = new FakeIoSession();

        MikrotikKeepAliveMessageFactory m = new MikrotikKeepAliveMessageFactory();

        Object req = m.getRequest(s);

        Assert.assertEquals(Command.class, req.getClass());

        Assert.assertTrue( m.isRequest(s, req));

        Assert.assertFalse(m.isRequest(s, new Command("/bogus")));

        String id = ((Command) req).getPath().substring(1);

        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("message", "no such command or directory (" + id + ")");

        TrapResponse resp = new TrapResponse("", attrs);

        Assert.assertEquals(resp, m.getResponse(s, resp));

        Assert.assertTrue(m.isResponse(s, resp));
        Assert.assertFalse(m.isResponse(s, new Command("/bogus")));

        attrs.clear();
        attrs.put("message", "bogus");

        Assert.assertFalse(m.isResponse(s, new TrapResponse("", attrs)));
    }
}