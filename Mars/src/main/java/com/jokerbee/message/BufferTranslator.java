package com.jokerbee.message;

import com.jokerbee.protocol.System.ProtocolWrapper;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferTranslator {
    private static Logger logger = LoggerFactory.getLogger("Message");

    public static ProtocolWrapper toWrapper(Buffer buffer) {
        try {
            return ProtocolWrapper.parseFrom(buffer.getBytes());
        } catch (Exception e) {
            logger.error("Protocol parse error.", e);
        }
        return null;
    }

}
