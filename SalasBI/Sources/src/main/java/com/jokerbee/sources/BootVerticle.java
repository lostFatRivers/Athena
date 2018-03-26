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
        return Future.future();
    }

    private Future<Void> deployCollectors() {
        vertx.deployVerticle("com.jokerbee.sources.http.HttpReportMonitor", res -> {
            if (res.succeeded()) {
                logger.info("Http report monitor deploy success.");
            } else {
                logger.info("Http report monitor deploy failed. {}", res.cause());
            }
        });
        return Future.future();
    }
}
