package com.jokerbee.verticle;

import com.jokerbee.cache.CacheManager;
import com.jokerbee.consts.ClusterDataKey;
import com.jokerbee.consts.Constants;
import com.jokerbee.consts.RedisKey;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WsServerVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("WsServer");
    private Map<String, MessageConsumer<?>> closeConsumers = new HashMap<>();

    @Override
    public void start() throws Exception {
        createWebSocketService();
    }

    private void createWebSocketService() throws Exception {
        HttpServerOptions options = new HttpServerOptions();
        if (config().getBoolean("SSL")) {
            options.setUseAlpn(true)
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath(config().getString("cerPath")).setPassword(config().getString("cerPassword")));
        }
        HttpServer httpServer = vertx.createHttpServer(options);
        httpServer.requestHandler(this::httpRequestHandler)
                .websocketHandler(this::websocketHandler)
                .listen(config().getInteger("port"));

        logger.info("WsServerVerticle start, port:{}", config().getInteger("port"));
    }

    private void httpRequestHandler(HttpServerRequest request) {
        logger.info("Web Socket Server Get HttpRequest: {}", request.path());
        request.response().setStatusCode(404).end("<h1>Resource Not Found.</h1>");
    }

    private void websocketHandler(ServerWebSocket webSocket) {
        if (!webSocket.path().equals(Constants.WEB_SOCKET_PATH)) {
            webSocket.reject();
            return;
        }
        String binaryId = webSocket.binaryHandlerID();
        logger.info("websocket connect open: {}", binaryId);

        webSocket.write(new JsonObject().put("test1", "clustered").toBuffer());
        // 注册关闭回调
        MessageConsumer<Object> consumer = vertx.eventBus().consumer(binaryId + Constants.SOCKET_CLOSE_TAIL, msg -> closeSocket(webSocket));
        closeConsumers.put(binaryId, consumer);

        webSocket.handler(buffer -> messageHandler(binaryId, buffer));
        webSocket.closeHandler(v -> {
            consumer.unregister();
            closeConsumers.remove(binaryId);
            logger.info("websocket closed binary id:{}, closeConsumersSize:{}", binaryId, closeConsumers.size());
        });
        webSocket.exceptionHandler(e -> logger.error("websocket cache exception, binary id:{}", binaryId, e));
    }

    private void closeSocket(ServerWebSocket webSocket) {
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
                .setHandler(res -> {
                    String playerId = null;
                    if (res.succeeded()) {
                        playerId = res.result();
                    }
                    // 是否找到绑定的playerId
                    if (playerId == null) {
                        tellToInitPlayer(socketBinaryHandlerId, buffer);
                    } else {
                        dispatchMessageToPlayer(playerId, buffer);
                    }
                });
    }

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

    private void dispatchMessageToPlayer(String playerId, Buffer buffer) {
        vertx.eventBus().request(playerId + Constants.PLAYER_MESSAGE_HANDLER_TAIL, buffer, res -> {
            if (res.succeeded()) {
                logger.info("player consume message ok");
            } else {
                logger.error("player consume message failed.", res.cause());
            }
        });
    }

}
