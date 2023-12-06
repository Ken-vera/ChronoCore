package me.kenvera.chronocore.managers;

import me.kenvera.chronocore.ChronoCore;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerData {
    private final ChronoCore plugin;
    private static final String GET_GROUP = "SELECT `group` FROM CNS1_cnplayerdata_1.player_data WHERE uuid=? LIMIT 1";
    public PlayerData(ChronoCore plugin) {
        this.plugin = plugin;
    }

    public String getPrefix(UUID uuid, boolean formatted) {
        User user = plugin.getLuckPerms().getUserManager().getUser(uuid);
        assert user != null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        if (formatted) {
            return Objects.requireNonNull(metaData.getPrefix()).replaceAll("&", "§");
        } else {
            return Objects.requireNonNull(metaData.getPrefix()).replace("&.", "");
        }
    }

    public User getUser(String uuid) {
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            UserManager userManager = plugin.getLuckPerms().getUserManager();
            try {
                return userManager.loadUser(UUID.fromString(uuid));
            } catch (Exception e) {
                plugin.getLogger().severe("§cError occurred while loading user data for UUID: " + uuid);
                e.printStackTrace();
                return null;
            }
        }).thenApply(CompletableFuture::join);
        return userFuture.join();
    }


    public String getGroup(String uuid) {
        try (Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_GROUP)) {

            statement.setString(1, uuid);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("group");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setGroup(String uuid, String group) {
        User user = getUser(uuid);
        String currentGroup = user.getPrimaryGroup();

        Group assignGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
        if (!isOnGroup(uuid, group)) {
            if (assignGroup != null) {
                String address;
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    address = Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).getAddress()).getHostName();
                } else {
                    address = "N/A";
                }
                user.data().clear(NodeType.INHERITANCE::matches);
                user.data().add(InheritanceNode.builder(assignGroup).build());
                plugin.getLuckPerms().getUserManager().saveUser(user);
                plugin.getChronoLogger().log("[Group] Group update for player: " + user.getUsername(), true, 1177176160464031875L);
                plugin.getChronoLogger().log("[Group] UUID: " + uuid, true, 1177176160464031875L);
                plugin.getChronoLogger().log("[Group] Ip address: " + address, true, 1177176160464031875L);
                plugin.getChronoLogger().log("[Group] Parent: ~~" + currentGroup + "~~ > " + group, true, 1177176160464031875L);
            }
        }

    }

    public boolean isOnGroup(String uuid, String group) {
        User user = getUser(uuid);
        return user.getPrimaryGroup().equalsIgnoreCase(group);
    }
}
