package cn.ElysianArena.ClusterCore.Nukkit.api;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.TransferPacket;
import cn.nukkit.utils.TextFormat;
import com.google.gson.JsonObject;
import cn.ElysianArena.ClusterCore.Nukkit.redis.RedisClient;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于Redis的集群API实现
 * Author: NemoCat
 */
public class RedisClusterAPI implements ClusterAPI {

    private final RedisClient redisClient;
    private final String currentGroupId;
    private final String currentWdpeId;
    private final boolean isLobbyServer;
    private final AtomicBoolean serverOpen;
    private final LangManager langManager;

    public RedisClusterAPI(RedisClient redisClient, String groupId, String wdpeId, boolean isLobby) {
        this.redisClient = redisClient;
        this.currentGroupId = groupId;
        this.currentWdpeId = wdpeId;
        this.isLobbyServer = isLobby;
        this.serverOpen = new AtomicBoolean(true);
        this.langManager = LangManager.getInstance();
    }

    @Override
    public void setServerOpen(boolean open) {
        this.serverOpen.set(open);
        int currentPlayers = Server.getInstance().getOnlinePlayers().size();
        redisClient.sendHeartbeat(currentWdpeId, currentPlayers,
                                 Server.getInstance().getMaxPlayers(), open);
    }

    @Override
    public boolean isServerOpen() {
        return serverOpen.get();
    }

    @Override
    public CompletableFuture<JsonObject> getAvailableServers(String groupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject servers = redisClient.queryAvailableServers(groupId);
                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                result.add("servers", servers);

                return result;
            } catch (Exception e) {
                Server.getInstance().getLogger().error("[ClusterCore] 查询服务器失败", e);
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", e.getMessage());
                return error;
            }
        });
    }

    @Override
    public boolean transferToServer(Player player, String wdpeId) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        TransferPacket transferPacket = new TransferPacket();
        transferPacket.address = wdpeId;
        transferPacket.port = 0;
        player.dataPacket(transferPacket);
        getServerDisplayName(wdpeId).thenAccept(displayName -> {
            player.getServer().getScheduler().scheduleTask(
                Server.getInstance().getPluginManager().getPlugin("ClusterCore"), 
                () -> {
                    player.sendMessage(langManager.getMessage("transfer.sending_to_server", "serverName", displayName));
                    String debugMsg = langManager.getOpMessage(player, "debug.transfer_packet_sent", "server", wdpeId);
                    if (!debugMsg.isEmpty()) {
                        player.sendMessage(debugMsg);
                    }
                }
            );
        });
        return true;
    }

    @Override
    public CompletableFuture<Boolean> transferToGroup(Player player, String groupId) {
        if (player == null || !player.isOnline()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject servers = redisClient.queryAvailableServers(groupId);

                if (servers.get("count").getAsInt() == 0) {
                    player.getServer().getScheduler().scheduleTask(
                        Server.getInstance().getPluginManager().getPlugin("ClusterCore"), 
                        () -> {
                            player.sendMessage(langManager.getMessage("error.no_target_servers"));
                            String debugMsg = langManager.getOpMessage(player, "debug.no_available_servers");
                            if (!debugMsg.isEmpty()) {
                                player.sendMessage(debugMsg);
                            }
                        }
                    );
                    return false;
                }

                JsonObject targetServer = servers.get("server_0").getAsJsonObject();
                String wdpeId = targetServer.get("wdpeId").getAsString();
                player.getServer().getScheduler().scheduleTask(
                    Server.getInstance().getPluginManager().getPlugin("ClusterCore"), 
                    () -> {
                        TransferPacket transferPacket = new TransferPacket();
                        transferPacket.address = wdpeId;
                        transferPacket.port = 0;
                        player.dataPacket(transferPacket);
                        getServerDisplayName(wdpeId).thenAccept(displayName -> {
                            player.getServer().getScheduler().scheduleTask(
                                Server.getInstance().getPluginManager().getPlugin("ClusterCore"), 
                                () -> {
                                    player.sendMessage(langManager.getMessage("transfer.sending_to_server", "serverName", displayName));
                                    String debugMsg = langManager.getOpMessage(player, "debug.transfer_packet_sent", "server", wdpeId);
                                    if (!debugMsg.isEmpty()) {
                                        player.sendMessage(debugMsg);
                                    }
                                }
                            );
                        });
                    }
                );
                
                return true;
            } catch (Exception e) {
                Server.getInstance().getLogger().error("[ClusterCore] 传送失败", e);
                player.getServer().getScheduler().scheduleTask(
                    Server.getInstance().getPluginManager().getPlugin("ClusterCore"), 
                    () -> {
                        player.sendMessage(langManager.getMessage("error.transfer_failed"));
                        String errorMsg = langManager.getOpMessage(player, "debug.transfer_failed_detail", "error", e.getMessage());
                        if (!errorMsg.isEmpty()) {
                            player.sendMessage(errorMsg);
                        }
                    }
                );
                return false;
            }
        });
    }

    @Override
    public String getGroupId() {
        return currentGroupId;
    }

    @Override
    public String getWdpeId() {
        return currentWdpeId;
    }

    @Override
    public boolean isLobbyServer() {
        return isLobbyServer;
    }

    @Override
    public CompletableFuture<String> getServerDisplayName(String wdpeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return redisClient.getServerDisplayName(wdpeId);
            } catch (Exception e) {
                Server.getInstance().getLogger().warning(
                    "[ClusterCore] 获取服务器显示名称失败: " + wdpeId, e);
                return wdpeId;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> transferToLobby(Player player) {
        if (player == null || !player.isOnline()) {
            return CompletableFuture.completedFuture(false);
        }

        if (isLobbyServer) {
            player.teleport(player.getLevel().getSpawnLocation());
            return CompletableFuture.completedFuture(true);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject lobbyServers = redisClient.queryAvailableServers("lobby");

                if (lobbyServers.get("count").getAsInt() == 0) {
                    player.sendMessage(langManager.getMessage("error.no_lobby_servers"));
                    String debugMsg = langManager.getOpMessage(player, "debug.no_available_servers");
                    if (!debugMsg.isEmpty()) {
                        player.sendMessage(debugMsg);
                    }
                    return false;
                }

                JsonObject targetServer = lobbyServers.get("server_0").getAsJsonObject();
                String wdpeId = targetServer.get("wdpeId").getAsString();
                return transferToServer(player, wdpeId);
            } catch (Exception e) {
                Server.getInstance().getLogger().error("[ClusterCore] 传送至大厅失败", e);
                player.sendMessage(langManager.getMessage("error.transfer_to_lobby_failed"));
                String errorMsg = langManager.getOpMessage(player, "debug.transfer_failed_detail", "error", e.getMessage());
                if (!errorMsg.isEmpty()) {
                    player.sendMessage(errorMsg);
                }
                return false;
            }
        });
    }
}
