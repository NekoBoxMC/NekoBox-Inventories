package es.nekobox.nekoboxinventories.utils;

import es.nekobox.nekoboxinventories.Inventories;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String url;
    private Connection connection;

    public Database(Inventories inventories) {
        this.url = inventories.config.getConfig().getString("MySQL.URL");
    }

    public void connect() {
        int maxReconnectAttempts = 3;
        long reconnectDelay = 5000;

        int attempts = 0;

        while (!isConnected() && attempts < maxReconnectAttempts) {
            try {
                connection = DriverManager.getConnection(url);
                if (isConnected()) {
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                attempts++;
                try {
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during reconnection delay", ie);
                }
            }
        }

        if (attempts >= maxReconnectAttempts) {
            throw new RuntimeException("Failed to connect to the database after " + maxReconnectAttempts + " attempts.");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
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

    public Connection getConnection() {
        return connection;
    }
}