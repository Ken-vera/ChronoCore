package me.kenvera.chronocore.Database;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Listener.RedisListeners;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisManager {
    private final JedisPool jedisPool;
    private JedisPubSub jedisPubSub;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final String channelName;

    public RedisManager() {
        ChronoCore plugin = ChronoCore.getInstance();
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
        String username = plugin.getDataManager().getConfig("config.yml").get().getString("redis.username");
        String password = plugin.getDataManager().getConfig("config.yml").get().getString("redis.password");
        channelName = plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel");

        if (username == null) {
            username = "default";
        }

        this.jedisPool = new JedisPool(jedisPoolConfig, host, port, 5000, username, password, 0);
        subscribe(channelName);
    }

    public void subscribe(String channelName) {
        executorService.execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedisPubSub = new RedisListeners(channelName);
                jedis.subscribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
    }

    public void publish(String channel, String message) {
        executorService.execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel, message);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
    }

    public void publish(String channel, Map<String, String> keyValues) {
        executorService.execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                StringBuilder jsonString = new StringBuilder("{");

                for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                    jsonString.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
                }

                if (jsonString.length() > 1) {
                    jsonString.setLength(jsonString.length() - 1);
                }

                jsonString.append("}");
                jedis.publish(channel, jsonString.toString());
            } catch (Exception e) {
                e.printStackTrace(System.err);
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

    public JedisPool getJedis() {
        return jedisPool;
    }

    public String getKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public String getChannel() {
        return channelName;
    }
}
