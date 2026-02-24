package cn.ElysianArena.ClusterCore.Nukkit;

import cn.ElysianArena.ClusterCore.Nukkit.api.ClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;
import cn.ElysianArena.ClusterCore.Nukkit.transfer.TransferManager;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.ServerStopEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 主监听器
 * Author: NemoCat
 */

public class EventListener implements Listener {

    private final Main plugin;
    private final LangManager langManager;
    private final ClusterAPI api;
    private final TransferManager transferManager;

    public EventListener(Main plugin) {
        this.plugin = plugin;
        this.langManager = LangManager.getInstance();
        this.api = plugin.getAPI();
        this.transferManager = plugin.getTransferManager();
    }

    /**
     * 处理服务器关闭事件
     * 将所有在线玩家传送到大厅服务器
     */
    @EventHandler
    public void onServerStop(ServerStopEvent event) {
        Server server = plugin.getServer();
        Map<UUID, Player> onlinePlayers = server.getOnlinePlayers();

        if (plugin.getNukkitConfig().isLobby() || onlinePlayers.isEmpty()) {
            return;
        }

        plugin.getLogger().info(langManager.getMessage("shutdown.initiating"));
        String title = langManager.getMessage("shutdown.title");
        String subtitle = langManager.getMessage("shutdown.subtitle");

        for (Player player : onlinePlayers.values()) {
            player.sendTitle(title, subtitle, 10, 100, 20);
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().warning("关闭等待被中断: " + e.getMessage());
        }

        plugin.getLogger().info(langManager.getMessage("shutdown.transferring_players"));
        for (Player player : onlinePlayers.values()) {
            try {
                CompletableFuture<Boolean> transferFuture = api.transferToLobby(player);
                transferFuture.thenAccept(success -> {
                    if (success) {
                        player.sendMessage(langManager.getMessage("shutdown.transfer_message"));
                    } else {
                        plugin.getLogger().warning(
                                langManager.getMessage("shutdown.transfer_failed",
                                        "player", player.getName(),
                                        "error", "API返回传送失败")
                        );
                        player.kick(langManager.getMessage("shutdown.kick_message"), false);
                    }
                }).exceptionally(throwable -> {
                    plugin.getLogger().error(
                            langManager.getMessage("shutdown.transfer_failed",
                                    "player", player.getName(),
                                    "error", throwable.getMessage())
                    );
                    player.kick(langManager.getMessage("shutdown.kick_message"), false);
                    return null;
                });
            } catch (Exception e) {
                plugin.getLogger().error(
                        langManager.getMessage("shutdown.transfer_failed",
                                "player", player.getName(),
                                "error", e.getMessage())
                );
                player.kick(langManager.getMessage("shutdown.kick_message"), false);
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().warning("传送等待被中断: " + e.getMessage());
        }

        plugin.getLogger().info(langManager.getMessage("shutdown.completed"));
    }
}
