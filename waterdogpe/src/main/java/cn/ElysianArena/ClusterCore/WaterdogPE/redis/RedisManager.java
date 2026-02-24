package cn.ElysianArena.ClusterCore.WaterdogPE.redis;

import dev.waterdog.waterdogpe.logger.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Redis连接管理器
 * Author: NemoCat
 */
public class RedisManager {

    private static final String SERVER_REGISTRY_KEY = "cluster:servers";
    private static final String HEARTBEAT_CHANNEL = "cluster:heartbeat";
    private static final String EVENT_CHANNEL = "cluster:events";

    private final JedisPool jedisPool;
    private final Logger logger;
    private final ExecutorService pubSubExecutor;
    private volatile boolean running = true;

    public RedisManager(String host, int port, String password, Logger logger) {
        this.logger = logger;
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

        logger.info("Redis连接池已初始化: " + host + ":" + port);
    }

    public Jedis getResource() {
        return jedisPool.getResource();
    }

    public void subscribeToEvents(Consumer<String> messageHandler) {
        pubSubExecutor.submit(() -> {
            while (running) {
                try (Jedis jedis = getResource()) {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            messageHandler.accept(message);
                        }
                    }, EVENT_CHANNEL, HEARTBEAT_CHANNEL);
                } catch (Exception e) {
                    if (running) {
                        logger.error("Redis订阅连接断开，正在重连...", e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        });
    }

    public String getServerRegistryKey() {
        return SERVER_REGISTRY_KEY;
    }

    public void shutdown() {
        running = false;
        pubSubExecutor.shutdownNow();
        if (jedisPool != null) {
            jedisPool.close();
        }
        logger.info("Redis连接池已关闭");
    }

    public boolean testConnection() {
        try (Jedis jedis = getResource()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            logger.error("Redis连接测试失败", e);
            return false;
        }
    }
}