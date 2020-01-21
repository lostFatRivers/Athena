package com.jokerbee.test;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VertxTest {
    private static Logger logger = LoggerFactory.getLogger("Test");
    private static Vertx vertx;

    public static void main(String[] args) {
//        vertx = Vertx.vertx();
//        vertx.eventBus().consumer("hello", msg -> msg.reply("new"));
//        mongoSave();
//        requestNotTest();
        testList();
    }

    public static void requestNotTest() {
        vertx.eventBus().request("hello", "goodMan", res2 -> {
            if (res2.succeeded()) {
                logger.info("存在");
            } else {
                logger.error("不存在", res2.cause());
            }
        });
    }

    public static void mongoSave() {
        JsonObject config = new JsonObject();
        config.put("host", "192.168.1.222")
                .put("port", 27001)
                .put("username", "root")
                .put("password", "root")
                .put("authSource", "admin")
                .put("db_name", "mars");

        MongoClient mongo = MongoClient.createNonShared(vertx, config);

        mongo.save("PlayerEntity", config, res -> {
            if (res.succeeded()) {
                logger.info("save player success");
            } else {
                logger.error("save player failed.", res.cause());
            }
        });
    }

    public static void testList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(i);
        }
        list = list.subList(list.size() - 20, list.size());
        logger.info("list:{}", list);
    }
}
