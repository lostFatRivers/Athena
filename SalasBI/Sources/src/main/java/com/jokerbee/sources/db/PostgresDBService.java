package com.jokerbee.sources.db;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresDBService extends AbstractVerticle {
    private final Logger LOG = LoggerFactory.getLogger("FILE");

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
        vertx.eventBus().consumer("ReportData", message -> {
            Object body = message.body();
            LOG.info("receive message:{}", body);
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
            AsyncSQLClient sqlClient = PostgreSQLClient.createShared(getVertx(), dbConfig, "PgSql-Pool");
            sqlClient.getConnection(res2 -> {
                connectionResultHandler(res2, startFuture);
            });
            LOG.info("find db config:{}", dbConfig);
        } else {
            startFuture.fail("db config not found");
        }
    }

    private void connectionResultHandler(AsyncResult<SQLConnection> res, Future<Void> startFuture) {
        if (res.succeeded()) {
            SQLConnection connection = res.result();
            connection.query("select count(*) from normal;", res2 -> {
                queryResultHandler(res2, startFuture);
            });
        } else {
            startFuture.fail("create shared client pool failed.");
        }
    }

    private void queryResultHandler(AsyncResult<ResultSet> res, Future<Void> startFuture) {
        if (res.succeeded()) {
            ResultSet resultSet = res.result();
            LOG.info("DB connect success. [select count(*) from normal] result:{}", resultSet.getResults().get(0));
            startFuture.complete();
        } else {
            startFuture.fail("get db connection failed.");
        }
    }
}
