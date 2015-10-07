package danlobo.mikrotik.fakes;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class FakeProtocolEncoderOutput implements ProtocolEncoderOutput {
    public byte[] data;

    @Override
    public void write(Object o) {
        IoBuffer buff = (IoBuffer) o;
        byte[] b = new byte[buff.remaining()];
        buff.get(b);
        data = b;
    }

    @Override
    public void mergeAll() {

    }

    @Override
    public WriteFuture flush() {
        return null;
    }
}
