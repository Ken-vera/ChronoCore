package me.kenvera.chronocore.Command;

import me.kenvera.chronocore.ChronoCore;
import net.luckperms.api.model.group.Group;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 3) {
                String group = args[2];
                List<String> groups = plugin.getLuckPerms().getGroupManager().getLoadedGroups()
                        .stream()
                        .map(Group::getName).distinct().toList();

                if (groups.contains(group)) {
                    String player = args[1];
                    String uuid = plugin.getPlayerData().getUUID(player);

                    //"set_" + uuid + "_" + group
                    plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), "add_" + uuid + "_" + group + "_" + "proxy");
                    plugin.getPlayerData().addGroup(uuid, group, null);
                } else {
                    sender.sendMessage("§cGroup §7" + group + " §cis not a valid luckperms group!");
                }
            } else {
                sender.sendMessage("§cCommand argument is not valid!");
            }
        }
        // group set <player> <group>
        return false;
    }
}
