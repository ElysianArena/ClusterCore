package cn.ElysianArena.ClusterCore.WaterdogPE.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 子服信息数据类
 * Author: NemoCat
 */
public class ServerInfo {

    private final String groupId;
    private final String wdpeId;
    private final String displayName;
    private final String address;
    private final int port;
    private final AtomicBoolean isOpen;
    private final AtomicBoolean isLobby;
    private final AtomicInteger maxPlayers;
    private final AtomicInteger currentPlayers;
    private volatile long lastHeartbeat;
    private volatile boolean online;

    public ServerInfo(String groupId, String wdpeId, String displayName, String address, int port,
                      boolean isOpen, boolean isLobby, int maxPlayers, int currentPlayers) {
        this.groupId = groupId;
        this.wdpeId = wdpeId;
        this.displayName = displayName;
        this.address = address;
        this.port = port;
        this.isOpen = new AtomicBoolean(isOpen);
        this.isLobby = new AtomicBoolean(isLobby);
        this.maxPlayers = new AtomicInteger(maxPlayers);
        this.currentPlayers = new AtomicInteger(currentPlayers);
        this.lastHeartbeat = System.currentTimeMillis();
        this.online = true;
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

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isOpen() {
        return isOpen.get();
    }

    public void setOpen(boolean open) {
        this.isOpen.set(open);
    }

    public boolean isLobby() {
        return isLobby.get();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers.set(maxPlayers);
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers.set(currentPlayers);
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
        this.online = true;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * 检查子服是否可用（在线、开放、未满）
     */
    public boolean isAvailable() {
        return online && isOpen.get() && currentPlayers.get() < maxPlayers.get();
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "groupId='" + groupId + '\'' +
                ", wdpeId='" + wdpeId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", isOpen=" + isOpen.get() +
                ", maxPlayers=" + maxPlayers.get() +
                ", currentPlayers=" + currentPlayers.get() +
                ", online=" + online +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}