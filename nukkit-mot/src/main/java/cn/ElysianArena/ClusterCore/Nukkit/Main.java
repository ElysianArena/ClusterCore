package cn.ElysianArena.ClusterCore.Nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.ElysianArena.ClusterCore.Nukkit.api.ClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.api.RedisClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.command.HubCommand;
import cn.ElysianArena.ClusterCore.Nukkit.command.WDTCommand;
import cn.ElysianArena.ClusterCore.Nukkit.config.NukkitConfig;
import cn.ElysianArena.ClusterCore.Nukkit.redis.RedisClient;
import cn.ElysianArena.ClusterCore.Nukkit.transfer.TransferManager;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;

/**
 * Author: NemoCat
 */
public class Main extends PluginBase {

    private static Main instance;
    private NukkitConfig config;
    private RedisClient redisClient;
    private TransferManager transferManager;
    private ClusterAPI api;
    private LangManager langManager;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.langManager = LangManager.getInstance();
        getLogger().info(langManager.getMessage("plugin.loading"));
        this.config = new NukkitConfig(this);
        this.redisClient = new RedisClient(
            config.getRedisHost(),
            config.getRedisPort(),
            config.getRedisPassword()
        );

        if (!redisClient.testConnection()) {
            getLogger().error(langManager.getMessage("redis.connection_failed"));
            return;
        }

        this.transferManager = new TransferManager();
        this.api = new RedisClusterAPI(redisClient, config.getGroupId(), config.getWdpeId(), config.isLobby());
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        registerCommands();
        registerToRedis();
        redisClient.subscribeToEvents(this::handleRedisEvent);
        startHeartbeatTask();
        getLogger().info(langManager.getMessage("plugin.loaded"));
        getLogger().info(langManager.getMessage("redis.connected", "host", config.getRedisHost(), "port", config.getRedisPort()));
        getLogger().info(langManager.getMessage("config.info", "groupId", config.getGroupId(), "wdpeId", config.getWdpeId()));
    }

    @Override
    public void onDisable() {
        if (redisClient != null) {
            redisClient.unregisterServer(config.getWdpeId());
            redisClient.shutdown();
        }

        getLogger().info(langManager.getMessage("plugin.disabling"));
    }

    private void registerCommands() {
        Server server = getServer();
        WDTCommand wdtCommand = new WDTCommand(api);
        server.getCommandMap().register("clustercore", wdtCommand);
        HubCommand hubCommand = new HubCommand(api);
        server.getCommandMap().register("clustercore", hubCommand);
        getLogger().info(langManager.getMessage("commands.registered"));
    }

    private void registerToRedis() {
        int currentPlayers = getServer().getOnlinePlayers().size();
        int maxPlayers = config.getMaxPlayers();

        redisClient.registerServer(
                config.getGroupId(),
                config.getWdpeId(),
                config.getDisplayName(),
                true,
                config.isLobby(),
                maxPlayers,
                currentPlayers
        );

        getLogger().info(langManager.getMessage("redis.registered"));
    }

    private void handleRedisEvent(String message) {
        getLogger().debug(TextFormat.GRAY + "[ClusterCore] 收到Redis事件: " + message);
    }

    private void startHeartbeatTask() {
        getServer().getScheduler().scheduleRepeatingTask(this, () -> {
            int currentPlayers = getServer().getOnlinePlayers().size();
            int maxPlayers = config.getMaxPlayers();
            boolean isOpen = true;
            redisClient.sendHeartbeat(config.getWdpeId(), currentPlayers, maxPlayers, isOpen);
        }, 20 * 30);
    }

    /**
     * 获取API实例
     */
    public ClusterAPI getAPI() {
        return api;
    }

    public NukkitConfig getNukkitConfig() {
        return config;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public TransferManager getTransferManager() {
        return transferManager;
    }
}
