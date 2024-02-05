package me.kenvera.chronocore.Handler;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Object.Ban;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BanHandler {
    private static final String BANNED = "purged = 0 and ((expire > ?) or (expire = -1))";
    private static final String GET_BAN = "SELECT id, reason, issuer, expire, banned_time FROM CNS1_cnplayerdata_1.ban WHERE " + BANNED + " and uuid = ? LIMIT 1";
    public Ban getBan(String uuid) throws SQLException {
        try (Connection connection = ChronoCore.getInstance().getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BAN)) {

            statement.setLong(1, System.currentTimeMillis());
            statement.setString(2, uuid);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                long expireMillis = result.getLong("expire");
                long bannedTimeMillis = result.getLong("banned_time");

                return new Ban(
                        result.getLong("id"),
                        uuid,
                        result.getString("issuer"),
                        result.getString("reason"),
                        expireMillis,
                        bannedTimeMillis);
            } else {
                return null;
            }
        }
    }

    public boolean isBanned(String uuid) {
        try {
            return getBan(uuid) != null;
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return false;
    }
}
