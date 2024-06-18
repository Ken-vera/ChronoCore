package me.kenvera.chronocore;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kenvera.chronocore.Command.GroupCommand;
import me.kenvera.chronocore.Database.DataManager;
import me.kenvera.chronocore.Database.RedisManager;
import me.kenvera.chronocore.Database.SqlManager;
import me.kenvera.chronocore.Handler.*;
import me.kenvera.chronocore.Hook.PlaceholderManager;
import me.kenvera.chronocore.Listener.ChatListener;
import me.kenvera.chronocore.Listener.PlayerSession;
import me.kenvera.chronocore.Listener.PlayerVisibility;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ChronoCore extends JavaPlugin implements Listener {
    public static ChronoCore instance;
    private final Cache<String, Cache<UUID, Long>> cooldowns = Caffeine.newBuilder().build();
    private static ExecutorService executorService;
    private SqlManager sqlManager;
    private RedisManager redisManager;
    private GroupHandler playerData;
    private DataManager dataManager;
    private BanHandler banHandler;
    private MuteHandler muteHandler;
    private GroupHandlerOld groupHandler;
    private PlayerDataHandler playerDataHandler;

    @Override
    public void onEnable() {
        instance = this;
        executorService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ChronoCoreMain").build());
        registerComponents();
        saveDefaultConfig();
        this.getLogger().info("\u001b[38;2;85;255;255mChronoCore \u001b[92mis Enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        redisManager.close();
        sqlManager.closeDataSource();
        groupHandler.getExecutorService().shutdown();
        executorService.shutdown();
        this.getLogger().info("§bChronoCore §cis Disabled!");
    }

    public void registerComponents() {
        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        dataManager = new DataManager(this);
        sqlManager = new SqlManager(this);
        redisManager = new RedisManager();
//        PlayerSession playerSession = new PlayerSession();
        playerData = new GroupHandler(this);
        banHandler = new BanHandler();
        muteHandler = new MuteHandler();
        playerDataHandler = new PlayerDataHandler();
        ChatListener chatListener = new ChatListener(this);

//        getServer().getPluginManager().registerEvents(playerSession, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(this, this);
//        Objects.requireNonNull(getCommand("group")).setExecutor(new GroupCommand(this));
//        Objects.requireNonNull(getCommand("debugc")).setExecutor(new DebugCommand(this));

        if (placeholderApi != null) {
            new PlaceholderManager(this).register();
        }

        if (Objects.requireNonNull(getDataManager().getConfig("config.yml").get().getString("server")).equalsIgnoreCase("lobby")) {
            PlayerVisibility playerVisibility = new PlayerVisibility();
        }
    }

    public void setCooldown(String cooldownType, int cooldownTime, UUID uuid) {
        Cache<UUID, Long> newCooldownMap = Caffeine.newBuilder()
                .expireAfterWrite(cooldownTime, TimeUnit.SECONDS)
                .build();
        newCooldownMap.put(uuid, System.currentTimeMillis());
        cooldowns.put(cooldownType, newCooldownMap);
    }

    public Long getCooldown(String cooldownType, UUID uuid) {
        Cache<UUID, Long> cooldownMap = cooldowns.getIfPresent(cooldownType);

        if (cooldownMap != null) {
            return cooldownMap.getIfPresent(uuid);
        }
        return null;
    }

    public void resetCooldown(String cooldownType, UUID uuid) {
        Cache<UUID, Long> cooldownMap = cooldowns.getIfPresent(cooldownType);

        if (cooldownMap != null) {
            cooldownMap.invalidate(uuid);
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("ChronoCord")) {
            groupHandler = new GroupHandlerOld();
        }
    }

    public CompletableFuture<String> getToken() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = sqlManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT discord FROM token LIMIT 1")) {
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    return result.getString("discord");
                }
            } catch (SQLException e) {
                e.printStackTrace(System.out);
            }
            return null;
        }, executorService);
    }

    public String getPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.GREEN + "Crazy " + ChatColor.WHITE + " Network" + ChatColor.GRAY + "]";
    }

    public LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }

    public GroupHandler getPlayerData() {
        return playerData;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public BanHandler getBanHandler() {
        return banHandler;
    }

    public MuteHandler getMuteHandler() {
        return muteHandler;
    }

    public GroupHandlerOld getGroupHandler() {
        return groupHandler;
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static ChronoCore getInstance() {
        return instance;
    }
}
