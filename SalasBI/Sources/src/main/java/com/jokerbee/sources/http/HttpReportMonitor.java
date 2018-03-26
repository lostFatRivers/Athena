package com.jokerbee.sources.http;

import com.jokerbee.sources.common.CommonFieldName;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpReportMonitor extends AbstractVerticle {
    private final Logger LOG = LoggerFactory.getLogger("Monitor-http");

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().requestHandler(createRouter()::accept).listen(8030);
        LOG.info("start success");
    }

    private Router createRouter() {
        Router router = Router.router(getVertx());
        router.route(HttpMethod.POST, "/recharge").handler(routingContext -> {
            @Nullable JsonObject body = routingContext.getBodyAsJson();
            LOG.info("request body:{}", body);
        });

        router.route(HttpMethod.GET, "/recharge").handler(routingContext -> {
            @Nullable JsonObject body = routingContext.getBodyAsJson();
            String serverId = routingContext.get(CommonFieldName.SERVER_ID);
            LOG.info("server id:{}", serverId);
            routingContext.response().putHeader("content-type", "text/plain").end("report success");
        });

        return router;
    }


}
