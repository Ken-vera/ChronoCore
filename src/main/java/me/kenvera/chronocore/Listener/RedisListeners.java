package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Exception.GroupAdditionException;
import org.bukkit.Bukkit;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class RedisListeners extends JedisPubSub{
    private final ChronoCore plugin;
    private final String channelName;
    private final ExecutorService executorService;

    public RedisListeners(String channelName) {
        this.plugin = ChronoCore.getInstance();
        this.channelName = channelName;
        this.executorService = plugin.getExecutorService();
    }

    @Override
    public void onMessage(String channel, String message) {
        CompletableFuture.runAsync(() -> {
            Bukkit.getLogger().warning("Received message on channel: " + channel + ":" + message);
            if (channel.equals(channelName)) {
                JSONObject jsonObject = new JSONObject(message);

                String action = jsonObject.optString("action");
                UUID uuid = UUID.fromString(jsonObject.optString("uuid"));
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                String group = jsonObject.optString("group", null);
                String issuer = jsonObject.optString("issuer");
                String server = jsonObject.optString("server");
                boolean isServer = Objects.equals(plugin.getDataManager().getConfig("config.yml").get().getString("server"), server);

                if (!isServer) {
                    try {
                        switch (action) {
                            case "add" -> plugin.getGroupHandler().addGroup(uuid, group, issuer, false);

                            case "remove" -> plugin.getGroupHandler().removeGroup(uuid, group, issuer, false);

                            case "set" -> plugin.getGroupHandler().setGroup(uuid, group, issuer, false);

                            case "reset" -> plugin.getGroupHandler().resetGroup(uuid, issuer, false);
                        }
                    } catch (GroupAdditionException e) {
                        plugin.getLogger().severe(e.getMessage());
                    }
                }
            }
        }, executorService);
    }
}
