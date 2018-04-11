package com.jokerbee.sources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主Verticle, 部署其他verticle;
 */
public class BootVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("FILE");

    public static final int COLLECTOR_SIZE = 5;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<String> fu1 = configLoad();
        Future<String> fu2 = deployCollectors();
        Future<String> fu3 = deployDBService();
        CompositeFuture.all(fu1, fu2, fu3).setHandler(res -> {
            if (res.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private Future<String> configLoad() {
        return Future.future();
    }

    private Future<String> deployCollectors() {
        Future<String> future = Future.future();
        vertx.deployVerticle("com.jokerbee.sources.http.HttpReportMonitor", future);
        return future;
    }

    private Future<String> deployDBService() {
        Future<String> future = Future.future();
        DeploymentOptions options = new DeploymentOptions().setInstances(COLLECTOR_SIZE);
        vertx.deployVerticle("com.jokerbee.sources.db.PostgresDBService", options, future);
        return future;
    }
}
