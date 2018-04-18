package com.jokerbee.sources.db;

import com.jokerbee.sources.utils.TimeUtil;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PostgresDBService extends AbstractVerticle {
    private volatile static AtomicInteger counter = new AtomicInteger(0);
    private final int ID = counter.getAndIncrement();
    public static final String DB_CONSUMER_NAME = "ReportData-";

    private final Logger LOG = LoggerFactory.getLogger("FILE");
    private Map<String, Map<Integer, JsonObject>> cache = new HashMap<>();

    private AsyncSQLClient sqlClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> future1 = initDBClient();
        Future<Void> future2 = deployConsumers();
        CompositeFuture.all(future1, future2).setHandler(res -> {
            if (res.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private Future<Void> deployConsumers() {
        Vertx vertx = getVertx();
        vertx.eventBus().consumer(DB_CONSUMER_NAME + ID, message -> {
            JsonObject body = JsonObject.mapFrom(message.body());
            LOG.info("receive message:{}", body);
            collectData(body);
        });
        return Future.future();
    }

    private Future<Void> initDBClient() {
        Future<Void> future = Future.future();
        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(res -> configResultHandler(res, future));
        return future;
    }

    private void configResultHandler(AsyncResult<JsonObject> res, Future<Void> startFuture) {
        if (res.succeeded()) {
            JsonObject result = res.result();
            JsonObject dbConfig = result.getJsonObject("dbConfig");
            LOG.info("find db config:{}", dbConfig);
            sqlClient = PostgreSQLClient.createShared(getVertx(), dbConfig, "PgSql-Pool");
            checkConnection(startFuture);
        } else {
            startFuture.fail("db config not found");
        }
    }

    private void checkConnection(Future<Void> startFuture) {
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.query("select count(*) from normal;", res2 -> {
                    if (res.succeeded()) {
                        LOG.info("DB connect success. [select count(*) from normal] result:{}", res2.result().getResults().get(0));
                        startFuture.complete();
                    } else {
                        startFuture.fail("get db connection failed.");
                    }
                    connection.close();
                });
            } else {
                startFuture.fail("create shared client pool failed.");
            }
        });
    }

    private void collectData(JsonObject body) {
        JsonArray paths = body.getJsonArray("paths");
        String data = body.getString("body");
        String serverIdStr = paths.getString(0);
        int serverId = 0;
        if (StringUtils.isNumeric(serverIdStr)) {
            serverId = Integer.parseInt(serverIdStr);
        } else {
            LOG.error("first path not serverId:{}", serverIdStr);
            return;
        }
        String today = TimeUtil.getCurrentFormatDay();
        if (!cache.containsKey(today)) {
            cache.put(today, new HashMap<>());
        }
        Map<Integer, JsonObject> serverData = cache.get(today);
        if (!serverData.containsKey(serverId)) {
            serverData.put(serverId, new JsonObject());
        }
        JsonObject jsonObject = serverData.get(serverId);
        paths.remove(0);
        Iterator<Object> iterator = paths.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next().toString();
            if (!jsonObject.containsKey(name)) {
                jsonObject.put(name, new JsonObject());
            }
            jsonObject = jsonObject.getJsonObject(name);
            if (!iterator.hasNext()) {
                jsonObject.put("data", data);
            }
        }
        saveData(serverId, today, serverData.get(serverId));
    }

    private void saveData(int serverId, String today, JsonObject data) {
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.queryWithParams("select jsondata from normal where serverid = ? and day = ?", new JsonArray().add(serverId).add(today), res2 -> {
                    if (res.succeeded()) {
                        String jsonStr = res2.result().getResults().get(0).getString(0);

                        if (StringUtils.isEmpty(jsonStr)) {
                            insertData(serverId, today, data);
                        } else {
                            JsonObject loadData = new JsonObject(jsonStr);
                            data.mergeIn(loadData, true);
                            updateData(serverId, today, data);
                        }
                    } else {
                        LOG.error("select data error.", res2.cause());
                    }
                    connection.close();
                });
            } else {
                LOG.error("get connection failed.", res.cause());
            }
        });
    }

    private void insertData(int serverId, String today, JsonObject data) {
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.updateWithParams("insert into normal (serverid, jsondata, day) values (?, ?::json, ?)", new JsonArray().add(serverId).add(data.toString()).add(today), upRes -> {
                    if (upRes.succeeded()) {
                        LOG.debug("insert success");
                    } else {
                        LOG.error("data insert failed.", res.cause());
                    }
                });
            } else {
                LOG.error("get connection failed.", res.cause());
            }
        });
    }

    private void updateData(int serverId, String today, JsonObject data) {
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.updateWithParams("update normal set jsondata = ?::json where serverid = ? and day = ?", new JsonArray().add(data.toString()).add(serverId).add(today), upRes -> {
                    if (upRes.succeeded()) {
                        LOG.debug("insert success");
                    } else {
                        LOG.error("data insert failed.", res.cause());
                    }
                });
            } else {
                LOG.error("get connection failed.", res.cause());
            }
        });
    }

}
