package es.nekobox.nekoboxinventories.utils;

import es.nekobox.nekoboxinventories.Inventories;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String url;

    private Connection connection;

    public Database(Inventories inventories) {
        this.url = inventories.config.getConfig().getString("MySQL.URL");;
    }

    public void connect() {
        try {
            if (!isConnected()) {
                connection = DriverManager.getConnection(url);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Connection getConnection(){
        return connection;
    }
}