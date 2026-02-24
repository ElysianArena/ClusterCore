package cn.ElysianArena.ClusterCore.Nukkit.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.google.gson.JsonObject;
import cn.ElysianArena.ClusterCore.Nukkit.Main;
import cn.ElysianArena.ClusterCore.Nukkit.api.ClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;

import java.util.concurrent.CompletableFuture;

/**
 * /wdt 命令处理类
 * Author: NemoCat
 */
public class WDTCommand extends Command {

    private final ClusterAPI api;
    private final LangManager langManager;

    public WDTCommand(ClusterAPI api) {
        super("wdt", "传送到指定分组的子服", "/wdt <groupId>");
        this.api = api;
        this.langManager = LangManager.getInstance();
        this.setPermission("clustercore.wdt");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(langManager.getMessage("wdt.player_only"));
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(langManager.getMessage("wdt.usage"));
            player.sendMessage(langManager.getMessage("wdt.description"));
            return false;
        }

        String groupId = args[0];
        CompletableFuture<JsonObject> future = api.getAvailableServers(groupId);
        future.whenComplete((response, throwable) -> {
            player.getServer().getScheduler().scheduleTask(Main.getInstance(), () -> {
                try {
                    if (throwable != null) {
                        player.sendMessage(langManager.getMessage("wdt.query_error", "error", throwable.getMessage()));
                        throwable.printStackTrace();
                        return;
                    }
                    
                    if (response == null) {
                        player.sendMessage(langManager.getMessage("wdt.connection_failed"));
                        return;
                    }

                    boolean success = response.has("success") && response.get("success").getAsBoolean();

                    if (success && response.has("servers")) {
                        JsonObject serversObj = response.getAsJsonObject("servers");
                        int count = serversObj.get("count").getAsInt();

                        if (count == 0) {
                            player.sendMessage(langManager.getMessage("wdt.no_servers", "groupId", groupId));
                            return;
                        }

                        if (player.isOp()) {
                            player.sendMessage(langManager.getMessage("wdt.server_list_header", "groupId", groupId));
                            for (int i = 0; i < count; i++) {
                                JsonObject server = serversObj.getAsJsonObject("server_" + i);
                                String wdpeId = server.get("wdpeId").getAsString();
                                int current = server.get("currentPlayers").getAsInt();
                                int max = server.get("maxPlayers").getAsInt();
                                boolean isOpen = server.get("open").getAsBoolean();
                                String status = isOpen ? langManager.getMessage("server.open") : langManager.getMessage("server.closed");
                                player.sendMessage(langManager.getMessage("wdt.server_format", 
                                    "wdpeId", wdpeId, 
                                    "current", current, 
                                    "max", max,
                                    "status", status));
                            }
                        }

                        api.transferToGroup(player, groupId);

                    } else {
                        String message = response.has("message") ? response.get("message").getAsString() : "未知错误";
                        player.sendMessage(langManager.getMessage("wdt.query_failed", "message", message));
                    }
                } catch (Exception e) {
                    player.sendMessage(langManager.getMessage("wdt.process_error", "error", e.getMessage()));
                    e.printStackTrace();
                }
            });
        }).exceptionally(throwable -> {
            player.getServer().getScheduler().scheduleTask(Main.getInstance(), () -> {
                player.sendMessage(langManager.getMessage("wdt.exception", "error", throwable.getMessage()));
                throwable.printStackTrace();
            });
            return null;
        });
        return true;
    }
}