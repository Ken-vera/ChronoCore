package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Exception.GroupAdditionException;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class PlayerSession implements Listener {
    private final ChronoCore plugin;
    private final ExecutorService executorService;
    private final String[] vanillaBlacklist = {"novice", "apprentice", "expert", "veteran", "elder", "vip", "mvp"};
    public PlayerSession() {
        this.plugin = ChronoCore.getInstance();
        this.executorService = plugin.getExecutorService();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataHandler().loadData(player.getUniqueId());
        CompletableFuture.runAsync(() -> {
            UUID uuid = player.getUniqueId();
            User user = plugin.getLuckPerms().getUserManager().getUser(uuid);
            assert user != null;
            Set<String> inheritedGroups = user.getNodes(NodeType.INHERITANCE).stream()
                    .map(InheritanceNode::getGroupName)
                    .collect(Collectors.toSet());
            try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                List<String> groupDataRedis = jedis.lrange("group::" + player.getName(), 0, -1);
                for (String group : groupDataRedis) {
                    if (!inheritedGroups.contains(group)) {
                        try {
                            if (plugin.getDataManager().getConfig("config.yml").get().getString("server").equalsIgnoreCase("vanilla")) {
                                for (String blacklisted : vanillaBlacklist) {
                                    if (!group.equals(blacklisted)) {
                                        plugin.getGroupHandler().addGroup(uuid, group, null, false);
                                    }
                                }
                            }
                            plugin.getGroupHandler().addGroup(uuid, group, null, false);
                        } catch (GroupAdditionException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
                for (String group : inheritedGroups) {
                    if (!groupDataRedis.contains(group) && !group.equalsIgnoreCase("default")) {
                        try {
                            plugin.getGroupHandler().removeGroup(uuid, group, null, false);
                            Bukkit.getLogger().warning("Found invalid group, removing " + group + " from " + player.getName() + ":" + uuid);
                        } catch (GroupAdditionException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
        }, executorService);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (Objects.requireNonNull(plugin.getDataManager().getConfig("config.yml").get().getString("server")).equalsIgnoreCase("lobby")) {
            if (event.getInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
            }
        }
    }
}
