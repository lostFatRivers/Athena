package com.jokerbee.sources.http;

import com.jokerbee.sources.common.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <b>RESTFul API设计; </b> <br/>
 * <br/>
 * GET (select): 从服务器取出资源 (一项或多项) <br/>
 * POST (create): 在服务器新建一个资源 <br/>
 * PUT (update): 在服务器更新资源 (客户端提供改变后的完整资源) <br/>
 * PATCH (update): 在服务器更新资源 (客户端提供改变的属性) <br/>
 * DELETE (delete): 从服务器删除资源<br/>
 * <br/>
 * HEAD：获取资源的元数据 <br/>
 * OPTIONS：获取信息，关于资源的哪些属性是客户端可以改变的 <br/>
 */
public class HttpReportMonitor extends AbstractVerticle {
    private final Logger LOG = LoggerFactory.getLogger("Monitor-http");

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().requestHandler(createRouter()::accept).listen(8030);
        //vertx.createNetServer().connectHandler(connectHandler()).listen(4321);
        LOG.info("start success");
    }

    private Router createRouter() {
        Router router = Router.router(getVertx());
        router.route().handler(CookieHandler.create())
                .handler(BodyHandler.create())
                .handler(SessionHandler.create(LocalSessionStore.create(getVertx())));

        router.route(HttpMethod.GET, Constants.REPORT_PATH).consumes(Constants.REPORT_CONTENT_TYPE).handler(getSource());
        router.route(HttpMethod.POST, Constants.REPORT_PATH).consumes(Constants.REPORT_CONTENT_TYPE).handler(postSource());
        router.route(HttpMethod.DELETE, Constants.REPORT_PATH).consumes(Constants.REPORT_CONTENT_TYPE).handler(deleteSource());

        router.route(HttpMethod.PUT, Constants.REPORT_PATH).handler(defaultHandler(HttpMethod.PUT.name()));
        router.route(HttpMethod.PATCH, Constants.REPORT_PATH).handler(defaultHandler(HttpMethod.PATCH.name()));
        router.route(HttpMethod.HEAD, Constants.REPORT_PATH).handler(defaultHandler(HttpMethod.HEAD.name()));
        router.route(HttpMethod.OPTIONS, Constants.REPORT_PATH).handler(defaultHandler(HttpMethod.OPTIONS.name()));

        router.exceptionHandler(tw -> LOG.info("http error:{}", tw.getMessage()));

        return router;
    }

    private Handler<RoutingContext> getSource() {
        return rtc -> {
            List<String> pathList = getPathList(rtc.normalisedPath());
            System.out.println(pathList);
            rtc.response().end(Constants.END_CODE);
        };
    }

    private Handler<RoutingContext> postSource() {
        return rtc -> {
            List<String> pathList = getPathList(rtc.normalisedPath());
            System.out.println(pathList);
            rtc.response().end(Constants.END_CODE);
        };
    }

    private Handler<RoutingContext> deleteSource() {
        return rtc -> {
            List<String> pathList = getPathList(rtc.normalisedPath());
            System.out.println(pathList);
            rtc.response().end(Constants.END_CODE);
        };
    }

    private List<String> getPathList(String path) {
        if ("/".equals(path)) return Collections.emptyList();
        ArrayList<String> pathList = new ArrayList<>(Arrays.asList(path.split("/")));
        pathList.remove(0);
        return pathList;
    }

    private Handler<RoutingContext> defaultHandler(String method) {
        return routingContext -> {
            LOG.info("{} request path:{}", method, routingContext.normalisedPath());
            LOG.info("{} request body:{}", method, routingContext.getBody());
            LOG.info("{} request params:{}", method, routingContext.request().params());
            routingContext.response()
                    .putHeader("content-type", "text/plain")
                    .end(method + " is invalid report method.");
        };
    }

    private Handler<NetSocket> connectHandler() {
        return socket -> {
            socket.handler(buf -> {
                int length = buf.length();
                System.out.println("get data, length:" + length);
                System.out.println("get data, body:");
                System.out.println(buf.toString("UTF-8"));

                String response = "HTTP/1.1 200 OK\n" +
                        "ContentType: text/html\n" +
                        "Server: localhost\n" +
                        "Date: Sun Dec 11 21:40:46 2018\r\n\r\n" +
                        "<html><head><title>simple httpServer</title></head><body><h1>Hello</h1></body></html>";
                System.out.println(response);

                socket.write(response);

                socket.end();
            });

            socket.closeHandler(v -> {
                LOG.info("socket closed.");
            });

            socket.exceptionHandler(tw -> {
                LOG.info("socket error:{}", tw.getMessage());
            });
        };
    }

}
