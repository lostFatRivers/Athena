package com.jokerbee.player;

import com.jokerbee.consts.ClusterDataKey;
import com.jokerbee.consts.Constants;
import com.jokerbee.handler.WsHandlerManager;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player {
    private static Logger logger = LoggerFactory.getLogger("Player");

    private Vertx vertx;
    private String playerId;
    private String socketId;

    private MessageConsumer<JsonObject> weepConsumer;
    private MessageConsumer<Buffer> messageConsumer;

    public Player(Vertx vertx, String playerId, String socketId) {
        this.vertx = vertx;
        this.playerId = playerId;
        this.socketId = socketId;
        resetSocketPlayerMap(socketId).setHandler(res -> {
            if (res.failed()) {
                logger.error("reset socket id error.", res.cause());
            }
        });
    }

    public void registerConsumers() {
        weepConsumer = vertx.eventBus().consumer(this.playerId + Constants.PLAYER_SWEEP_SOCKET_TAIL, this::sweepSocketId);
        messageConsumer = vertx.eventBus().consumer(this.playerId + Constants.PLAYER_MESSAGE_HANDLER_TAIL, this::acceptMessage);
    }

    private void sweepSocketId(Message<JsonObject> tMessage) {
        String newSocketId = tMessage.body().getString("socketId");
        if (StringUtils.isEmpty(newSocketId)) {
            logger.error("sweep socket id error. empty.");
            tMessage.reply("sweep error empty.");
            return;
        }
        resetSocketPlayerMap(newSocketId).setHandler(res -> {
            if (res.succeeded()) {
                this.socketId = newSocketId;
            } else {
                logger.error("sweep socket id error.", res.cause());
            }
            tMessage.reply("sweep success.");
        });
    }

    private Future<Void> resetSocketPlayerMap(String newSocketId) {
        SharedData sharedData = vertx.sharedData();
        return Future.<AsyncMap<String, String>>future(p -> sharedData.getClusterWideMap(ClusterDataKey.SOCKET_ID_PLAYER, p))
                .compose(map -> Future.future(p -> {
                    if (newSocketId.equals(this.socketId)) {
                        p.complete();
                        return;
                    }
                    // 有老的 socket - playerId 映射, 则删除并关闭 socket;
                    if (StringUtils.isNotEmpty(this.socketId)) {
                        String oldSocketId = this.socketId;
                        map.remove(oldSocketId, oid -> {
                            if (oid.succeeded()) {
                                vertx.eventBus().send(oldSocketId + Constants.SOCKET_CLOSE_TAIL, "close");
                                logger.info("remove old socketId success:{}", oldSocketId);
                            } else {
                                logger.error("remove old socketId error:{}", oldSocketId);
                            }
                        });
                    }
                    // 存入新的 socket - playerId 映射;
                    map.put(newSocketId, this.playerId, p);
                }));
    }

    private void acceptMessage(Message<Buffer> tMessage) {
        WsHandlerManager.getInstance().onProtocol(this, tMessage.body());
        tMessage.reply("accepted message");
    }

    public void destroy() {
        weepConsumer.unregister();
        messageConsumer.unregister();
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
