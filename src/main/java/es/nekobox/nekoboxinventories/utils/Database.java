package es.nekobox.nekoboxinventories.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String url;

    public Database() {
        this.url = "jdbc:mysql://u8_fNFPenGv7B:jQ!P%3Du26yqU%40%40%40NH%40MVsBneN@136.243.146.219:3306/s8_Inventories";
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}