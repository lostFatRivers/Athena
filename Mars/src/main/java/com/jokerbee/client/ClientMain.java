package com.jokerbee.client;

import com.jokerbee.protocol.Game.*;
import com.jokerbee.protocol.MessageCode;
import com.jokerbee.protocol.System.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mars Client;
 *
 * @author: Joker
 * @date: Created in 2020/10/30 23:06
 * @version: 1.0
 */
public class ClientMain {
    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    private static final Logger logger = LoggerFactory.getLogger("Client");

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Future.<WebSocket>future(pro -> {
            vertx.createHttpClient().webSocket(40021, "10.0.0.159", "/websocket", pro);
        }).onSuccess(ws -> {
            logger.info("connect server ok");
            ws.closeHandler(v -> vertx.close());
            sendLoginMessage(ws);
        });
    }

    public static void sendLoginMessage(WebSocket ws) {
        PlayerLogin.Builder builder = PlayerLogin.newBuilder().setAccount("joker").setPassword("123321123321");
        ProtocolWrapper.Builder loginBuilder = ProtocolWrapper.newBuilder()
                .setCode(MessageCode.Code.CS_ACCOUNT_LOGIN_VALUE)
                .setBody(builder.build().toByteString());
        ws.write(Buffer.buffer(loginBuilder.build().toByteArray()));
    }
}
