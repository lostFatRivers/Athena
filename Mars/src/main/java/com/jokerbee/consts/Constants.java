package com.jokerbee.consts;

/**
 * 静态常量;
 */
public interface Constants {
    /** websocket 路径 */
    String WEB_SOCKET_PATH = "/websocket";

    /** socket关闭handler的后缀 */
    String SOCKET_CLOSE_TAIL = "_close";

    /** socket handler id shared data key */
    String SOCKET_ID_CACHE_KEY = "SocketHandlerCache";

    /** 玩家替换socketId */
    String PLAYER_SWEEP_SOCKET_TAIL = "_sweep_socket";

    /** 玩家处理消息handler的后缀 */
    String PLAYER_MESSAGE_HANDLER_TAIL = "_message";

    /** 创建新玩家消息的 key */
    String PLAYER_CREATE_KEY = "playerCreate";

    /** 玩家主动断开连接消息的 key */
    String PLAYER_DESTROY_KEY = "playerLogout";


    /** 获取redis锁成功 */
    String LOCK_SUCCESS = "OK";
    /** 如果可以存在则不做任何操作, 如果不存在则设置 */
    String SET_IF_NOT_EXIST = "NX";
    /** 锁有效时间, 超时则自动删除 */
    String SET_WITH_EXPIRE_TIME = "PX";

    /** 释放锁的redis脚本 */
    String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    /** 释放锁成功的返回值 */
    Long UNLOCK_SUCCESS = 1L;

    enum ModuleType {
        // http server
        Http,
        // websocket server
        Websocket,
        // player cache
        Player,
        // player bind service
        PlayerBind,
    }
}
