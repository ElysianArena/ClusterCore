package cn.ElysianArena.ClusterCore.WaterdogPE;

import dev.waterdog.waterdogpe.plugin.Plugin;
import cn.ElysianArena.ClusterCore.WaterdogPE.config.WDPEConfig;
import cn.ElysianArena.ClusterCore.WaterdogPE.redis.RedisManager;
import cn.ElysianArena.ClusterCore.WaterdogPE.redis.RedisServerRegistry;

/**
 * Author: NemoCat
 */
public class Main extends Plugin {
    
    private static Main instance;
    private WDPEConfig config;
    private RedisManager redisManager;
    private RedisServerRegistry serverRegistry;

    @Override
    public void onStartup() {
        instance = this;
        getLogger().info("[" + getDescription().getName() + "] 正在启动版本 " + getDescription().getVersion());
    }
    
    @Override
    public void onEnable() {
        this.config = new WDPEConfig(this);
        this.redisManager = new RedisManager(
            config.getRedisHost(),
            config.getRedisPort(),
            config.getRedisPassword(),
            (dev.waterdog.waterdogpe.logger.Logger) getLogger()
        );

        if (!redisManager.testConnection()) {
            getLogger().error("[" + getDescription().getName() + "] Redis连接失败！");
            return;
        }

        this.serverRegistry = new RedisServerRegistry(redisManager, config.getHeartbeatTimeout());
        redisManager.subscribeToEvents(this::handleRedisEvent);
        getLogger().info("[" + getDescription().getName() + "] WaterdogPE 模块已加载");
        getLogger().info("[" + getDescription().getName() + "] Redis连接成功: " + config.getRedisHost() + ":" + config.getRedisPort());
    }
    
    @Override
    public void onDisable() {
        if (redisManager != null) {
            redisManager.shutdown();
        }

        getLogger().info("[" + getDescription().getName() + "] WaterdogPE 模块正在关闭");
    }

    /**
     * 处理Redis事件
     */
    private void handleRedisEvent(String message) {
        getLogger().debug("收到Redis事件: " + message);
    }

}
