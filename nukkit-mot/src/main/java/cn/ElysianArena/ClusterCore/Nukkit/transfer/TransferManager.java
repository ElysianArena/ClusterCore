package cn.ElysianArena.ClusterCore.Nukkit.transfer;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.TransferPacket;
import cn.ElysianArena.ClusterCore.Nukkit.Main;
import cn.ElysianArena.ClusterCore.Nukkit.api.ClusterAPI;
import cn.ElysianArena.ClusterCore.Nukkit.lang.LangManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 传送管理类
 * Author: NemoCat
 */
public class TransferManager {
    private final Map<String, String> serverAddresses = new ConcurrentHashMap<>();
    private final LangManager langManager = LangManager.getInstance();
    private final ClusterAPI api = Main.getInstance().getAPI();

    /**
     * 传送玩家到指定子服
     * @param player 玩家
     * @param wdpeId 目标子服ID
     * @param address 目标子服地址（IP:Port格式）
     */
    public void transferPlayer(Player player, String wdpeId, String address) {
        if (player == null || !player.isOnline()) {
            return;
        }

        String[] parts = address.split(":");
        if (parts.length != 2) {
            player.sendMessage(langManager.getMessage("error.invalid_server_address"));
            String debugMsg = langManager.getOpMessage(player, "debug.invalid_address_format", "address", address);
            if (!debugMsg.isEmpty()) {
                player.sendMessage(debugMsg);
            }
            return;
        }

        String host = parts[0];
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessage("error.invalid_port"));
            String debugMsg = langManager.getOpMessage(player, "debug.port_parse_failed", "port", parts[1]);
            if (!debugMsg.isEmpty()) {
                player.sendMessage(debugMsg);
            }
            return;
        }

        serverAddresses.put(wdpeId, address);
        transferPlayer(player, host, port);
        api.getServerDisplayName(wdpeId).thenAccept(displayName -> {
            player.getServer().getScheduler().scheduleTask(Main.getInstance(), () -> {
                player.sendMessage(langManager.getMessage("transfer.sending_to_server", "serverName", displayName));
                
                String debugMsg = langManager.getOpMessage(player, "debug.transfer_packet_sent", "server", wdpeId);
                if (!debugMsg.isEmpty()) {
                    player.sendMessage(debugMsg);
                }
            });
        });
    }

    /**
     * 传送玩家到指定地址和端口
     * @param player 玩家
     * @param address 目标地址
     * @param port 目标端口
     */
    public void transferPlayer(Player player, String address, int port) {
        TransferPacket transferPacket = new TransferPacket();
        transferPacket.address = address;
        transferPacket.port = port;
        player.dataPacket(transferPacket);
    }

    /**
     * 使用wdpeId传送（仅用于WaterdogPE代理服务器）
     * @param player 玩家
     * @param wdpeId 目标子服ID
     */
    public void transferPlayerByWdpeId(Player player, String wdpeId) {
        if (player == null || !player.isOnline()) {
            return;
        }

        TransferPacket transferPacket = new TransferPacket();
        transferPacket.address = wdpeId;
        transferPacket.port = 0;
        player.dataPacket(transferPacket);

        api.getServerDisplayName(wdpeId).thenAccept(displayName -> {
            player.getServer().getScheduler().scheduleTask(Main.getInstance(), () -> {
                player.sendMessage(langManager.getMessage("transfer.sending_to_server", "serverName", displayName));
                
                String debugMsg = langManager.getOpMessage(player, "debug.transfer_packet_sent", "server", wdpeId);
                if (!debugMsg.isEmpty()) {
                    player.sendMessage(debugMsg);
                }
            });
        });
    }

    public String getCachedAddress(String wdpeId) {
        return serverAddresses.get(wdpeId);
    }
    public void clearCache() {
        serverAddresses.clear();
    }
}