package com.jokerbee.sources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

/**
 * 主Verticle, 部署其他verticle? or 自己做所有的事情?
 */
public class BootVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Future<Void> fu1 = configLoad();
        Future<Void> fu2 = deployCollectors();

        CompositeFuture.all(fu1, fu2);
    }

    private Future<Void> deployCollectors() {
        return null;
    }

    private Future<Void> configLoad() {
        return null;
    }

}
