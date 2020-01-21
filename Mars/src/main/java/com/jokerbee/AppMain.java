package com.jokerbee;

import com.jokerbee.cache.CacheManager;
import com.jokerbee.consts.Constants.ModuleType;
import com.jokerbee.handler.HttpHandlerManager;
import com.jokerbee.handler.WsHandlerManager;
import com.jokerbee.verticle.HttpServerVerticle;
import com.jokerbee.verticle.PlayerBindVerticle;
import com.jokerbee.verticle.PlayerVerticle;
import com.jokerbee.verticle.WsServerVerticle;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AppMain {
    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    private static Logger logger = LoggerFactory.getLogger("Console");

    public static void main(String[] args) {
        logger.info("Mars server start");
        try {
            List<String> configLines = Files.readAllLines(Paths.get(AppMain.class.getResource("/server.json").toURI()), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            configLines.forEach(sb::append);
            JsonObject config = new JsonObject(sb.toString());
            logger.info("server config: {}", config.encode());

            initVertx(config);
        } catch (Exception e) {
            logger.info("Mars server start failed.", e);
        }
    }

    private static void initVertx(JsonObject config) {
        ClusterManager manager = new ZookeeperClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(manager)
                .setEventBusOptions(new EventBusOptions().setHost(config.getString("clusterHost")));

        Vertx.clusteredVertx(options, res -> {
            if (res.failed()) {
                return;
            }
            Vertx vertx = res.result();
            deployVerticle(vertx, config);
        });
    }

    private static void deployVerticle(Vertx vertx, JsonObject config) {
        JsonArray modules = config.getJsonArray("openModule");
        if (modules == null || modules.size() <= 0) {
            logger.error("Mars server start failed, no module args.");
            vertx.close();
            return;
        }
        initCacheManager(config.getJsonObject("cache"));
        for (Object eachModule : modules) {
            // http 服务启动
            if (ModuleType.Http.name().equals(eachModule)) {
                initHttpHandlerManager(vertx);
                Future<String> fu = Future.future(promise -> vertx.deployVerticle(HttpServerVerticle.class.getName(),
                        new DeploymentOptions().setWorkerPoolName("HTTP").setInstances(4).setConfig(config.getJsonObject("http")), promise));
                fu.setHandler(res -> verticleDeployHandle(vertx, res, "HTTP"));
            }

            // websocket 服务启动
            if (ModuleType.Websocket.name().equals(eachModule)) {
                Future<String> fu1 = Future.future(promise -> vertx.deployVerticle(WsServerVerticle.class.getName(),
                        new DeploymentOptions().setWorkerPoolName("WebSocket").setInstances(4).setConfig(config.getJsonObject("websocket")), promise));
                fu1.setHandler(res -> verticleDeployHandle(vertx, res, "WebSocket"));
            }

            // player 服务启动
            if (ModuleType.Player.name().equals(eachModule)) {
                initWsHandlerManager(vertx);
                Future<String> fu1 = Future.future(promise -> vertx.deployVerticle(PlayerVerticle.class.getName(),
                        new DeploymentOptions().setWorkerPoolName("Player").setInstances(8).setConfig(config.getJsonObject("mongo")), promise));
                fu1.setHandler(res -> verticleDeployHandle(vertx, res, "Player"));
            }

            // player bind 服务启动
            if (ModuleType.PlayerBind.name().equals(eachModule)) {
                Future<String> fu1 = Future.future(promise -> vertx.deployVerticle(PlayerBindVerticle.class.getName(),
                        new DeploymentOptions().setWorkerPoolName("PlayerBind").setInstances(1), promise));
                fu1.setHandler(res -> verticleDeployHandle(vertx, res, "PlayerBind"));
            }
        }
    }

    private static void initCacheManager(JsonObject config) {
        try {
            CacheManager.getInstance().init(config);
        } catch (Exception e) {
            logger.error("CacheManager init error.", e);
        }
    }

    private static void initHttpHandlerManager(Vertx vertx) {
        try {
            HttpHandlerManager.getInstance().init(vertx);
        } catch (Exception e) {
            logger.error("HandlerManager init error.", e);
        }
    }

    private static void initWsHandlerManager(Vertx vertx) {
        try {
            WsHandlerManager.getInstance().init(vertx);
        } catch (Exception e) {
            logger.error("HandlerManager init error.", e);
        }
    }

    private static void verticleDeployHandle(Vertx vertx, AsyncResult<String> res, String module) {
        if (res.succeeded()) {
            logger.info("Mars {} server start success", module);
        } else {
            logger.info("Mars {} server start failed", module, res.cause());
            vertx.close();
        }
    }

}
