package com.jokerbee.verticle;

import com.jokerbee.handler.HttpHandlerManager;
import com.jokerbee.http.handler.AESDecodeHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger("HTTP");

    private HttpServer httpServer;
    private String encryptKey;

    @Override
    public void start(Promise<Void> promise) {
        try {
            HttpServerOptions options = new HttpServerOptions();
            encryptKey = config().getString("encryptKey");
            httpServer = vertx.createHttpServer(options).requestHandler(this.acceptor())
                    .listen(config().getInteger("port"));
            addShutdownHook();
            logger.info("Http server start, port:{}", config().getInteger("port"));
            promise.complete();
        } catch (Exception e) {
            promise.fail(e);
        }
    }

    private Router acceptor() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create())
                .handler(AESDecodeHandler.create(encryptKey))
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

    private void addShutdownHook() {
        context.addCloseHook(h -> {
            httpServer.close(h);
            logger.info("close http service");
        });
    }

}
