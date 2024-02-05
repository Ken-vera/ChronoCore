package me.kenvera.chronocore.Handler;

import me.kenvera.chronocord.ChronoCord;
import me.kenvera.chronocore.ChronoCore;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerData {
    private final ChronoCore plugin;
    private static final String MUTED_CRITERIA = "muted > ? OR muted IS NOT NULL";
    private static final String GET_GROUP = "SELECT `group` FROM CNS1_cnplayerdata_1.player_data WHERE uuid=? LIMIT 1";
    private static final String GET_MUTE = "SELECT muted FROM CNS1_cnplayerdata_1.player_data WHERE " + MUTED_CRITERIA + " and uuid = ?";
    private static final String GET_ID = "SELECT uuid FROM CNS1_cnplayerdata_1.player_data WHERE username = ?";
    private static final String GET_USERNAME = "SELECT username FROM CNS1_cnplayerdata_1.player_data WHERE uuid = ?";
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

    public String getUserName(String uuid) throws SQLException {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player != null && player.isOnline()) {
            return player.getName();
        }

        try (Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_USERNAME)) {

            statement.setString(1, uuid);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("username");
            }
        }

        return null;
    }

    public String getPrefix(String group) {
        Group group1 = ChronoCore.getInstance().getLuckPerms().getGroupManager().getGroup(group);
        assert group1 != null;
        CachedMetaData metaData = group1.getCachedData().getMetaData();
        return metaData.getPrefix();
    }

    public User getUser(String uuid) {
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            UserManager userManager = plugin.getLuckPerms().getUserManager();
            try {
                return userManager.loadUser(UUID.fromString(uuid));
            } catch (Exception e) {
                plugin.getLogger().severe("§cError occurred while loading user data for UUID: " + uuid);
                e.printStackTrace(System.out);
                return null;
            }
        }).thenApply(CompletableFuture::join);
        return userFuture.join();
    }

    public String getUUID(String playerName) {
        try (Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ID)) {

            statement.setString(1, playerName);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("uuid");
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return null;
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
            e.printStackTrace(System.out);
        }
        return null;
    }

    public void setGroup(String uuid, String group, String issuer) {
        User user = getUser(uuid);
        if (issuer == null) {
            issuer = "Console";
        }

        Group assignGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
        if (assignGroup != null) {
            if (group.equalsIgnoreCase("default")) {
                String address;
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    address = Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).getAddress()).getHostName();
                } else {
                    address = "N/A";
                }
                user.data().clear(NodeType.INHERITANCE::matches);
                plugin.getLuckPerms().getUserManager().saveUser(user);
                ChronoCord.getInstance().getChronoLogger().logGroup(user.getUsername(),
                        uuid,
                        address,
                        group,
                        "-",
                        getGroup(uuid).replace(",", ", "),
                        issuer,
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getString("server"),
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getLong("group-log-id"));

                Bukkit.getLogger().info("-------------------------------------------");
                Bukkit.getLogger().info("Player: " + user.getUsername());
                Bukkit.getLogger().info("UUID: " + uuid);
                Bukkit.getLogger().info("Ip Address: " + address);
                Bukkit.getLogger().info("Parent Set: " + group);
                Bukkit.getLogger().info("Parent Removed: " + "-");
                Bukkit.getLogger().info("Inherited: " + getGroup(uuid).replace(",", ", "));
                Bukkit.getLogger().info("Issuer: " + "Console");
                Bukkit.getLogger().info("-------------------------------------------");

            } else {
                String address;
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    address = Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).getAddress()).getHostName();
                } else {
                    address = "N/A";
                }
                user.data().clear(NodeType.INHERITANCE::matches);
                user.data().add(InheritanceNode.builder("default").build());
                user.data().add(InheritanceNode.builder(assignGroup).build());
                plugin.getLuckPerms().getUserManager().saveUser(user);
                ChronoCord.getInstance().getChronoLogger().logGroup(user.getUsername(),
                        uuid,
                        address,
                        group,
                        "-",
                        getGroup(uuid).replace(",", ", "),
                        issuer,
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getString("server"),
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getLong("group-log-id"));

                Bukkit.getLogger().info("-------------------------------------------");
                Bukkit.getLogger().info("Player: " + user.getUsername());
                Bukkit.getLogger().info("UUID: " + uuid);
                Bukkit.getLogger().info("Ip Address: " + address);
                Bukkit.getLogger().info("Parent Set: " + group);
                Bukkit.getLogger().info("Parent Removed: " + "-");
                Bukkit.getLogger().info("Inherited: " + getGroup(uuid).replace(",", ", "));
                Bukkit.getLogger().info("Issuer: " + "Console");
                Bukkit.getLogger().info("-------------------------------------------");
            }

        }
    }

    public void addGroup(String uuid, String group, String issuer) {
        User user = getUser(uuid);
        if (issuer == null) {
            issuer = "Console";
        }

        Group assignGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
        if (!isOnGroup(uuid, group)) {
            if (assignGroup != null) {
                String address;
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    address = Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).getAddress()).getHostName();
                } else {
                    address = "N/A";
                }
                user.data().add(InheritanceNode.builder(assignGroup).build());
                plugin.getLuckPerms().getUserManager().saveUser(user);
                ChronoCord.getInstance().getChronoLogger().logGroup(user.getUsername(),
                        uuid,
                        address,
                        group,
                        "-",
                        getGroup(uuid).replace(",", ", "),
                        issuer,
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getString("server"),
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getLong("group-log-id"));

                Bukkit.getLogger().info("-------------------------------------------");
                Bukkit.getLogger().info("Player: " + user.getUsername());
                Bukkit.getLogger().info("UUID: " + uuid);
                Bukkit.getLogger().info("Ip Address: " + address);
                Bukkit.getLogger().info("Parent Added: " + group);
                Bukkit.getLogger().info("Parent Removed: " + "-");
                Bukkit.getLogger().info("Inherited: " + getGroup(uuid).replace(",", ", "));
                Bukkit.getLogger().info("Issuer: " + "Console");
                Bukkit.getLogger().info("-------------------------------------------");
            }
        }
    }

    public void removeGroup(String uuid, String group, String issuer) {
        User user = getUser(uuid);
        if (issuer == null) {
            issuer = "Console";
        }

        Group assignGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
        if (isOnGroup(uuid, group)) {
            if (assignGroup != null) {
                String address;
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    address = Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).getAddress()).getHostName();
                } else {
                    address = "N/A";
                }
                user.data().remove(InheritanceNode.builder(assignGroup).build());
                plugin.getLuckPerms().getUserManager().saveUser(user);
                ChronoCord.getInstance().getChronoLogger().logGroup(user.getUsername(),
                        uuid,
                        address,
                        "-",
                        group,
                        getGroup(uuid).replace(",", ", "),
                        issuer,
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getString("server"),
                        ChronoCore.getInstance().getDataManager().getConfig("config.yml").get().getLong("group-log-id"));

                Bukkit.getLogger().info("-------------------------------------------");
                Bukkit.getLogger().info("Player: " + user.getUsername());
                Bukkit.getLogger().info("UUID: " + uuid);
                Bukkit.getLogger().info("Ip Address: " + address);
                Bukkit.getLogger().info("Parent Added: " + "-");
                Bukkit.getLogger().info("Parent Removed: " + group);
                Bukkit.getLogger().info("Inherited: " + getGroup(uuid).replace(",", ", "));
                Bukkit.getLogger().info("Issuer: " + "Console");
                Bukkit.getLogger().info("-------------------------------------------");
            }
        }
    }

    public boolean isOnGroup(String uuid, String group) {
        User user = getUser(uuid);
        Set<String> inheritedGroups = user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        return inheritedGroups.contains(group);
    }

    public Long getMuted(String uuid) {
        try (Connection connection = plugin.getSqlManager().getConnection();
        PreparedStatement statement = connection.prepareStatement(GET_MUTE)) {

            statement.setLong(1, System.currentTimeMillis());
            statement.setString(2, uuid);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getLong("muted");
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }
}
