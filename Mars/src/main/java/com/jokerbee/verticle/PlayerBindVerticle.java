package com.jokerbee.verticle;

import com.jokerbee.cache.CacheManager;
import com.jokerbee.consts.Constants;
import com.jokerbee.consts.RedisKey;
import com.jokerbee.message.BufferTranslator;
import com.jokerbee.protocol.Game.PlayerLogin;
import com.jokerbee.protocol.MessageCode;
import com.jokerbee.protocol.System.ProtocolWrapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将 player 对象和网络 socketId 绑定 Verticle.
 * 全集群唯一, 保证玩家单点登录正常.
 */
public class PlayerBindVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger("PlayerBind");

    @Override
    public void start() {
        vertx.eventBus().consumer("bindPlayer", this::bindPlayer);
    }

    private void bindPlayer(Message<JsonObject> msg) {
        Buffer buffer = Buffer.buffer(msg.body().getBinary("buffer"));
        String socketId = msg.body().getString("socketId");
        ProtocolWrapper wrapper = BufferTranslator.toWrapper(buffer);
        // 检测是否是登录消息
        if (wrapper == null || wrapper.getCode() != MessageCode.Code.CS_ACCOUNT_LOGIN_VALUE) {
            msg.fail(-1, "message type error");
            return;
        }
        PlayerLogin loginMessage;
        try {
            loginMessage = PlayerLogin.parseFrom(wrapper.getBody());
        } catch (Exception e) {
            logger.error("message parse error.", e);
            msg.fail(-2, "message parse error");
            return;
        }
        String account = loginMessage.getAccount();
        String password = loginMessage.getPassword();

        // 查找 playerId; 绑定并发送登录消息;
        searchPlayerId(account)
                .compose(pId -> sweepPlayerSocketId(socketId, account, pId))
                .compose(pId -> sendLoginMessage(pId, buffer))
                .setHandler(res -> {
                    if (res.succeeded()) {
                        msg.reply("bind success");
                    } else {
                        logger.error("player bind socket failed", res.cause());
                        msg.fail(-3, "player bind socket failed");
                    }
                });
    }

    /**
     * 通过 account 查询玩家id, 如果没有查到则认为是新玩家;
     *
     * @param account 玩家账号;
     * @return 玩家 id 的 future;
     */
    private Future<String> searchPlayerId(String account) {
        return Future.future(pm -> vertx.executeBlocking(p -> {
                String playerId = CacheManager.getInstance().redis().hget(RedisKey.ACCOUNT_TO_PLAYER_ID, account);
                p.complete(playerId);
            }, false, pm));
    }

    /**
     * 通知 Player 重新绑定 socket;
     * 若通知失败, 则通知创建新的 Player, 然后绑定 socket;
     *
     * @param socketId 网络 socket id;
     * @param playerId 玩家 id;
     * @return 绑定结果 Future;
     */
    private Future<String> sweepPlayerSocketId(String socketId, String account, String playerId) {
        if (StringUtils.isEmpty(playerId)) {
            playerId = new ObjectId().toHexString();
            CacheManager.getInstance().redis().hset(RedisKey.ACCOUNT_TO_PLAYER_ID, account, playerId);
        }
        final String truePlayerId = playerId;
        JsonObject msg = new JsonObject().put("socketId", socketId);
        return Future.future(p -> vertx.eventBus().request(truePlayerId + Constants.PLAYER_SWEEP_SOCKET_TAIL, msg, res -> {
            if (res.succeeded()) {
                p.complete(truePlayerId);
            } else {
                logger.info("no such player handler:{}", res.cause().getMessage());
                createPlayer(truePlayerId, socketId).setHandler(p);
            }
        }));
    }

    /**
     * 创建新玩家对象
     */
    private Future<String> createPlayer(String playerId, String socketId) {
        JsonObject msg = new JsonObject().put("playerId", playerId).put("socketId", socketId);
        return Future.future(p -> vertx.eventBus().<String>request(Constants.PLAYER_CREATE_KEY, msg, res -> {
            if (res.succeeded()) {
                p.complete(res.result().body());
            } else {
                logger.info("create player failed. playerId:{}", playerId,  res.cause());
                p.fail("create player failed. playerId:" + playerId);
            }
        }));
    }

    private Future<Message<Object>> sendLoginMessage(String playerId, Buffer buffer) {
        return Future.future(p -> vertx.eventBus().request(playerId + Constants.PLAYER_MESSAGE_HANDLER_TAIL, buffer, p));
    }

    @Override
    public void stop() throws Exception {

    }
}
