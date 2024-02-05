package me.kenvera.chronocore;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.kenvera.chronocore.Command.DebugCommand;
import me.kenvera.chronocore.Command.GroupCommand;
import me.kenvera.chronocore.Database.DataManager;
import me.kenvera.chronocore.Database.RedisManager;
import me.kenvera.chronocore.Database.SqlManager;
import me.kenvera.chronocore.Handler.BanHandler;
import me.kenvera.chronocore.Handler.MuteHandler;
import me.kenvera.chronocore.Hook.PlaceholderManager;
import me.kenvera.chronocore.Listener.ChatListener;
import me.kenvera.chronocore.Listener.PlayerSession;
import me.kenvera.chronocore.Handler.PlayerData;
import me.kenvera.chronocore.Object.Mute;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ChronoCore extends JavaPlugin {
    public static ChronoCore instance;
    private final Cache<String, Cache<UUID, Long>> cooldowns = Caffeine.newBuilder().build();
    private SqlManager sqlManager;
    private RedisManager redisManager;
    private PlayerSession playerSession;
    private PlayerData playerData;
    private DataManager dataManager;
    private BanHandler banHandler;
    private MuteHandler muteHandler;

    @Override
    public void onEnable() {
        instance = this;
        registerComponents();
        saveDefaultConfig();
        this.getLogger().info("§bChronoCore §ais Enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        redisManager.close();
        sqlManager.closeDataSource();
        this.getLogger().info("§bChronoCore §cis Disabled!");
    }

    public void registerComponents() {
        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        dataManager = new DataManager(this);
        sqlManager = new SqlManager(this);
        redisManager = new RedisManager(this);
        playerSession = new PlayerSession(this);
        playerData = new PlayerData(this);
        banHandler = new BanHandler();
        muteHandler = new MuteHandler();
        ChatListener chatListener = new ChatListener(this);

        getServer().getPluginManager().registerEvents(playerSession, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        Objects.requireNonNull(getCommand("group")).setExecutor(new GroupCommand(this));
        Objects.requireNonNull(getCommand("debugc")).setExecutor(new DebugCommand(this));

        if (placeholderApi != null) {
            new PlaceholderManager(this).register();
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

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static ChronoCore getInstance() {
        return instance;
    }

    public BanHandler getBanHandler() {
        return banHandler;
    }

    public MuteHandler getMuteHandler() {
        return muteHandler;
    }

    public String getToken() {
        try (Connection connection = sqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT discord FROM CNS1_cnplayerdata_1.token LIMIT 1")) {
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("discord");
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public String getRedisChannel() {
        return dataManager.getConfig("config.yml").get().getString("redis.channel");
    }
}
