package cn.ElysianArena.ClusterCore.Nukkit.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.ElysianArena.ClusterCore.Nukkit.api.ClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;

/**
 * /hub 命令处理类 - 返回大厅服务器
 * Author: NemoCat
 */
public class HubCommand extends Command {

    private final ClusterAPI api;
    private final LangManager langManager;

    public HubCommand(ClusterAPI api) {
        super("hub", "返回大厅服务器", "/hub");
        this.api = api;
        this.langManager = LangManager.getInstance();
        this.getCommandParameters().clear();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(langManager.getMessage("hub.player_only"));
            return false;
        }

        Player player = (Player) sender;

        if (api.isLobbyServer()) {
            player.teleport(player.getLevel().getSpawnLocation());
            player.sendMessage(langManager.getMessage("hub.teleport_spawn"));
            return true;
        }

        player.sendMessage(langManager.getMessage("hub.teleporting_lobby"));

        api.transferToLobby(player).thenAccept(success -> {
            if (!success) {
                player.sendMessage(langManager.getMessage("hub.no_lobby_found"));
            }
        });

        return true;
    }
}
