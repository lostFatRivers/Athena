package com.jokerbee.handler;

import com.jokerbee.anno.MessageHandler;
import com.jokerbee.anno.Route;
import com.jokerbee.handler.http.GmModule;
import com.jokerbee.handler.ws.LoginModule;
import com.jokerbee.message.BufferTranslator;
import com.jokerbee.player.Player;
import com.jokerbee.protocol.System.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum WsHandlerManager {
    INSTANCE;
    private static Logger logger = LoggerFactory.getLogger("Handler");

    private Vertx vertx;
    private Map<Integer, Method> handlerMap = new HashMap<>();
    private Map<Integer, Object> hostMap = new HashMap<>();

    public static WsHandlerManager getInstance() {
        return INSTANCE;
    }

    public void init(Vertx vertx) throws Exception {
        this.vertx = vertx;
        createHandlers();
    }

    private void createHandlers() throws Exception {
        newInstance(LoginModule.class);
    }

    private void newInstance(Class<? extends AbstractModule> clazz) throws Exception {
        if (clazz == null) {
            return;
        }
        AbstractModule instance = clazz.getDeclaredConstructor().newInstance();
        instance.setVertx(vertx);

        Method[] methods = clazz.getDeclaredMethods();
        Stream.of(methods).filter(each -> {
            MessageHandler annotation = each.getAnnotation(MessageHandler.class);
            return annotation != null;
        }).forEach(each -> {
            each.setAccessible(true);
            MessageHandler annotation = each.getAnnotation(MessageHandler.class);
            handlerMap.put(annotation.code(), each);
            hostMap.put(annotation.code(), instance);
        });
    }

    public void onProtocol(Player player, Buffer buffer) {
        ProtocolWrapper wrapper = BufferTranslator.toWrapper(buffer);
        if (wrapper == null) {
            logger.error("protocol message translate failed. buffer:{}", buffer);
            return;
        }
        Method method = handlerMap.get(wrapper.getCode());
        if (method == null) {
            logger.error("protocol message not have handler method:{}", wrapper.getCode());
            return;
        }
        try {
            method.invoke(hostMap.get(wrapper.getCode()), player, wrapper);
        } catch (Exception e) {
            logger.error("protocol message handle error:{}", wrapper.getCode(), e);
        }
    }
}
