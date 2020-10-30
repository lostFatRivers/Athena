package com.jokerbee;

import com.jokerbee.cache.CacheManager;
import com.jokerbee.consts.Constants.ModuleType;
import com.jokerbee.handler.HttpHandlerManager;
import com.jokerbee.handler.WsHandlerManager;
import com.jokerbee.verticle.HttpServerVerticle;
import com.jokerbee.verticle.PlayerBindVerticle;
import com.jokerbee.verticle.PlayerVerticle;
import com.jokerbee.verticle.WsServerVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppMain {
    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    private static final Logger logger = LoggerFactory.getLogger("Console");

    private static ConfigRetriever configRetriever;
    private static Context bootContext;

    public static void main(String[] args) {
        logger.info("Mars server start");
        createClusterVertx()
                .compose(AppMain::loadConfig)
                .compose(AppMain::deployVerticle)
                .onSuccess(v -> {
                    logger.info("boot Mars server success");
                    addShutdownOptional(bootContext.owner());
                })
                .onFailure(tr -> {
                    logger.info("boot Mars server failed.", tr);
                    bootContext.owner().close();
                });
    }

    /**
     * 创建集群模式 Vertx;
     */
    private static Future<Vertx> createClusterVertx() {
        VertxOptions options = new VertxOptions().setClusterManager(new HazelcastClusterManager());
        return Future.future(pro -> Vertx.clusteredVertx(options, pro));
    }

    /**
     * 加载服务器配置;
     */
    private static Future<JsonObject> loadConfig(Vertx vertx) {
        bootContext = vertx.getOrCreateContext();
        ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "server.conf"));
        logger.info("start load server config");
        configRetriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store));
        return Future.future(configRetriever::getConfig);
    }

    private static Future<Void> deployVerticle(JsonObject config) {
        configRetriever.close();
        Vertx vertx = bootContext.owner();
        logger.info("start deploy verticle");

        return Future.future(pro -> {
            JsonArray modules = config.getJsonArray("openModule");
            if (modules == null || modules.size() <= 0) {
                pro.fail(new RuntimeException("server config no module args."));
            } else {
                pro.complete();
            }
        })
        .compose(v -> Future.<Void>future(pro -> initCacheManager(config.getJsonObject("cache"), pro)))
        .compose(v -> {
            JsonArray modules = config.getJsonArray("openModule");
            @SuppressWarnings("rawtypes")
            List<Future> futureList = new ArrayList<>();
            for (Object eachModule : modules) {
                // http 服务启动
                if (ModuleType.Http.name().equals(eachModule)) {
                    Future<String> fu = Future.<Void>future(pro -> initHttpHandlerManager(vertx, pro))
                            .compose(iv -> Future.future(promise -> vertx.deployVerticle(HttpServerVerticle.class.getName(),
                                    new DeploymentOptions().setWorkerPoolName("HTTP").setInstances(4).setConfig(config.getJsonObject("http")), promise)));
                    futureList.add(fu);
                }

                // websocket 服务启动
                if (ModuleType.Websocket.name().equals(eachModule)) {
                    Future<String> fu1 = Future.future(promise -> vertx.deployVerticle(WsServerVerticle.class.getName(),
                            new DeploymentOptions().setWorkerPoolName("WebSocket").setInstances(4).setConfig(config.getJsonObject("websocket")), promise));
                    futureList.add(fu1);
                }

                // player 服务启动
                if (ModuleType.Player.name().equals(eachModule)) {
                    Future<String> fu2 = Future.<Void>future(pro -> initWsHandlerManager(vertx, pro))
                            .compose(iv -> Future.future(promise -> vertx.deployVerticle(PlayerVerticle.class.getName(),
                                    new DeploymentOptions().setWorkerPoolName("Player").setInstances(8).setConfig(config.getJsonObject("mongo")), promise)));
                    futureList.add(fu2);
                }

                // player bind 服务启动
                if (ModuleType.PlayerBind.name().equals(eachModule)) {
                    Future<String> fu3 = Future.future(promise -> vertx.deployVerticle(PlayerBindVerticle.class.getName(),
                            new DeploymentOptions().setWorkerPoolName("PlayerBind").setInstances(1), promise));
                    futureList.add(fu3);
                }
            }
            return CompositeFuture.all(futureList);
        }).compose(cf -> Future.succeededFuture());
    }

    private static void initCacheManager(JsonObject config, Promise<Void> pro) {
        try {
            CacheManager.getInstance().init(config);
            pro.complete();
        } catch (Exception e) {
            logger.error("CacheManager init error.");
            pro.fail(e);
        }
    }

    private static void initHttpHandlerManager(Vertx vertx, Promise<Void> pro) {
        try {
            HttpHandlerManager.getInstance().init(vertx);
            pro.complete();
        } catch (Exception e) {
            logger.error("HandlerManager init error.");
            pro.fail(e);
        }
    }

    private static void initWsHandlerManager(Vertx vertx, Promise<Void> pro) {
        try {
            WsHandlerManager.getInstance().init(vertx);
            pro.complete();
        } catch (Exception e) {
            logger.error("HandlerManager init error.");
            pro.fail(e);
        }
    }

    private static void addShutdownOptional(Vertx vertx) {
        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    int read = System.in.read();
                    logger.info("read console input:{}", read);
                    if (read == 10) {
                        logger.info("******************************************");
                        logger.info("***                                    ***");
                        logger.info("*****            Good bye            *****");
                        logger.info("***                                    ***");
                        logger.info("******************************************");
                        HttpHandlerManager.getInstance().close();
                        WsHandlerManager.getInstance().close();
                        CacheManager.getInstance().close();
                        vertx.close();
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
