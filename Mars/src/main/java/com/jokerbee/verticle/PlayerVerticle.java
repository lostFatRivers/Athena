package com.jokerbee.verticle;

import com.jokerbee.consts.Constants;
import com.jokerbee.handler.WsHandlerManager;
import com.jokerbee.player.Player;
import com.jokerbee.player.PlayerManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("Player");
    private static long counter = 0;
    private static AtomicInteger aCounter = new AtomicInteger(0);

    private MongoClient mongo;

    @Override
    public void start() {
        vertx.eventBus().consumer(Constants.PLAYER_CREATE_KEY, this::createPlayer);
        vertx.eventBus().consumer(Constants.PLAYER_DESTROY_KEY, this::destroyPlayer);
        mongo = MongoClient.createNonShared(vertx, config());
        logger.info("Player handler start success.");
    }

    private void createPlayer(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        String socketId = body.getString("socketId");
        String playerId = body.getString("playerId");

        Player player = new Player(vertx, playerId, socketId);
        player.registerConsumers();

        msg.reply(playerId);
    }

    private void destroyPlayer(Message<JsonObject> msg) {

    }

    private void initPlayer(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        String socketId = body.getString("socketId");
        Buffer buffer = Buffer.buffer(msg.body().getBinary("buffer"));
        String playerId = body.getString("playerId");

        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (player == null) {
            player = new Player(vertx, playerId, socketId);
        } else {
            if (StringUtils.isNotEmpty(player.getSocketId())) {
                vertx.eventBus().send(player.getSocketId() + Constants.SOCKET_CLOSE_TAIL, "");
            }
        }
        player.setSocketId(socketId);
        WsHandlerManager.getInstance().onProtocol(player, buffer);
    }


}
