package me.kenvera.chronocore;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.kenvera.chronocore.database.DataManager;
import me.kenvera.chronocore.database.RedisManager;
import me.kenvera.chronocore.database.SqlManager;
import me.kenvera.chronocore.hooks.ChronoLogger;
import me.kenvera.chronocore.hooks.DiscordConnection;
import me.kenvera.chronocore.listeners.DiscordListeners;
import me.kenvera.chronocore.listeners.PlayerSession;
import me.kenvera.chronocore.managers.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ChronoCore extends JavaPlugin {
    private final Cache<String, Cache<UUID, Long>> cooldowns = Caffeine.newBuilder().build();
    private SqlManager sqlManager;
    private RedisManager redisManager;
    private PlayerSession playerSession;
    private PlayerData playerData;
    private DataManager dataManager;
    private DiscordConnection discordConnection;
    private DiscordListeners discordListeners;
    private ChronoLogger chronoLogger;

    @Override
    public void onEnable() {
        registerComponents();
        saveDefaultConfig();
        discordConnection.disconnect();
        discordConnection.connect(discordListeners);
        this.getLogger().info("§bChronoCore §ais Enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        redisManager.close();
        sqlManager.closeDataSource();
        discordConnection.disconnect();
        this.getLogger().info("§bChronoCore §cis Disabled!");
    }

    public void registerComponents() {
        dataManager = new DataManager(this);
        sqlManager = new SqlManager(this);
        redisManager = new RedisManager(this);
        playerSession = new PlayerSession(this);
        playerData = new PlayerData(this);
        discordConnection = new DiscordConnection(this);
        discordListeners = new DiscordListeners(this);
        chronoLogger = new ChronoLogger(this);

        getServer().getPluginManager().registerEvents(playerSession, this);
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

    public DiscordConnection getDiscordConnection() {
        return discordConnection;
    }

    public DiscordListeners getDiscordListeners() {
        return discordListeners;
    }
    public ChronoLogger getChronoLogger() {
        return chronoLogger;
    }
}
