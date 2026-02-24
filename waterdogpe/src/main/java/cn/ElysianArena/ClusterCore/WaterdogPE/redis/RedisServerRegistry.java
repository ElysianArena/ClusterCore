package cn.ElysianArena.ClusterCore.WaterdogPE.redis;

import com.google.gson.Gson;

/**
 * 基于Redis的服务器注册表
 * Author: NemoCat
 */
public class RedisServerRegistry {

    private final RedisManager redisManager;
    private final Gson gson;
    private final int heartbeatTimeout;

    public RedisServerRegistry(RedisManager redisManager, int heartbeatTimeout) {
        this.redisManager = redisManager;
        this.gson = new Gson();
        this.heartbeatTimeout = heartbeatTimeout;
    }

}