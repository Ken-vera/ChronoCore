package me.kenvera.chronocore.hooks;

import me.kenvera.chronocore.ChronoCore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordConnection {
    public JDA jda;
    private final ChronoCore plugin;
    private static final String TOKEN = "SELECT discord FROM CNS1_cnplayerdata_1.token LIMIT 1";

    public DiscordConnection(ChronoCore plugin) {
        this.plugin = plugin;
    }

    public void connect(ListenerAdapter listener) {
        try (Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(TOKEN)) {

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String token = result.getString("discord");
                plugin.getLogger().warning("§eEstablishing Discord Connection!");

                try {
                    jda = JDABuilder.createDefault(token)
                            .enableIntents(
                                    GatewayIntent.DIRECT_MESSAGES,
                                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                                    GatewayIntent.DIRECT_MESSAGE_TYPING,
                                    GatewayIntent.GUILD_MESSAGES,
                                    GatewayIntent.GUILD_MESSAGE_TYPING,
                                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                                    GatewayIntent.GUILD_MEMBERS,
                                    GatewayIntent.GUILD_WEBHOOKS,
                                    GatewayIntent.GUILD_PRESENCES,
                                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                            .addEventListeners(listener)
                            .build();

                    jda.awaitReady();
                    plugin.getLogger().info("§9Connected to Discord!");
                } catch (Exception e) {
                    plugin.getLogger().severe("§cDiscord Connection Failed!");
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().severe("§cToken is not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (jda != null) {
            jda.shutdownNow();
            plugin.getLogger().info("§cDisconnected from Discord!");
        }
    }

    public TextChannel getTextChannel(Long id) {
        return jda.getTextChannelById(id);
    }

}
