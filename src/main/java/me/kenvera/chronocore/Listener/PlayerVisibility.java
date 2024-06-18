package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerVisibility implements Listener {
    private final ChronoCore plugin;
    public PlayerVisibility() {
        this.plugin = ChronoCore.getInstance();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack slot8 = player.getInventory().getItem(8);

        if (slot8 == null || slot8.getType() == Material.AIR || Objects.requireNonNull(slot8.getItemMeta()).getDisplayName().equals("§c7Visibility : §aON")) {
            ItemStack toggleItem = new ItemStack(Material.REDSTONE, 1);

            ItemMeta itemMeta = toggleItem.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName("§7Visibility : §aON");
            itemMeta.setLore(Collections.singletonList("§7Click to toggle §cPlayers Visibility!"));

            toggleItem.setItemMeta(itemMeta);
            player.getInventory().setItem(8, toggleItem);
        }
    }

    @EventHandler
    public void toggleVisibility(PlayerInteractEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Long cooldown = plugin.getCooldown("visibility", player.getUniqueId());
        int cooldownTime = 3;
        if (itemInHand.getType() != Material.REDSTONE || !itemInHand.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = itemInHand.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasDisplayName() || !itemMeta.hasLore()) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (cooldown != null) {
            player.sendMessage(ChatColor.RED + "This tool is in cooldown " + TimeUnit.MILLISECONDS.toSeconds((cooldownTime * 1000) - (System.currentTimeMillis() - cooldown)));
            return;
        }

        if (itemMeta.getDisplayName().equals("§7Visibility : §aON")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(onlinePlayer);
            }
            itemMeta.setDisplayName("§7Visibility : §cOFF");
            player.sendMessage( plugin.getPrefix() + ChatColor.YELLOW + " You have toggled player visibility: " + ChatColor.RED + "OFF");
            event.setCancelled(true);

        } else if (itemMeta.getDisplayName().equals("§7Visibility : §cOFF")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                player.showPlayer(onlinePlayer);
            }
            itemMeta.setDisplayName("§7Visibility : §aON");
            player.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + " You have toggled player visibility: " + ChatColor.GREEN + "ON");
            event.setCancelled(true);
        }

        itemInHand.setItemMeta(itemMeta);
        plugin.setCooldown("visibility", cooldownTime, player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Bukkit.broadcastMessage(String.valueOf(event.getAction()));
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
                return;
            }

            if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals("§7Visibility : §aON") ||
                    clickedItem.getItemMeta().getDisplayName().equals("§7Visibility : §cOFF")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().hasPermission("staff")) {
            event.setCancelled(true);
        }
    }
}
