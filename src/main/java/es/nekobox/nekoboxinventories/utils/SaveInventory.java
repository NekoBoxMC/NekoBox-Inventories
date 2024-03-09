package es.nekobox.nekoboxinventories.utils;

import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.Base64;

public class SaveInventory {

    private Database db;

    public SaveInventory(Database db) {
        this.db = db;
    }

    public void save(Player player, Player killer) {
        Connection conn = db.getConnection();
        String killerUuid = killer != null ? killer.getUniqueId().toString() : null;
        String killerName = killer != null ? killer.getName() : null;

        long unixTime = System.currentTimeMillis() / 1000L;

        String query = "INSERT INTO inventories (player_name, player_uuid, inventory_contents, unix_timestamp, killer_name, killer_uuid) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, player.getName());
            ps.setString(2, player.getUniqueId().toString());
            byte[] inventoryBytes = serialize(player.getInventory().getContents());
            ps.setBytes(3, inventoryBytes);
            ps.setLong(4, unixTime);
            ps.setString(5, killerName);
            ps.setString(6, killerUuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private byte[] serialize(ItemStack[] items) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("items", items);
        try {
            String serializedItems = yaml.saveToString();
            return Base64.getEncoder().encode(serializedItems.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}