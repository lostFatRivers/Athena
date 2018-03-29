package com.jokerbee.sources;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {
    private static Logger logger = LoggerFactory.getLogger("FILE");

    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setWorkerPoolSize(10);
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle("com.jokerbee.sources.BootVerticle", res -> {
            if (res.succeeded()) {
                logger.info("BI sources collector start OK.");
            } else {
                logger.error("BI sources collector start error.", res.cause());
                //vertx.close();
            }
        });
    }
}
