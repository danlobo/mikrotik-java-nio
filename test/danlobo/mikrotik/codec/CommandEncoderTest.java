package danlobo.mikrotik.codec;

import danlobo.mikrotik.Command;
import danlobo.mikrotik.fakes.FakeIoSession;
import danlobo.mikrotik.fakes.FakeProtocolEncoderOutput;
import danlobo.mikrotik.query.CommandQueryAnd;
import danlobo.mikrotik.query.CommandQueryExpression;
import danlobo.mikrotik.query.CommandQueryOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CommandEncoderTest {

    @Test
    public void testEncodeSimple() throws Exception {
        CommandEncoder enc = new CommandEncoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolEncoderOutput p = new FakeProtocolEncoderOutput();
        Command c = new Command("/test");

        enc.encode(s, c, p);

        Assert.assertArrayEquals(new byte[]{5, '/', 't', 'e', 's', 't', 0}, p.data);
    }

    @Test
    public void testEncodeWithArgs() throws Exception {
        CommandEncoder enc = new CommandEncoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolEncoderOutput p = new FakeProtocolEncoderOutput();
        Map<String, String> args = new HashMap<String, String>();
        args.put("mock", "123");
        Command c = new Command("/test", args);

        enc.encode(s, c, p);

        Assert.assertArrayEquals(new byte[]{
                5, '/', 't', 'e', 's', 't',
                9, '=', 'm', 'o', 'c', 'k', '=', '1', '2', '3',
                0
        }, p.data);
    }

    @Test
    public void testEncodeWithMultilineArgs() throws Exception {
        CommandEncoder enc = new CommandEncoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolEncoderOutput p = new FakeProtocolEncoderOutput();
        Map<String, String> args = new HashMap<String, String>();
        args.put("mock", "123\r\n456\r\n789");
        args.put("mock2", "test");
        Command c = new Command("/test", args);

        enc.encode(s, c, p);

        Assert.assertArrayEquals(new byte[]{
                5, '/', 't', 'e', 's', 't',
                19, '=', 'm', 'o', 'c', 'k', '=', '1', '2', '3', '\r', '\n', '4', '5', '6', '\r', '\n', '7', '8', '9',
                11, '=', 'm', 'o', 'c', 'k', '2', '=', 't', 'e', 's', 't',
                0
        }, p.data);
    }

    @Test
    public void testEncodeWithQuery() throws Exception {
        CommandEncoder enc = new CommandEncoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolEncoderOutput p = new FakeProtocolEncoderOutput();

        Command c = new Command("/test", new CommandQueryAnd(
                new CommandQueryExpression("mock", CommandQueryOperation.GREATER_THAN, "1"),
                new CommandQueryExpression("hello", CommandQueryOperation.EQUALS, "world")
        )
        );

        enc.encode(s, c, p);

        Assert.assertArrayEquals(new byte[]{
                5, '/', 't', 'e', 's', 't',
                8, '?', '>', 'm', 'o', 'c', 'k', '=', '1',
                12,'?', 'h', 'e', 'l', 'l', 'o', '=', 'w', 'o', 'r', 'l', 'd',
                3, '?', '#', '&',
                0
        }, p.data);
    }

    @Test
    public void testEncodeWithArgsAndQuery() throws Exception {
        CommandEncoder enc = new CommandEncoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolEncoderOutput p = new FakeProtocolEncoderOutput();
        Map<String, String> args = new HashMap<String, String>();
        args.put("mock", "123");
        Command c = new Command("/test", args, new CommandQueryExpression("hello", CommandQueryOperation.EQUALS, "world"));

        enc.encode(s, c, p);

        Assert.assertArrayEquals(new byte[]{
                5, '/', 't', 'e', 's', 't',
                9, '=', 'm', 'o', 'c', 'k', '=', '1', '2', '3',
                12,'?', 'h', 'e', 'l', 'l', 'o', '=', 'w', 'o', 'r', 'l', 'd',
                0
        }, p.data);
    }

    //TODO add tests for length encoding
}