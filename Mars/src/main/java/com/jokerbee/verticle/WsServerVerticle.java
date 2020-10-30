package com.jokerbee.verticle;

import com.jokerbee.consts.ClusterDataKey;
import com.jokerbee.consts.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class WsServerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger("WsServer");

    private static final AtomicInteger CONNECT_COUNTER = new AtomicInteger(0);

    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> promise) {
        try {
            createWebSocketService();
            addShutdownHook();
            promise.complete();
        } catch (Exception e) {
            promise.fail(e);
        }
    }

    private void createWebSocketService() {
        HttpServerOptions options = new HttpServerOptions();
        if (config().getBoolean("SSL")) {
            options.setUseAlpn(true)
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath(config().getString("cerPath")).setPassword(config().getString("cerPassword")));
        }
        httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(this::httpRequestHandler)
                .webSocketHandler(this::websocketHandler)
                .connectionHandler(this::httpConnection)
                .listen(config().getInteger("port"));

        logger.info("WsServerVerticle start, port:{}", config().getInteger("port"));
    }

    private void httpRequestHandler(HttpServerRequest request) {
        logger.info("Web Socket Server Get HttpRequest: {}", request.path());
        request.response().setStatusCode(404).end("<h1>Resource Not Found.</h1>");
    }

    private void websocketHandler(ServerWebSocket webSocket) {
        logger.info("websocket connect: {}", webSocket.path());
        if (!webSocket.path().equals(Constants.WEB_SOCKET_PATH)) {
            webSocket.reject();
            logger.info("websocket connect reject");
            return;
        }
        String binaryId = webSocket.binaryHandlerID();
        logger.info("websocket connect open: {}, connectCount:{}", binaryId, CONNECT_COUNTER.incrementAndGet());

        webSocket.write(new JsonObject().put("test1", "clustered").toBuffer());
        // 注册关闭回调
        MessageConsumer<Object> consumer = vertx.eventBus().consumer(binaryId + Constants.SOCKET_CLOSE_TAIL, msg -> closeSocket(webSocket));

        webSocket.handler(buffer -> messageHandler(binaryId, buffer));
        webSocket.closeHandler(v -> {
            consumer.unregister();
            logger.info("websocket closed binary id:{}, connectCount:{}", binaryId, CONNECT_COUNTER.decrementAndGet());
        });
        webSocket.exceptionHandler(e -> logger.error("websocket catch exception, binary id:{}, {}", binaryId, e.getMessage()));
    }

    private void closeSocket(ServerWebSocket webSocket) {
        logger.info("close websocket by self.");
        webSocket.close();
    }

    /**
     * websocket 消息处理器;
     *
     * @param socketBinaryHandlerId websocket handler id, 作为全局发送消息的key;
     * @param buffer 收到的二进制消息;
     */
    private void messageHandler(final String socketBinaryHandlerId, Buffer buffer) {
        logger.info("socketBinaryHandlerId: {} receive message. {}", socketBinaryHandlerId, buffer.getBytes());
        SharedData sharedData = vertx.sharedData();

        Future.<AsyncMap<String, String>>future(p -> sharedData.getClusterWideMap(ClusterDataKey.SOCKET_ID_PLAYER, p))
                .compose(map -> Future.<String>future(p -> map.get(socketBinaryHandlerId, p)))
                .onSuccess(playerId -> dispatchMessageToPlayer(socketBinaryHandlerId, playerId, buffer))
                .onFailure(tr -> tellToInitPlayer(socketBinaryHandlerId, buffer));
    }

    private void httpConnection(HttpConnection connection) {
        logger.info("get http connection: {}", connection.remoteAddress());
    }

    /**
     * 通知玩家创建;
     */
    private void tellToInitPlayer(String socketBinaryHandlerId, Buffer buffer) {
        JsonObject msg = new JsonObject().put("socketId", socketBinaryHandlerId)
                .put("buffer", buffer.getBytes());
        vertx.eventBus().request("bindPlayer", msg, res -> {
            if (res.succeeded()) {
                logger.info("player bind socket ok: {}", res.result().body());
            } else {
                // 绑定player失败, 直接断开;
                logger.error("player bind socket failed.", res.cause());
                vertx.eventBus().send(socketBinaryHandlerId + Constants.SOCKET_CLOSE_TAIL, "");
            }
        });
    }

    /**
     * 派送消息给玩家;
     */
    private void dispatchMessageToPlayer(String socketBinaryHandlerId, String playerId, Buffer buffer) {
        if (StringUtils.isEmpty(playerId)) {
            tellToInitPlayer(socketBinaryHandlerId, buffer);
            return;
        }
        vertx.eventBus().request(playerId + Constants.PLAYER_MESSAGE_HANDLER_TAIL, buffer, res -> {
            if (res.succeeded()) {
                logger.info("player consume message ok");
            } else {
                logger.error("player consume message failed.", res.cause());
            }
        });
    }

    private void addShutdownHook() {
        context.addCloseHook(h -> {
            httpServer.close(h);
            logger.info("close websocket service");
        });
    }
}
