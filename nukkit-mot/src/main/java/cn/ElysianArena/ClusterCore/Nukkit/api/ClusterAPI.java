package cn.ElysianArena.ClusterCore.Nukkit.api;

import cn.nukkit.Player;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * ClusterCore API接口
 * Author: NemoCat
 */
public interface ClusterAPI {

    /**
     * 设置当前子服的开放状态
     * @param open 是否开放
     */
    void setServerOpen(boolean open);

    /**
     * 获取当前子服的开放状态
     * @return 是否开放
     */
    boolean isServerOpen();

    /**
     * 获取指定分组下所有可用子服的信息
     * @param groupId 分组ID
     * @return 异步返回子服信息列表的JSON
     */
    CompletableFuture<JsonObject> getAvailableServers(String groupId);

    /**
     * 强制传送玩家到指定wdpeId的子服
     * @param player 玩家
     * @param wdpeId 目标子服ID
     * @return 是否成功发起传送
     */
    boolean transferToServer(Player player, String wdpeId);

    /**
     * 传送玩家到指定分组下的一个可用子服
     * @param player 玩家
     * @param groupId 分组ID
     * @return 异步返回是否成功传送
     */
    CompletableFuture<Boolean> transferToGroup(Player player, String groupId);

    /**
     * 获取当前服务器的groupId
     * @return groupId
     */
    String getGroupId();

    /**
     * 获取当前服务器的wdpeId
     * @return wdpeId
     */
    String getWdpeId();

    /**
     * 判断当前服务器是否为大厅服务器
     * @return 是否为大厅服务器
     */
    boolean isLobbyServer();

    /**
     * 传送玩家到大厅服务器
     * @param player 玩家
     * @return 异步返回是否成功传送
     */
    CompletableFuture<Boolean> transferToLobby(Player player);

    /**
     * 获取指定服务器的显示名称
     * @param wdpeId 服务器ID
     * @return 服务器显示名称
     */
    CompletableFuture<String> getServerDisplayName(String wdpeId);
}