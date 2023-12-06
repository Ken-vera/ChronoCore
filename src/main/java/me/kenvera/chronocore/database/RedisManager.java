package me.kenvera.chronocore.database;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.listeners.RedisListeners;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisManager {
    private final ChronoCore plugin;
    private JedisPool jedisPool;
    private JedisPubSub jedisPubSub;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public RedisManager(ChronoCore plugin) {
        this.plugin = plugin;
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(2);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        jedisPoolConfig.setBlockWhenExhausted(false);

        String host = plugin.getDataManager().getConfig("config.yml").get().getString("redis.host");
        int port = plugin.getDataManager().getConfig("config.yml").get().getInt("redis.port");
        String password = plugin.getDataManager().getConfig("config.yml").get().getString("redis.password");
        String channelName = plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel");

        this.jedisPool = new JedisPool(jedisPoolConfig, host, port, 5000, password, 0);
        subscribe(channelName);
    }

    public void subscribe(String channelName) {
        executorService.execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedisPubSub = new RedisListeners(plugin, this, channelName);
                jedis.subscribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void unSubscribe() {
        if (jedisPubSub != null) {
            jedisPubSub.unsubscribe();
            jedisPubSub = null;
        }
    }

    public void close() {
        unSubscribe();
        jedisPool.close();
        executorService.shutdown();
    }
}
