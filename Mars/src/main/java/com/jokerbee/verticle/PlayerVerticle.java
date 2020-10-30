package com.jokerbee.verticle;

import com.jokerbee.consts.Constants;
import com.jokerbee.player.Player;
import com.jokerbee.player.PlayerManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger("Player");

    private static final AtomicInteger aCounter = new AtomicInteger(0);

    private long counter = 0;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> promise) {
        try {
            consumer = vertx.eventBus().consumer(Constants.PLAYER_CREATE_KEY, this::createPlayer);
            counter = aCounter.getAndIncrement();
            addShutdownHook();
            logger.info("Player verticle start success.{}", counter);
            promise.complete();
        } catch (Exception e) {
            promise.fail(e);
        }
    }

    private void createPlayer(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        String socketId = body.getString("socketId");
        String playerId = body.getString("playerId");

        Future.<Void>future(pro -> {
            Player player = new Player(vertx, playerId, socketId, pro);
            PlayerManager.getInstance().putPlayer(playerId, player);
        }).onSuccess(v -> msg.reply(playerId))
        .onFailure(v -> msg.fail(0, "create error"));
    }

    private void addShutdownHook() {
        context.addCloseHook(h -> {
            consumer.unregister(h);
            logger.info("close player verticle.{}", counter);
        });
    }

}
