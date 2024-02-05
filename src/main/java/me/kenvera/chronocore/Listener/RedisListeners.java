package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Database.RedisManager;
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
            if (messageParts.length <= 3) {
                String messageType = messageParts[0];
                String uuid = messageParts[1];
                String group = messageParts[2];

                switch (messageType) {
                    case "set" -> {
                        plugin.getPlayerData().setGroup(uuid, group, null);
                    }
                    case "reset" -> {
                        plugin.getPlayerData().setGroup(uuid, "default", null);
                    }
                    case "add" -> {
                        plugin.getPlayerData().addGroup(uuid, group, null);
                        System.out.println("add");
                    }
                    case "remove" -> {
                        plugin.getPlayerData().removeGroup(uuid, group, null);
                        System.out.println("remove");
                    }
                }
            }
        }
    }
}
