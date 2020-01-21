package com.jokerbee.handler;

import com.jokerbee.anno.Route;
import com.jokerbee.handler.http.GmModule;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum HttpHandlerManager {
    INSTANCE;
    private static Logger logger = LoggerFactory.getLogger("Handler");

    private Vertx vertx;
    private Map<String, Method> handlerMap = new HashMap<>();
    private Map<String, Object> hostMap = new HashMap<>();

    public static HttpHandlerManager getInstance() {
        return INSTANCE;
    }

    public void init(Vertx vertx) throws Exception {
        this.vertx = vertx;
        createHandlers();
    }

    private void createHandlers() throws Exception {
        newInstance(GmModule.class);
    }

    public void registerRoute(Router router) {
        for (String eachPath : handlerMap.keySet()) {
            Method method = handlerMap.get(eachPath);
            router.route(eachPath).method(HttpMethod.POST).handler(ctx -> {
                try {
                    JsonObject result = (JsonObject) method.invoke(hostMap.get(eachPath), ctx.getBodyAsJson());
                    logger.error("request [{}] invoke success.", eachPath);
                    ctx.response().putHeader("content-type", "application/json").end(result.encode());
                } catch (Exception e) {
                    logger.error("request [{}] invoke failed.", eachPath, e);
                    ctx.response().setStatusCode(502).end();
                }
            });
        }
    }

    private void newInstance(Class<? extends AbstractModule> clazz) throws Exception {
        if (clazz == null) {
            return;
        }
        AbstractModule instance = clazz.getDeclaredConstructor().newInstance();
        instance.setVertx(vertx);

        Route clazzAnnotation = clazz.getAnnotation(Route.class);
        String classRoutePath = clazzAnnotation == null ? "" : clazzAnnotation.value();

        Method[] methods = clazz.getDeclaredMethods();
        Stream.of(methods).filter(each -> {
            Route annotation = each.getAnnotation(Route.class);
            return annotation != null;
        }).forEach(each -> {
            each.setAccessible(true);
            Route annotation = each.getAnnotation(Route.class);
            handlerMap.put(classRoutePath + annotation.value(), each);
            hostMap.put(classRoutePath + annotation.value(), instance);
        });
    }
}
