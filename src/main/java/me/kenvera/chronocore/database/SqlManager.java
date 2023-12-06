package me.kenvera.chronocore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.kenvera.chronocore.ChronoCore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlManager {
    private final ChronoCore plugin;
    private static HikariDataSource dataSource;

    private static final String CREATE_TABLE_PLAYER_DATA = "CREATE TABLE IF NOT EXISTS CNS1_cnplayerdata_1.player_data (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(18), `group` VARCHAR(18), whitelisted BOOLEAN, first_join LONGTEXT, last_join LONGTEXT)";

    public SqlManager(ChronoCore plugin) {
        this.plugin = plugin;

        String host = plugin.getDataManager().getConfig("config.yml").get().getString("mysql.host");
        int port = plugin.getDataManager().getConfig("config.yml").get().getInt("mysql.port");
        String username = plugin.getDataManager().getConfig("config.yml").get().getString("mysql.username");
        String password = plugin.getDataManager().getConfig("config.yml").get().getString("mysql.password");
        String database = plugin.getDataManager().getConfig("config.yml").get().getString("mysql.database");
        String ssl = plugin.getDataManager().getConfig("config.yml").get().getString("mysql.ssl");

        plugin.getLogger().severe(plugin.getPrefix() + "§eConnection Pool Initiating!");
        HikariConfig config = new HikariConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(10000);
        config.setMaximumPoolSize(20); // Adjust the pool size as needed
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=" + ssl);
        config.setMaxLifetime(30000);
        dataSource = new HikariDataSource(config);

        try {
            Connection connection = dataSource.getConnection();
            closeConnection(connection);
            plugin.getLogger().info(plugin.getPrefix() + "§9Connection Pool Established!");
        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getPrefix() + "§cConnection Pool Initiation Failed!" + e);
            e.printStackTrace();
        }
    }

    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    public int getIdleConnections() {
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            plugin.getLogger().info(plugin.getPrefix() + "§cConnection Pool Closed!");
        }
    }

    public void closeConnection(Connection connection) {
        dataSource.evictConnection(connection);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void loadTables() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE_PLAYER_DATA);
            plugin.getLogger().info(plugin.getPrefix() + "§9Loaded Table " + "'player_data'");
        } catch (SQLException e) {
            plugin.getLogger().severe(plugin.getPrefix() + "§9Database Generation Failed!" + e);
            e.printStackTrace();
        }
    }
}
