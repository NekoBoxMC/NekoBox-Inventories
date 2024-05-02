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
        byte[] inventoryBytes = serialize(player.getInventory().getContents());

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO inventories (player_name, player_uuid, inventory_contents, unix_timestamp, killer_name, killer_uuid) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, player.getName());
            ps.setString(2, player.getUniqueId().toString());
            ps.setBytes(3, inventoryBytes);
            ps.setLong(4, System.currentTimeMillis() / 1000L);

            if (killer != null) {
                ps.setString(5, killer.getName());
                ps.setString(6, killer.getUniqueId().toString());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save player inventory data.");
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