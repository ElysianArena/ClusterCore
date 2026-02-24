package cn.ElysianArena.ClusterCore.WaterdogPE.config;

import dev.waterdog.waterdogpe.plugin.Plugin;
import dev.waterdog.waterdogpe.utils.config.Configuration;

/**
 * WaterdogPE配置类
 * Author: NemoCat
 */
public class WDPEConfig {
    
    private final Plugin plugin;
    private int heartbeatInterval;
    private int heartbeatTimeout;
    private String redisHost;
    private int redisPort;
    private String redisPassword;
    
    public WDPEConfig(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        Configuration config = plugin.getConfig();
        this.heartbeatInterval = config.getInt("heartbeat.interval", 60); // 秒
        this.heartbeatTimeout = config.getInt("heartbeat.timeout", 5); // 秒
        this.redisHost = config.getString("redis.host", "localhost");
        this.redisPort = config.getInt("redis.port", 6379);
        this.redisPassword = config.getString("redis.password", "");
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }
}