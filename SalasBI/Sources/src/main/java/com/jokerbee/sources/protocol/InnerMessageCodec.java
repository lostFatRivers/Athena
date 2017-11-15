package com.jokerbee.sources.protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class InnerMessageCodec implements MessageCodec<InnerMessage, Buffer> {

    @Override
    public void encodeToWire(Buffer buffer, InnerMessage innerMessage) {

    }

    @Override
    public Buffer decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public Buffer transform(InnerMessage innerMessage) {
        return null;
    }

    @Override
    public String name() {
        return "JokerBeeMessageCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
