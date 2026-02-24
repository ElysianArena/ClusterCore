package cn.ElysianArena.ClusterCore.Nukkit.redis;

import cn.nukkit.Server;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Redis客户端 - Nukkit端
 * Author: NemoCat
 */
public class RedisClient {

    private static final String SERVER_REGISTRY_KEY = "cluster:servers";
    private static final String EVENT_CHANNEL = "cluster:events";

    private final JedisPool jedisPool;
    private final Gson gson;
    private final ExecutorService pubSubExecutor;
    private volatile boolean running = true;

    public RedisClient(String host, int port, String password) {
        this.gson = new Gson();
        this.pubSubExecutor = Executors.newCachedThreadPool();

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxWait(Duration.ofSeconds(2));

        if (password != null && !password.isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000);
        }

        Server.getInstance().getLogger().info("[ClusterCore] Redis客户端已初始化: " + host + ":" + port);
    }

    public String getServerDisplayName(String wdpeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serverJson = jedis.hget(SERVER_REGISTRY_KEY, wdpeId);
            if (serverJson != null) {
                JsonObject serverInfo = gson.fromJson(serverJson, JsonObject.class);
                if (serverInfo.has("displayName")) {
                    return serverInfo.get("displayName").getAsString();
                }
            }
            return wdpeId;
        }
    }

    public void registerServer(String groupId, String wdpeId, String displayName,
                               boolean isOpen, boolean isLobby, int maxPlayers, int currentPlayers) {
        try (Jedis jedis = jedisPool.getResource()) {
            JsonObject serverInfo = new JsonObject();
            serverInfo.addProperty("groupId", groupId);
            serverInfo.addProperty("wdpeId", wdpeId);
            serverInfo.addProperty("displayName", displayName);
            serverInfo.addProperty("open", isOpen);
            serverInfo.addProperty("lobby", isLobby);
            serverInfo.addProperty("maxPlayers", maxPlayers);
            serverInfo.addProperty("currentPlayers", currentPlayers);
            serverInfo.addProperty("online", true);
            serverInfo.addProperty("lastHeartbeat", System.currentTimeMillis());

            jedis.hset(SERVER_REGISTRY_KEY, wdpeId, serverInfo.toString());
            jedis.publish(EVENT_CHANNEL, "register:" + wdpeId);

            Server.getInstance().getLogger().info("[ClusterCore] 成功注册到Redis: " + wdpeId + " (" + displayName + ")");
        }
    }

    public void sendHeartbeat(String wdpeId, int currentPlayers, int maxPlayers, boolean isOpen) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serverJson = jedis.hget(SERVER_REGISTRY_KEY, wdpeId);
            if (serverJson != null) {
                JsonObject serverInfo = gson.fromJson(serverJson, JsonObject.class);
                serverInfo.addProperty("currentPlayers", currentPlayers);
                serverInfo.addProperty("maxPlayers", maxPlayers);
                serverInfo.addProperty("open", isOpen);
                serverInfo.addProperty("online", true);
                serverInfo.addProperty("lastHeartbeat", System.currentTimeMillis());

                jedis.hset(SERVER_REGISTRY_KEY, wdpeId, serverInfo.toString());
            }
        }
    }

    public void unregisterServer(String wdpeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(SERVER_REGISTRY_KEY, wdpeId);
            jedis.publish(EVENT_CHANNEL, "unregister:" + wdpeId);
        }
    }

    public JsonObject queryAvailableServers(String groupId) {
        try (Jedis jedis = jedisPool.getResource()) {
            var allServers = jedis.hgetAll(SERVER_REGISTRY_KEY);
            JsonObject result = new JsonObject();
            int count = 0;

            for (var entry : allServers.entrySet()) {
                JsonObject serverInfo = gson.fromJson(entry.getValue(), JsonObject.class);
                if (groupId.equals(serverInfo.get("groupId").getAsString()) &&
                        serverInfo.get("online").getAsBoolean()) {
                    result.add("server_" + count, serverInfo);
                    count++;
                }
            }

            result.addProperty("count", count);
            return result;
        }
    }

    public void subscribeToEvents(Consumer<String> messageHandler) {
        pubSubExecutor.submit(() -> {
            while (running) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            messageHandler.accept(message);
                        }
                    }, EVENT_CHANNEL);
                } catch (Exception e) {
                    if (running) {
                        Server.getInstance().getLogger().error("[ClusterCore] Redis订阅连接断开，正在重连...", e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        });
    }

    public Jedis getResource() {
        return jedisPool.getResource();
    }

    public boolean testConnection() {
        try (Jedis jedis = jedisPool.getResource()) {
            return "PONG".equals(jedis.ping());
        }
    }

    public void shutdown() {
        running = false;
        pubSubExecutor.shutdownNow();
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}