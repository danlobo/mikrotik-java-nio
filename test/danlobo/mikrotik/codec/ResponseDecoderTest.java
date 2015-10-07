package danlobo.mikrotik.codec;

import danlobo.mikrotik.fakes.FakeIoSession;
import danlobo.mikrotik.fakes.FakeProtocolDecoderOutput;
import danlobo.mikrotik.response.DoneResponse;
import danlobo.mikrotik.response.FatalResponse;
import danlobo.mikrotik.response.ReResponse;
import danlobo.mikrotik.response.TrapResponse;
import org.apache.mina.core.buffer.IoBuffer;
import org.junit.Assert;
import org.junit.Test;

public class ResponseDecoderTest {

    @Test
    public void testDoDecodeMessageInParts() throws Exception {
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] { 5 });
        ioBuffer.flip();

        Assert.assertFalse(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(0, p.objs.size());

        ioBuffer.compact();
        ioBuffer.put(new byte[] { '!', 'd' });
        ioBuffer.flip();

        Assert.assertFalse(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(0, p.objs.size());

        ioBuffer.compact();
        ioBuffer.put(new byte[] { 'o', 'n', 'e' });
        ioBuffer.flip();

        Assert.assertFalse(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(0, p.objs.size());

        ioBuffer.compact();
        ioBuffer.put(new byte[] { 0 });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(1, p.objs.size());
        Assert.assertEquals(DoneResponse.class, p.objs.get(0).getClass());
    }

    @Test
    public void testReceiveReResponse() throws Exception {
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] {
                3, '!', 'r', 'e',
                10,'=', '.', 'i', 'd', '=', '*', '5', '9', '0', '2',
                12,'=', 'd', 'i', 's', 'a', 'b', 'l', 'e', 'd', '=', 'n', 'o',
                20,'=', 'n', 'a', 'm', 'e', '=', 'a', 'd', 'v', 'a', 'n', 'c', 'e', 'd', '-', 't', 'o', 'o', 'l', 's',
                17,'=', 'v', 'e', 'r', 's', 'i', 'o', 'n', '=', '3', '.', '0', 'b', 'e', 't', 'a', '2',
                11,'=', 's', 'c', 'h', 'e', 'd', 'u', 'l', 'e', 'd', '=',
                6 ,'.', 't', 'a', 'g', '=', '5',
                5, '!', 'd', 'o', 'n', 'e',
                0
        });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(1, p.objs.size());
        Assert.assertEquals(ReResponse.class, p.objs.get(0).getClass());

        ReResponse re = (ReResponse) p.objs.get(0);

        Assert.assertEquals("5", re.getTag());
        Assert.assertEquals("*5902", re.getAttrs().get(".id"));
        Assert.assertEquals("", re.getAttrs().get("scheduled"));
        Assert.assertEquals("advanced-tools", re.getAttrs().get("name"));
        Assert.assertEquals("3.0beta2", re.getAttrs().get("version"));
    }

    @Test
    public void testReceiveMultiReResponse() throws Exception {
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] {
                3, '!', 'r', 'e',
                7 ,'=', '.', 'i', 'd', '=', '*', '1',
                12,'=', 'd', 'i', 's', 'a', 'b', 'l', 'e', 'd', '=', 'n', 'o',
                6 ,'.', 't', 'a', 'g', '=', '5',
                3, '!', 'r', 'e',
                7 ,'=', '.', 'i', 'd', '=', '*', '2',
                12,'=', 'd', 'i', 's', 'a', 'b', 'l', 'e', 'd', '=', 'n', 'o',
                6 ,'.', 't', 'a', 'g', '=', '5',
                3, '!', 'r', 'e',
                7 ,'=', '.', 'i', 'd', '=', '*', '3',
                13,'=', 'd', 'i', 's', 'a', 'b', 'l', 'e', 'd', '=', 'y', 'e', 's',
                6 ,'.', 't', 'a', 'g', '=', '5',
                5, '!', 'd', 'o', 'n', 'e',
                0
        });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(3, p.objs.size());

        Assert.assertEquals(ReResponse.class, p.objs.get(0).getClass());
        Assert.assertEquals(ReResponse.class, p.objs.get(1).getClass());
        Assert.assertEquals(ReResponse.class, p.objs.get(2).getClass());

        ReResponse re1 = (ReResponse) p.objs.get(0);
        ReResponse re2 = (ReResponse) p.objs.get(1);
        ReResponse re3 = (ReResponse) p.objs.get(2);

        Assert.assertEquals("5", re1.getTag());
        Assert.assertEquals("5", re2.getTag());
        Assert.assertEquals("5", re3.getTag());
        Assert.assertEquals("*1", re1.getAttrs().get(".id"));
        Assert.assertEquals("*2", re2.getAttrs().get(".id"));
        Assert.assertEquals("*3", re3.getAttrs().get(".id"));
        Assert.assertEquals("no", re1.getAttrs().get("disabled"));
        Assert.assertEquals("no", re2.getAttrs().get("disabled"));
        Assert.assertEquals("yes", re3.getAttrs().get("disabled"));
    }

    @Test
    public void testReceiveDoneResponse() throws Exception {
        //In some cases the protocol returns a !done message with attributes. Figure.
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] {
                5, '!', 'd', 'o', 'n', 'e',
                6 ,'.', 't', 'a', 'g', '=', '5',
                12,'=', 'd', 'i', 's', 'a', 'b', 'l', 'e', 'd', '=', 'n', 'o',
                0
        });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(1, p.objs.size());
        Assert.assertEquals(DoneResponse.class, p.objs.get(0).getClass());

        DoneResponse re = (DoneResponse) p.objs.get(0);

        Assert.assertEquals("5", re.getTag());
        Assert.assertEquals("no", re.getAttrs().get("disabled"));
    }

    @Test
    public void testReceiveTrapResponse() throws Exception {
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] {
                5, '!', 't', 'r', 'a', 'p',
                6 ,'.', 't', 'a', 'g', '=', '5',
                11,'=', 'c', 'a', 't', 'e', 'g', 'o', 'r', 'y', '=', '2',
                20,'=', 'm', 'e', 's', 's', 'a', 'g', 'e', '=', 'i', 'n', 't', 'e', 'r', 'r', 'u', 'p', 't', 'e', 'd',
                0
        });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(1, p.objs.size());
        Assert.assertEquals(TrapResponse.class, p.objs.get(0).getClass());

        TrapResponse re = (TrapResponse) p.objs.get(0);

        Assert.assertEquals("5", re.getTag());
        Assert.assertEquals("interrupted", re.getMessage());
        Assert.assertEquals("2", re.getAttrs().get("category"));
    }


    @Test
    public void testReceiveFatalResponse() throws Exception {
        ResponseDecoder dec = new ResponseDecoder();
        FakeIoSession s = new FakeIoSession();
        FakeProtocolDecoderOutput p = new FakeProtocolDecoderOutput();

        IoBuffer ioBuffer = IoBuffer.allocate(256);
        ioBuffer.put(new byte[] {
                6, '!', 'f', 'a', 't', 'a', 'l',
                6 ,'c', 'l', 'o', 's', 'e', 'd',
                0
        });
        ioBuffer.flip();

        Assert.assertTrue(dec.doDecode(s, ioBuffer, p));
        Assert.assertEquals(1, p.objs.size());
        Assert.assertEquals(FatalResponse.class, p.objs.get(0).getClass());

        FatalResponse re = (FatalResponse) p.objs.get(0);

        Assert.assertEquals("closed", re.getMessage());
    }
}