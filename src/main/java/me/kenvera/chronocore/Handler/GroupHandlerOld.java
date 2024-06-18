package me.kenvera.chronocore.Handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kenvera.chronocord.ChronoCord;
import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Exception.GroupAdditionException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupHandlerOld {
    private final ChronoCore plugin;
    private final ChronoCord chronoCord;
    private final LuckPerms luckPerms;
    private static ExecutorService executorService;

    public GroupHandlerOld() {
        plugin = ChronoCore.getInstance();
        chronoCord = ChronoCord.getInstance();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        luckPerms = plugin.getLuckPerms();
        executorService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ChronoCoreGroupThread").build());
    }

    public void addGroup(UUID uuid, String group, String issuer, boolean global) throws GroupAdditionException {
        if (luckPerms.getGroupManager().getGroup(group) == null) {
            throw new GroupAdditionException("Group " + group + " is invalid!");
        }

        if (isInherit(uuid, group)) {
            throw new GroupAdditionException("Player " + Bukkit.getOfflinePlayer(uuid).getName() + " already inherited group " + group);
        }

        String username = Bukkit.getOfflinePlayer(uuid).getName();
        if (Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
            Player player = Bukkit.getPlayer(username);

            try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                User user = luckPerms.getUserManager().getUser(uuid);
                Group assignedGroup = luckPerms.getGroupManager().getGroup(group);

                assert user != null;
                assert assignedGroup != null;
                user.data().add(InheritanceNode.builder(assignedGroup).build());

                luckPerms.getUserManager().saveUser(user);
                List<String>inheritedGroupsRedis = jedis.lrange("group::" + username, 0, -1);

                logGroupTask(username, uuid, null, (issuer != null) ? issuer : "Console", group, "-", inheritedGroupsRedis.toString());
                assert player != null;
                logGroupTaskDiscord(username, uuid, Objects.requireNonNull(player.getAddress()).getHostName(), group, "-", inheritedGroupsRedis.toString().substring(1, inheritedGroupsRedis.toString().length() - 1), (issuer != null) ? issuer : "Console");

                if (global) {
                    Map<String, String> keyValues = Map.of(
                            "action", "add",
                            "uuid", uuid.toString(),
                            "group", group,
                            "issuer", (issuer != null) ? issuer : "Console",
                            "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
                            "global", "true"
                    );
                    plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
                }
            }
        }
    }

    public void removeGroup(UUID uuid, String group, String issuer, boolean global) throws GroupAdditionException {
        if (luckPerms.getGroupManager().getGroup(group) == null) {
            throw new GroupAdditionException("Group " + group + " is invalid!");
        }

        if (!isInherit(uuid, group)) {
            throw new GroupAdditionException("Player " + Bukkit.getOfflinePlayer(uuid).getName() + " doesn't inherited group " + group);
        }

        String username = Bukkit.getOfflinePlayer(uuid).getName();
        if (Bukkit.getPlayer(uuid).isOnline()) {
            Player player = Bukkit.getPlayer(username);

            try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                User user = luckPerms.getUserManager().getUser(uuid);
                Group assignedGroup = luckPerms.getGroupManager().getGroup(group);

                assert user != null;
                assert assignedGroup != null;
                user.data().remove(InheritanceNode.builder(assignedGroup).build());

                luckPerms.getUserManager().saveUser(user);
                List<String>inheritedGroupsRedis = jedis.lrange("group::" + username, 0, -1);

                logGroupTask(username, uuid, null, (issuer != null) ? issuer : "Console", "-", group, inheritedGroupsRedis.toString());
                logGroupTaskDiscord(username, uuid, player.getAddress().getHostName(), "-", group, inheritedGroupsRedis.toString().substring(1, inheritedGroupsRedis.toString().length() - 1), (issuer != null) ? issuer : "Console");

                if (global) {
                    Map<String, String> keyValues = Map.of(
                            "action", "remove",
                            "uuid", uuid.toString(),
                            "group", group,
                            "issuer", (issuer != null) ? issuer : "Console",
                            "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
                            "global", "true"
                    );
                    plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
                }
            }
        }
    }

    public void setGroup(UUID uuid, String group, String issuer, boolean global) throws GroupAdditionException {
        if (luckPerms.getGroupManager().getGroup(group) == null) {
            throw new GroupAdditionException("Group " + group + " is invalid!");
        }

        if (isInherit(uuid, group)) {
            throw new GroupAdditionException("Player " + Bukkit.getOfflinePlayer(uuid).getName() + " already inherited group " + group);
        }

        String username = Bukkit.getOfflinePlayer(uuid).getName();
        if (Bukkit.getPlayer(uuid).isOnline()) {
            Player player = Bukkit.getPlayer(username);

            try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                User user = luckPerms.getUserManager().getUser(uuid);
                Group assignedGroup = luckPerms.getGroupManager().getGroup(group);
                Group defaultGroup = luckPerms.getGroupManager().getGroup("default");

                assert user != null;
                assert assignedGroup != null;
                user.data().clear(NodeType.INHERITANCE::matches);
                user.data().add(InheritanceNode.builder(defaultGroup).build());
                user.data().add(InheritanceNode.builder(assignedGroup).build());

                luckPerms.getUserManager().saveUser(user);
                List<String>inheritedGroupsRedis = jedis.lrange("group::" + username, 0, -1);

                logGroupTask(username, uuid, null, (issuer != null) ? issuer : "Console", group, "*", inheritedGroupsRedis.toString());
                logGroupTaskDiscord(username, uuid, player.getAddress().getHostName(), group, "*", inheritedGroupsRedis.toString().substring(1, inheritedGroupsRedis.toString().length() - 1), (issuer != null) ? issuer : "Console");

                if (global) {
                    Map<String, String> keyValues = Map.of(
                            "action", "set",
                            "uuid", uuid.toString(),
                            "group", group,
                            "issuer", (issuer != null) ? issuer : "Console",
                            "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
                            "global", "true"
                    );
                    plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
                }
            }
        }
    }

    public void resetGroup(UUID uuid, String issuer, boolean global) throws GroupAdditionException {
        String username = Bukkit.getOfflinePlayer(uuid).getName();
        if (Bukkit.getPlayer(uuid).isOnline()) {
            Player player = Bukkit.getPlayer(username);

            try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                User user = luckPerms.getUserManager().getUser(uuid);
                Group assignedGroup = luckPerms.getGroupManager().getGroup("default");

                assert user != null;
                assert assignedGroup != null;
                user.data().clear(NodeType.INHERITANCE::matches);
                user.data().add(InheritanceNode.builder(assignedGroup).build());

                luckPerms.getUserManager().saveUser(user);
                List<String>inheritedGroupsRedis = jedis.lrange("group::" + username, 0, -1);

                logGroupTask(username, uuid, null, (issuer != null) ? issuer : "Console", "-", "*", inheritedGroupsRedis.toString());
                logGroupTaskDiscord(username, uuid, player.getAddress().getHostName(), "-", "*", inheritedGroupsRedis.toString().substring(1, inheritedGroupsRedis.toString().length() - 1), (issuer != null) ? issuer : "Console");

                if (global) {
                    Map<String, String> keyValues = Map.of(
                            "action", "reset",
                            "uuid", uuid.toString(),
                            "issuer", (issuer != null) ? issuer : "Console",
                            "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
                            "global", "true"
                    );
                    plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
                }
            }
        }
    }

//    public CompletableFuture<Void> addGroup(UUID uuid, String playerName, String group, String issuer, boolean global) {
//        return playerDataHandler.getPlayerData(uuid, playerName, true)
//                .thenAcceptAsync(playerData -> {
//                    if (playerData != null) {
//                        String address;
//                        User user = playerDataHandler.getUserLuckPerms(uuid);
//                        String issuerString = (issuer != null) ? issuer : "Console";
//                        Group assignedGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
//                        if (!playerDataHandler.isInheritGroup(uuid, group)) {
//                            if (assignedGroup != null) {
//                                if (Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
//                                    address = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getPlayer()).getAddress()).getHostName();
//                                } else {
//                                    address = "N/A";
//                                }
//
//                                Set<String> inheritedGroups = user.getNodes(NodeType.INHERITANCE).stream()
//                                        .map(InheritanceNode::getGroupName)
//                                        .collect(Collectors.toSet());
//
//                                inheritedGroups.add(group);
//                                String inheritedGroup = String.join(", ", inheritedGroups);
//                                chronoCord.getChronoLogger().logGroup(playerName,
//                                        uuid.toString(),
//                                        address,
//                                        group,
//                                        "-",
//                                        inheritedGroup,
//                                        issuerString,
//                                        plugin.getDataManager().getConfig("config.yml").get().getString("server"),
//                                        plugin.getDataManager().getConfig("config.yml").get().getLong("group-log-id"));
//
//                                user.data().add(InheritanceNode.builder(assignedGroup).build());
//                                plugin.getLuckPerms().getUserManager().saveUser(user);
//
//                                Bukkit.getLogger().info("-------------------------------------------");
//                                Bukkit.getLogger().info("Player: " + playerName);
//                                Bukkit.getLogger().info("UUID: " + uuid);
//                                Bukkit.getLogger().info("Ip Address: " + address);
//                                Bukkit.getLogger().info("Parent Added: " + group);
//                                Bukkit.getLogger().info("Parent Removed: " + "-");
//                                Bukkit.getLogger().info("Inherited: " + inheritedGroup);
//                                Bukkit.getLogger().info("Issuer: " + "Console");
//                                Bukkit.getLogger().info("-------------------------------------------");
//                            }
//                        }
//                    }
//                }, executorService)
//                .thenRunAsync(() -> {
//                    playerDataHandler.invalidateCache(uuid);
//                    if (global) {
//                        Map<String, String> keyValues = Map.of(
//                            "action", "add",
//                                "uuid", uuid.toString(),
//                                "group", group,
//                                "issuer", (issuer != null) ? issuer : "Console",
//                                "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
//                                "global", "true"
//                        );
//                        plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
//                    }
//                }, executorService)
//                .exceptionally(ex -> {
//                    Bukkit.getLogger().severe("Failed to execute group add [" + group + "] for " + playerName + " " + uuid.toString());
//                    ex.printStackTrace(System.err);
//                    return null;
//                });
//    }

//    public CompletableFuture<Void> removeGroup(UUID uuid, String playerName, String group, String issuer, boolean global) {
//        return playerDataHandler.getPlayerData(uuid, playerName, true)
//                .thenAcceptAsync(playerData -> {
//                    if (playerData != null) {
//                        String address;
//                        User user = playerDataHandler.getUserLuckPerms(uuid);
//                        String issuerString = (issuer != null) ? issuer : "Console";
//                        Group assignedGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
//                        if (!playerDataHandler.isInheritGroup(uuid, group)) {
//                            if (assignedGroup != null) {
//                                if (Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
//                                    address = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getPlayer()).getAddress()).getHostName();
//                                } else {
//                                    address = "N/A";
//                                }
//
//                                String[] inheritedGroups = playerData.getInheritedGroup().split(", ");
//                                List<String> inheritedGroupList = new ArrayList<>(Arrays.asList(inheritedGroups));
//                                inheritedGroupList.remove(group);
//                                String inheritedGroup = String.join(", ", inheritedGroupList);
//                                chronoCord.getChronoLogger().logGroup(playerName,
//                                        uuid.toString(),
//                                        address,
//                                        "-",
//                                        group,
//                                        inheritedGroup,
//                                        issuerString,
//                                        plugin.getDataManager().getConfig("config.yml").get().getString("server"),
//                                        plugin.getDataManager().getConfig("config.yml").get().getLong("group-log-id"));
//
//                                user.data().remove(InheritanceNode.builder(assignedGroup).build());
//                                plugin.getLuckPerms().getUserManager().saveUser(user);
//
//                                Bukkit.getLogger().info("-------------------------------------------");
//                                Bukkit.getLogger().info("Player: " + playerName);
//                                Bukkit.getLogger().info("UUID: " + uuid);
//                                Bukkit.getLogger().info("Ip Address: " + address);
//                                Bukkit.getLogger().info("Parent Added: " + "-");
//                                Bukkit.getLogger().info("Parent Removed: " + group);
//                                Bukkit.getLogger().info("Inherited: " + inheritedGroup);
//                                Bukkit.getLogger().info("Issuer: " + issuerString);
//                                Bukkit.getLogger().info("-------------------------------------------");
//                            }
//                        }
//                    }
//                }, executorService)
//                .thenRunAsync(() -> {
//                    playerDataHandler.invalidateCache(uuid);
//                    if (global) {
//                        Map<String, String> keyValues = Map.of(
//                                "action", "remove",
//                                "uuid", uuid.toString(),
//                                "group", group,
//                                "issuer", (issuer != null) ? issuer : "Console",
//                                "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
//                                "global", "true"
//                        );
//                        plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
//                    }
//                }, executorService)
//                .exceptionally(ex -> {
//                    Bukkit.getLogger().severe("Failed to execute group remove [" + group + "] for " + playerName + " " + uuid.toString());
//                    ex.printStackTrace(System.err);
//                    return null;
//                });
//    }

//    public CompletableFuture<Void> setGroup(UUID uuid, String playerName, String group, String issuer, boolean global) {
//        return playerDataHandler.getPlayerData(uuid, playerName, true)
//                .thenAcceptAsync(playerData -> {
//                    if (playerData != null) {
//                        String address;
//                        User user = playerDataHandler.getUserLuckPerms(uuid);
//                        String issuerString = (issuer != null) ? issuer : "Console";
//                        Group assignedGroup = plugin.getLuckPerms().getGroupManager().getGroup(group);
//                        if (assignedGroup != null) {
//                            if (Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
//                                address = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getPlayer()).getAddress()).getHostName();
//                            } else {
//                                address = "N/A";
//                            }
//
//                            List<String> inheritedGroupList = new ArrayList<>();
//                            inheritedGroupList.add("default");
//                            inheritedGroupList.add(group);
//                            String inheritedGroup = String.join(", ", inheritedGroupList);
//                            chronoCord.getChronoLogger().logGroup(playerName,
//                                    uuid.toString(),
//                                    address,
//                                    group + " [set]",
//                                    "-",
//                                    inheritedGroup,
//                                    issuerString,
//                                    plugin.getDataManager().getConfig("config.yml").get().getString("server"),
//                                    plugin.getDataManager().getConfig("config.yml").get().getLong("group-log-id"));
//
//                            user.data().clear(NodeType.INHERITANCE::matches);
//                            user.data().add(InheritanceNode.builder("default").build());
//                            user.data().add(InheritanceNode.builder(assignedGroup).build());
//                            plugin.getLuckPerms().getUserManager().saveUser(user);
//
//                            Bukkit.getLogger().info("-------------------------------------------");
//                            Bukkit.getLogger().info("Player: " + playerName);
//                            Bukkit.getLogger().info("UUID: " + uuid);
//                            Bukkit.getLogger().info("Ip Address: " + address);
//                            Bukkit.getLogger().info("Parent Added: " + group + " [set]");
//                            Bukkit.getLogger().info("Parent Removed: " + "-");
//                            Bukkit.getLogger().info("Inherited: " + inheritedGroup);
//                            Bukkit.getLogger().info("Issuer: " + "Console");
//                            Bukkit.getLogger().info("-------------------------------------------");
//                        }
//                    }
//                }, executorService)
//                .thenRunAsync(() -> {
//                    playerDataHandler.invalidateCache(uuid);
//                    if (global) {
//                        Map<String, String> keyValues = Map.of(
//                                "action", "set",
//                                "uuid", uuid.toString(),
//                                "group", group,
//                                "issuer", (issuer != null) ? issuer : "Console",
//                                "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
//                                "global", "true"
//                        );
//                        plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
//                    }
//                }, executorService)
//                .exceptionally(ex -> {
//                    Bukkit.getLogger().severe("Failed to execute group set [" + group + "] for " + playerName + " " + uuid.toString());
//                    ex.printStackTrace(System.err);
//                    return null;
//                });
//    }

//    public CompletableFuture<Void> resetGroup(UUID uuid, String playerName, String issuer, boolean global) {
//        return playerDataHandler.getPlayerData(uuid, playerName, true)
//                .thenAcceptAsync(playerData -> {
//                    if (playerData != null) {
//                        String address;
//                        User user = playerDataHandler.getUserLuckPerms(uuid);
//                        String issuerString = (issuer != null) ? issuer : "Console";
//                        if (Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
//                            address = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getPlayer()).getAddress()).getHostName();
//                        } else {
//                            address = "N/A";
//                        }
//
//                        String inheritedGroup = "default";
//                        chronoCord.getChronoLogger().logGroup(playerName,
//                                uuid.toString(),
//                                address,
//                                "-",
//                                "[reset]",
//                                inheritedGroup,
//                                issuerString,
//                                plugin.getDataManager().getConfig("config.yml").get().getString("server"),
//                                plugin.getDataManager().getConfig("config.yml").get().getLong("group-log-id"));
//
//                        user.data().clear(NodeType.INHERITANCE::matches);
//                        user.data().add(InheritanceNode.builder("default").build());
//                        plugin.getLuckPerms().getUserManager().saveUser(user);
//
//                        Bukkit.getLogger().info("-------------------------------------------");
//                        Bukkit.getLogger().info("Player: " + playerName);
//                        Bukkit.getLogger().info("UUID: " + uuid);
//                        Bukkit.getLogger().info("Ip Address: " + address);
//                        Bukkit.getLogger().info("Parent Added: " + "-");
//                        Bukkit.getLogger().info("Parent Removed: " + "[reset]");
//                        Bukkit.getLogger().info("Inherited: " + inheritedGroup);
//                        Bukkit.getLogger().info("Issuer: " + "Console");
//                        Bukkit.getLogger().info("-------------------------------------------");
//                    }
//                }, executorService)
//                .thenRunAsync(() -> {
//                    playerDataHandler.invalidateCache(uuid);
//                    if (global) {
//                        Map<String, String> keyValues = Map.of(
//                                "action", "reset",
//                                "uuid", uuid.toString(),
//                                "group", "default",
//                                "issuer", (issuer != null) ? issuer : "Console",
//                                "server", plugin.getDataManager().getConfig("config.yml").get().getString("server", "null"),
//                                "global", "true"
//                        );
//                        plugin.getRedisManager().publish(plugin.getDataManager().getConfig("config.yml").get().getString("redis.channel"), keyValues);
//                    }
//                }, executorService)
//                .exceptionally(ex -> {
//                    Bukkit.getLogger().severe("Failed to execute group reset for " + playerName + " " + uuid.toString());
//                    ex.printStackTrace(System.err);
//                    return null;
//                });
//    }

    private void logGroupTaskDiscord(String username, UUID uuid, String address, String groupAdded, String groupRemoved, String inheritedGroup, String issuer) {
        chronoCord.getChronoLogger().logGroup(username,
                uuid.toString(),
                address,
                groupAdded,
                groupRemoved,
                inheritedGroup,
                issuer,
                plugin.getDataManager().getConfig("config.yml").get().getString("server"),
                plugin.getDataManager().getConfig("config.yml").get().getLong("group-log-id"));
    }

    private void logGroupTask(String username, UUID uuid, String address, String issuer, String addedGroup, String removedGroup, String inheritedGroup) {
        plugin.getLogger().info("-------------------------------------------");
        plugin.getLogger().info("Player: " + username);
        plugin.getLogger().info("UUID: " + uuid);
        plugin.getLogger().info("Ip Address: " + (address != null ? address : "N/A"));
        plugin.getLogger().info("Parent Added: " + addedGroup);
        plugin.getLogger().info("Parent Removed: " + removedGroup);
        plugin.getLogger().info("Inherited: " + inheritedGroup);
        plugin.getLogger().info("Issuer: " + (issuer != null ? issuer : "Console"));
        plugin.getLogger().info("-------------------------------------------");
    }

    private boolean isInherit(UUID uuid, String group) {
        User user = plugin.getPlayerDataHandler().getUserLuckPerms(uuid);
        return user.getNodes(NodeType.INHERITANCE).stream()
                .anyMatch(node -> node.getGroupName().equals(group));
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
