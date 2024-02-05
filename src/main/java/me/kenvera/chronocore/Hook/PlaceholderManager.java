package me.kenvera.chronocore.Hook;

import com.google.common.base.Joiner;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kenvera.chronocore.ChronoCore;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlaceholderManager extends PlaceholderExpansion {
    private final ChronoCore plugin;
    public PlaceholderManager(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "reputation";
    }

    @Override
    public @NotNull String getAuthor() {
        return Joiner.on(", ").join(this.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        try {
            return String.valueOf(getPlayerReputation(player.getUniqueId(), params));
        } catch (SQLException e) {
            e.printStackTrace();
            return "0";
        }
    }

    public int getPlayerReputation(UUID playerUUID, String reputationType) throws SQLException {
        try (Connection connection = plugin.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + reputationType + " FROM player_reputation WHERE player_uuid=?")) {

            statement.setString(1, playerUUID.toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(reputationType);
            }
        }
        return 0;
    }
}
