package com.jokerbee.handler.http;

import com.jokerbee.anno.Route;
import com.jokerbee.handler.AbstractModule;
import io.vertx.core.json.JsonObject;

/**
 * Http Router 上注册的handler模块, class的 @Route 注解可省略.
 */
@Route("/gm")
public class GmModule extends AbstractModule {

    @Route("/player/info")
    private JsonObject getPlayerInfo(JsonObject body) {
        logger.info("get player info: {}", body);
        vertx.eventBus().send("cluster_handler_player_info", body);
        return new JsonObject().put("status", 1);
    }
}
