package com.jokerbee.handler;

import com.jokerbee.anno.Route;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public enum HttpHandlerManager {
    INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger("Handler");

    private Vertx vertx;
    private final Map<String, Method> handlerMap = new HashMap<>();
    private final Map<String, Object> hostMap = new HashMap<>();

    public static HttpHandlerManager getInstance() {
        return INSTANCE;
    }

    public void init(Vertx vertx) throws Exception {
        this.vertx = vertx;
        scanHandlers();
    }

    private void scanHandlers() throws Exception {
        Reflections reflections = new Reflections("com.jokerbee.handler.http");
        Set<Class<? extends AbstractModule>> handlerClassSet = reflections.getSubTypesOf(AbstractModule.class);
        for (Class<? extends AbstractModule> eachClass : handlerClassSet) {
            logger.info("new instance handler:{}", eachClass.getName());
            newInstance(eachClass);
        }
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

    public void close() {
        handlerMap.clear();
        hostMap.clear();
    }
}
