package com.jokerbee.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PlayerManager {
    INSTANCE;

    private static Logger logger = LoggerFactory.getLogger("PlayerManager");

    private Map<String, Player> players = new ConcurrentHashMap<>();

    public static PlayerManager getInstance() {
        return INSTANCE;
    }

    public void putPlayer(String playerId, Player player) {
        logger.info("put new player:{}", playerId);
        players.put(playerId, player);
    }

    public Player getPlayer(String account) {
        return players.get(account);
    }
}
