package com.jokerbee.sources;

import com.jokerbee.sources.protocol.InnerMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主Verticle, 部署其他verticle;
 */
public class BootVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("FILE");

    @Override
    public void start() throws Exception {
        Future<Void> fu1 = configLoad();
        Future<Void> fu2 = deployCollectors();
        CompositeFuture.all(fu1, fu2);
    }

    private Future<Void> configLoad() {
        vertx.eventBus().consumer("protocol", msg -> {
            JsonObject json = (JsonObject) msg.body();
            logger.info("message cast ok:{}", json);
            JsonObject result = new JsonObject();
            result.put("key1", "HELLO").put("key2", "BAD");
            msg.reply(result);
        });
        return Future.future();
    }

    private Future<Void> deployCollectors() {
        JsonObject request = new JsonObject();
        request.put("key1", "HELLO").put("key2", "BAD");
        vertx.eventBus().send("protocol", request, res -> {
            if (res.succeeded()) {
                JsonObject json = (JsonObject) res.result().body();
                logger.info("send message result:{}", json);
            } else {
                res.cause().printStackTrace();
            }
        });
        return Future.future();
    }
}
