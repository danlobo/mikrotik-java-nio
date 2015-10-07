package danlobo.mikrotik.fakes;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.util.ArrayList;
import java.util.List;

public class FakeProtocolDecoderOutput implements ProtocolDecoderOutput {
    public List<Object> objs = new ArrayList<Object>();

    @Override
    public void write(Object o) {
        objs.add(o);
    }

    @Override
    public void flush(IoFilter.NextFilter nextFilter, IoSession ioSession) {

    }
}
