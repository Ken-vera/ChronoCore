package me.kenvera.chronocore.Command;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Exception.GroupAdditionException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GroupCommand implements CommandExecutor {
    private final ChronoCore plugin;
    public GroupCommand(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("§cThis command can only be executed on console!");
            return false;
        }

        System.out.println(Thread.currentThread().getName());
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String group = args[2];
                    try {
                        Player player = Bukkit.getPlayer(args[1]);
                        assert player != null;
                        UUID uuid = player.getUniqueId();
                        plugin.getGroupHandler().addGroup(uuid, group, null, true);
                        System.out.println(Thread.currentThread().getName());
                    } catch (GroupAdditionException e) {
                        Bukkit.getLogger().severe(e.getMessage());
                    }
//                List<String> groups = plugin.getLuckPerms().getGroupManager().getLoadedGroups()
//                        .stream()
//                        .map(Group::getName).distinct().toList();
//
//                if (groups.contains(group)) {
//                    Player player = Bukkit.getPlayer(args[1]);
//                    assert player != null;
//                    String playerName = player.getName();
//                    UUID uuid = player.getUniqueId();
//
//                    plugin.getGroupHandler().addGroup(uuid, playerName, group, null, true);
//                    return true;
//                } else {
//                    Bukkit.getLogger().severe("Group " + group + " is not a valid luckperms group!");
//                }
                } else {
                    Bukkit.getLogger().severe("Command argument is not valid!");
                }
            }
        }, plugin.getGroupHandler().getExecutorService());
        return false;
    }
}
