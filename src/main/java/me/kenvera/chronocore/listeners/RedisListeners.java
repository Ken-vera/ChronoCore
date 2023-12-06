package me.kenvera.chronocore.listeners;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.database.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListeners extends JedisPubSub{
    private final ChronoCore plugin;
    private final RedisManager redisManager;
    private final String channelName;

    public RedisListeners(ChronoCore plugin, RedisManager redisManager, String channelName) {
        this.plugin = plugin;
        this.redisManager = redisManager;
        this.channelName = channelName;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(channelName)) {
            System.out.println("Received message on channel " + channel + ":" + message);

            String[] messageParts = message.split("_");
            String messageType = messageParts[0];
            String uuid;

            switch (messageType) {
                case "set":
                    uuid = messageParts[1];
                    String group = messageParts[2];
                    plugin.getChronoLogger().log("[Group] Received group command (set) on " + plugin.getPlayerData().getUser(uuid).getUsername() + " from proxy", false, 1177176160464031875L);
                    plugin.getPlayerData().setGroup(uuid, group);
                    break;
                case "reset":
                    uuid = messageParts[1];
                    plugin.getChronoLogger().log("[Group] Received group command (reset) on " + plugin.getPlayerData().getUser(uuid).getUsername() + " from proxy", false, 1177176160464031875L);
                    plugin.getPlayerData().setGroup(uuid, "default");
                    break;
            }
        }
    }
}
