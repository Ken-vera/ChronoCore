package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerSession implements Listener {
    private final ChronoCore plugin;
    public PlayerSession(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        User user = plugin.getLuckPerms().getUserManager().getUser(uuid);
        Set<String> inheritedGroups = user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        List<String> groupData = List.of(plugin.getPlayerData().getGroup(uuid.toString()).split(","));

        for (String group : groupData) {
            if (!inheritedGroups.contains(group)) {
                plugin.getPlayerData().addGroup(uuid.toString(), group, null);
            }
        }

        for (String group : inheritedGroups) {
            if (!groupData.contains(group) && !group.equalsIgnoreCase("default")) {
                System.out.println(groupData.contains(group));
                    plugin.getPlayerData().setGroup(uuid.toString(), "default", null);
            }
        }

//        String group = plugin.getPlayerData().getGroup(uuid.toString());
//        plugin.getPlayerData().setGroup(uuid.toString(), group, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer().getServer().getName().equalsIgnoreCase("lobby") || event.getPlayer().getServer().getName().equalsIgnoreCase("hub")) {
            if (event.getPlayer() instanceof Player) {
                if (event.getInventory().getType() == InventoryType.PLAYER) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
