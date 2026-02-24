package cn.ElysianArena.ClusterCore.Nukkit.config;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;

/**
 * Nukkit-MOT端配置类
 * Author: NemoCat
 */
public class NukkitConfig {

    private final Plugin plugin;
    private String groupId;
    private String wdpeId;
    private String displayName;
    private int maxPlayers;
    private boolean isLobby;
    private boolean redisEnabled;
    private String redisHost;
    private int redisPort;
    private String redisPassword;

    public NukkitConfig(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        Config config = plugin.getConfig();
        this.groupId = config.getString("server.groupId", "default");
        this.wdpeId = config.getString("server.wdpeId", "server-1");
        this.displayName = config.getString("server.displayName", this.wdpeId);
        this.maxPlayers = config.getInt("server.maxPlayers", 100);
        this.isLobby = config.getBoolean("server.isLobby", false);
        this.redisHost = config.getString("redis.host", "localhost");
        this.redisPort = config.getInt("redis.port", 6379);
        this.redisPassword = config.getString("redis.password", "");
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getWdpeId() {
        return wdpeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isLobby() {
        return isLobby;
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