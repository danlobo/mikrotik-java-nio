package danlobo.mikrotik.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CommandCodecFactory implements ProtocolCodecFactory {
    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;

    public CommandCodecFactory(boolean client) {
        if (client) {
            encoder = new CommandEncoder();
            decoder = new ResponseDecoder();
        } else {
            throw new NotImplementedException();
        }
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
