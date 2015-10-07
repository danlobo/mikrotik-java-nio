package danlobo.mikrotik.codec;

import danlobo.mikrotik.Command;
import danlobo.mikrotik.exception.MikrotikProtocolException;
import danlobo.mikrotik.query.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.util.Map;
import java.util.Stack;

import static danlobo.mikrotik.query.CommandQueryOperation.*;
import static danlobo.mikrotik.query.CommandQueryOperation.HAS_PROPERTY;

public class CommandEncoder implements ProtocolEncoder{
    @Override
    public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
        Command command = (Command) o;
        IoBuffer ioBuffer = IoBuffer.allocate(64*1024, false);
        String word = command.getPath();
        encodeLen(word, ioBuffer);
        ioBuffer.put(word.getBytes());

        Map<String, String> mktAttrs = command.mktAttributes();
        if (mktAttrs != null && mktAttrs.size() > 0) {
            for(Map.Entry<String, String> entry : mktAttrs.entrySet()) {
                String line = "=" + entry.getKey() + "=" + entry.getValue();
                encodeLen(line, ioBuffer);
                ioBuffer.put(line.getBytes());
            }
        }

        if (command.getQuery() != null) {
            CommandQueryVisitor visitor = new CommandQueryVisitor() {
                public Stack<String> s = new Stack<String>();

                public Stack<String> getS() { return s; }

                @Override
                public void visit(CommandQueryAnd c) throws MikrotikProtocolException {
                    s.push("?#&");
                    c.getB().accept(this);
                    c.getA().accept(this);
                }

                @Override
                public void visit(CommandQueryOr c) throws MikrotikProtocolException {
                    s.push("?#|");
                    c.getB().accept(this);
                    c.getA().accept(this);
                }

                @Override
                public void visit(CommandQueryNot c) throws MikrotikProtocolException {
                    s.push("?#!");
                    c.getQuery().accept(this);
                }

                @Override
                public void visit(CommandQueryExpression c) throws MikrotikProtocolException {
                    switch (c.getOperation()) {
                        case HAS_PROPERTY:
                            s.push('?' + c.getOperand());
                            break;
                        case HAS_NO_PROPERTY:
                            s.push("?-" + c.getOperand());
                            break;
                        case EQUALS:
                            s.push("?" + c.getOperand() + "=" + c.getValue());
                            break;
                        case LESS_THAN:
                            s.push("?<" + c.getOperand() + "=" + c.getValue());
                            break;
                        case GREATER_THAN:
                            s.push("?>" + c.getOperand() + "=" + c.getValue());
                            break;
                        default:
                            throw new MikrotikProtocolException("Unknown query operation");
                    }
                }
            };

            command.getQuery().accept(visitor);
            while (!visitor.getS().empty()) {
                String item = (String) visitor.getS().pop();
                encodeLen(item, ioBuffer);
                ioBuffer.put(item.getBytes());
            }
        }

        if (command.getTag() != null) {
            String line = ".tag=" + command.getTag();
            encodeLen(line, ioBuffer);
            ioBuffer.put(line.getBytes());
        }
        //fim
        ioBuffer.put((byte) 0);
        ioBuffer.flip();
        protocolEncoderOutput.write(ioBuffer);

    }

    private void encodeLen(String word, IoBuffer ioBuffer) {
        int len = word.length();

        byte[] encodedLen;
        if (len < 0x80) {
            encodedLen = new byte[1];
            encodedLen[0] = (byte) len;
        } else if (len < 0x4000) {
            len |= 0x8000;
            encodedLen = new byte[2];
            encodedLen[0] = (byte) (len >> 8);
            encodedLen[1] = (byte) (len);
        } else if (len < 0x20000) {
            len |= 0xC00000;
            encodedLen = new byte[3];
            encodedLen[0] = (byte) (len >> 16);
            encodedLen[1] = (byte) (len >> 8);
            encodedLen[2] = (byte) (len);
        } else if (len < 0x10000000) {
            len |= 0xE0000000;
            encodedLen = new byte[4];
            encodedLen[0] = (byte) (len >> 24);
            encodedLen[1] = (byte) (len >> 16);
            encodedLen[2] = (byte) (len >> 8);
            encodedLen[3] = (byte) (len);
        } else {
            encodedLen = new byte[5];
            encodedLen[0] = (byte) (0xF0);
            encodedLen[1] = (byte) (len >> 24);
            encodedLen[2] = (byte) (len >> 16);
            encodedLen[3] = (byte) (len >> 8);
            encodedLen[4] = (byte) (len);
        }

        ioBuffer.put(encodedLen);
    }

    @Override
    public void dispose(IoSession ioSession) throws Exception {
        /*nothing*/
    }
}
