package me.kenvera.chronocore.Handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Exception.DataNotLoadedException;
import me.kenvera.chronocore.Object.PlayerData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PlayerDataHandler {
    private final ChronoCore plugin;
    private final ExecutorService executorService;
    private final Cache<UUID, PlayerData> playerDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private final List<UUID> loading = new CopyOnWriteArrayList<>();
    public PlayerDataHandler() {
        this.plugin = ChronoCore.getInstance();
        this.executorService = plugin.getExecutorService();
    }

    public void loadData(UUID uuid) {
        if (loading.contains(uuid)) {
            return;
        }

        loading.add(uuid);
        CompletableFuture<PlayerData> loadDataFuture = loadData(uuid, Bukkit.getOfflinePlayer(uuid.toString()).getName(), false);

        loadDataFuture.thenAcceptAsync(playerData -> {
            playerDataCache.put(uuid, playerData);
            loading.remove(uuid);
        }).exceptionally(ex -> {
            ex.printStackTrace(System.out);
            loading.remove(uuid);
            return null;
        });
    }

    public CompletableFuture<PlayerData> loadData(UUID uuid, String playerName, boolean storeCache) {
        return CompletableFuture.supplyAsync(() -> {
                    PlayerData data = new PlayerData(uuid);
                    String sql = "SELECT * FROM player_data WHERE uuid = ?";
                    try (Connection connection = plugin.getSqlManager().getConnection();
                         PreparedStatement statement = connection.prepareStatement(sql)) {

                        statement.setString(1, uuid.toString());
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            data.setPlayerName(resultSet.getString("username"));
//                    data.setAddress(resultSet.getString("address"));
                            data.setInheritedGroup(resultSet.getString("group"));
                        }
                    } catch (SQLException e) {
                        Bukkit.getLogger().severe("Player data loading failed " + e.getMessage());
                        e.printStackTrace(System.out);
                        return null;
                    }
                    if (storeCache) {
                        playerDataCache.put(uuid, data);
                    }
                    return data;
                }, plugin.getExecutorService())
                .exceptionally(ex -> {
                    Bukkit.getLogger().severe("Player data loading failed " + ex.getMessage());
                    ex.printStackTrace(System.out);
                    return null;
                });
    }

    public PlayerData getLoadedData(UUID uuid) throws DataNotLoadedException {
        PlayerData data = playerDataCache.getIfPresent(uuid);
        if (data == null) {
            throw new DataNotLoadedException("Data for " + uuid + " is not loaded yet!");
        }
        return data;
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid, String playerName, boolean wait) {
        if (wait) {
            try {
                PlayerData data = loadData(uuid, playerName, false).get(5, TimeUnit.SECONDS);
                return CompletableFuture.completedFuture(data);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace(System.out);
                return null;
            }
        } else {
            try {
                PlayerData data = getLoadedData(uuid);
                return CompletableFuture.completedFuture(data);
            } catch (DataNotLoadedException e) {
                return loadData(uuid, playerName, true)
                        .exceptionally((ex) -> {
                            ex.printStackTrace();
                            return null;
                        });
            }
        }
    }

    public void invalidateCache(UUID uuid) {
        playerDataCache.invalidate(uuid);
    }

    public User getUserLuckPerms(UUID uuid) {
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            UserManager userManager = plugin.getLuckPerms().getUserManager();
            try {
                return userManager.loadUser(uuid);
            } catch (Exception e) {
                plugin.getLogger().severe("§cError occurred while loading user data for UUID: " + uuid.toString());
                e.printStackTrace(System.out);
                return null;
            }
        }).thenApply(CompletableFuture::join);
        return userFuture.join();
    }

    public boolean isInheritGroup(UUID uuid, String group) {
        User user = getUserLuckPerms(uuid);
        return user.getNodes(NodeType.INHERITANCE).stream()
                .anyMatch(node -> node.getGroupName().equals(group));
    }
}
