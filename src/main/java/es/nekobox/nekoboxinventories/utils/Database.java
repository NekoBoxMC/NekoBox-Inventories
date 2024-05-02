package es.nekobox.nekoboxinventories.utils;

import es.nekobox.nekoboxinventories.Inventories;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private final HikariDataSource dataSource;

    public Database(Inventories inventories) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(inventories.config.getConfig().getString("MySQL.URL"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }

    public void connect() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                throw new RuntimeException("Failed to connect to the database.");
            }
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}