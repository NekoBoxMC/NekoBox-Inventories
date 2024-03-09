package es.nekobox.nekoboxinventories.utils;

import es.nekobox.nekoboxinventories.Inventories;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String url;

    public Database(Inventories inventories) {
        this.url = inventories.config.getConfig().getString("MySQL.URL");;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}