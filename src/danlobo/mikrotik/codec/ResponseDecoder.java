package danlobo.mikrotik.codec;

import danlobo.mikrotik.exception.MikrotikProtocolException;
import danlobo.mikrotik.response.DoneResponse;
import danlobo.mikrotik.response.FatalResponse;
import danlobo.mikrotik.response.ReResponse;
import danlobo.mikrotik.response.TrapResponse;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResponseDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = CommandDecoder.class.getName() + ".STATE";

    private static class DecoderState {
        long len;
        int lenLen;
        boolean gotLen;
        List<String> data;

        public DecoderState() {
            len = 0;
            lenLen = 1;
            gotLen = false;
            data = new ArrayList<String>();
        }
    }

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        DecoderState decoderState = (DecoderState) ioSession.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            ioSession.setAttribute(DECODER_STATE_KEY, decoderState);
        }

        while (ioBuffer.remaining() > 0) {
            if (!decoderState.gotLen) {
                if (ioBuffer.remaining() < decoderState.lenLen) return false;

                if (decoderState.len == 0)
                    decoderState.len = (int) ioBuffer.getUnsigned();

                if (decoderState.len > 0) {
                    if ((decoderState.len & 0x80) == 0) {
                    } else if ((decoderState.len & 0xC0) == 0x80) {
                        decoderState.lenLen = 1;
                        if (ioBuffer.remaining() < decoderState.lenLen) return false;

                        decoderState.len = decoderState.len & ~0xC0;
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                    } else if ((decoderState.len & 0xE0) == 0xC0) {
                        decoderState.lenLen = 2;
                        if (ioBuffer.remaining() < decoderState.lenLen) return false;

                        decoderState.len = decoderState.len & ~0xE0;
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                    } else if ((decoderState.len & 0xF0) == 0xE0) {
                        decoderState.lenLen = 3;
                        if (ioBuffer.remaining() < decoderState.lenLen) return false;

                        decoderState.len = decoderState.len & ~0xF0;
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                    } else if ((decoderState.len & 0xF8) == 0xF0) {
                        decoderState.lenLen = 5;
                        if (ioBuffer.remaining() < decoderState.lenLen) return false;

                        decoderState.len = (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                        decoderState.len = (decoderState.len << 8) | (int) ioBuffer.getUnsigned();
                    }
                }
                decoderState.gotLen = true;
            }

            if (decoderState.gotLen) {
                if (decoderState.len == 0) {
                    String tag = null;
                    String s = decoderState.data.get(0);
                    if (s.equals("!re")) {
                        Map<String, String> currentLine = new HashMap<String, String>();
                        for (int i = 1; i < decoderState.data.size(); i++) {
                            String line = decoderState.data.get(i);
                            if (line.equals("!done")) {
                                break;
                            }
                            if (line.equals("!re")) {
                                protocolDecoderOutput.write(new ReResponse(tag, currentLine));
                                currentLine = new HashMap<String, String>();
                                continue;
                            }
                            if (line.startsWith(".tag=")) {
                                tag = line.substring(5);
                            } else {
                                String[] entry = getAttribute(line);
                                currentLine.put(entry[1], entry[2]);
                            }
                        }
                        if (currentLine.size() > 0) {
                            protocolDecoderOutput.write(new ReResponse(tag, currentLine));
                        }

                    } else if (s.equals("!done")) {
                        for (String line : decoderState.data) {
                            if (line.startsWith(".tag=")) {
                                tag = line.substring(5);
                            }
                        }

                        Map<String, String> attrs = decoderState.data.size() > 1 ? getAttributes(decoderState.data.subList(1, decoderState.data.size())) : new HashMap<String, String>();
                        protocolDecoderOutput.write(new DoneResponse(tag, attrs));

                    } else if (s.equals("!trap")) {
                        for (String line : decoderState.data) {
                            if (line.startsWith(".tag=")) {
                                tag = line.substring(5);
                            }
                        }

                        Map<String, String> attrs = decoderState.data.size() > 1 ? getAttributes(decoderState.data.subList(1, decoderState.data.size())) :  new HashMap<String, String>();
                        protocolDecoderOutput.write(new TrapResponse(tag, attrs));

                    } else if (s.equals("!fatal")) {
                        protocolDecoderOutput.write(new FatalResponse(decoderState.data.get(1)));

                    } else {
                        throw new MikrotikProtocolException("Unrecognized reply: " + decoderState.data.get(0));
                    }

                    ioSession.removeAttribute(DECODER_STATE_KEY);

                    return true;
                } else {
                    if (decoderState.len < 0)
                        throw new MikrotikProtocolException("Negative len value: " + decoderState.len);

                    if (ioBuffer.remaining() < decoderState.len) return false;

                    byte[] wordBytes = new byte[(int) decoderState.len];
                    ioBuffer.get(wordBytes);
                    decoderState.data.add(new String(wordBytes));

                    decoderState.len = 0;
                    decoderState.lenLen = 1;
                    decoderState.gotLen = false;
                }
            }
        }
        return false;
    }

    private static Map<String, String> getAttributes(List<String> d) throws MikrotikProtocolException {
        Map<String, String> attributes = new HashMap<String, String>();
        if (d != null && d.size() > 0)
            for(String attr : d) {
                String[] entry = getAttribute(attr);
                if (entry == null)
                    continue;
                attributes.put(entry[1], entry[2]);
            }
        return attributes;
    }

    private static String[] getAttribute(String d) throws MikrotikProtocolException {
        if (d.startsWith("."))
            return null;
        if (!d.startsWith("="))
            throw new MikrotikProtocolException("Malformed attribute line: " + d);

        String[] attr = d.split("=", 3);
        if (attr.length != 3)
            throw new MikrotikProtocolException("Malformed attribute line: " + d);
        return attr;
    }
}
