package com.jokerbee.verticle;

import com.jokerbee.handler.HttpHandlerManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("HTTP");

    @Override
    public void start() {
        HttpServerOptions options = new HttpServerOptions();
        vertx.createHttpServer(options).requestHandler(this.accepter())
                .listen(config().getInteger("port"));
        logger.info("Http server start, port:{}", config().getInteger("port"));
    }

    private Router accepter() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create())
                .handler(LoggerHandler.create());

        // http:127.0.0.1:8008/home.jpg
        router.route("/static/*").handler(StaticHandler.create());
        router.errorHandler(404, ctx -> {
            logger.error("resource not found");
            ctx.response().setStatusCode(404).end();
        });
        HttpHandlerManager.getInstance().registerRoute(router);

        return router;
    }



}
