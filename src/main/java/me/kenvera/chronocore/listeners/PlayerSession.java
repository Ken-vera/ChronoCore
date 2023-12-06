package me.kenvera.chronocore.listeners;

import me.kenvera.chronocore.ChronoCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerSession implements Listener {
    private final ChronoCore plugin;
    public PlayerSession(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String group = plugin.getPlayerData().getGroup(uuid.toString());
        plugin.getPlayerData().setGroup(uuid.toString(), group);
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
