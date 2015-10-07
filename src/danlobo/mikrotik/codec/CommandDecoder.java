package danlobo.mikrotik.codec;

import danlobo.mikrotik.Command;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class CommandDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = CommandDecoder.class.getName() + ".STATE";

    private static class DecoderState {
        int len;
        int lenLen;
        boolean gotLen;

        public DecoderState() {
            len = 0;
            lenLen = 4;
            gotLen = false;
        }
    }

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        DecoderState decoderState = (DecoderState) ioSession.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            ioSession.setAttribute(DECODER_STATE_KEY, decoderState);
        }

        if (!decoderState.gotLen) {
            if (ioBuffer.remaining() < decoderState.lenLen) return false;

            if (decoderState.len == 0)
                decoderState.len = ioBuffer.getInt();

            if (decoderState.len > 0) {
                if ((decoderState.len & 0x80) == 0) {
                } else if ((decoderState.len & 0xC0) == 0x80) {
                    decoderState.lenLen = 4;
                    if (ioBuffer.remaining() < decoderState.lenLen) return false;

                    decoderState.len = decoderState.len & ~0xC0;
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                } else if ((decoderState.len & 0xE0) == 0xC0) {
                    decoderState.lenLen = 8;
                    if (ioBuffer.remaining() < decoderState.lenLen) return false;

                    decoderState.len = decoderState.len & ~0xE0;
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                } else if ((decoderState.len & 0xF0) == 0xE0) {
                    decoderState.lenLen = 12;
                    if (ioBuffer.remaining() < decoderState.lenLen) return false;

                    decoderState.len = decoderState.len & ~0xF0;
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                } else if ((decoderState.len & 0xF8) == 0xF0) {
                    decoderState.lenLen = 20;
                    if (ioBuffer.remaining() < decoderState.lenLen) return false;

                    decoderState.len = ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                    decoderState.len = (decoderState.len << 8) | ioBuffer.getInt();
                }
            }
            decoderState.gotLen = true;
        } else {
            if (ioBuffer.remaining() < decoderState.len) return false;
            byte[] packet = new byte[decoderState.len];
            ioBuffer.get(packet);

            Command cmd = new Command(packet);

            protocolDecoderOutput.write(cmd);

            ioSession.removeAttribute(DECODER_STATE_KEY);

            return true;
        }
        return false;
    }
}
