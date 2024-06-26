package es.nekobox.nekoboxinventories.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class LoadInventory {

    private final Database db;

    public LoadInventory(Database db) {
        this.db = db;
    }

    public void loadInventory(CommandSender sender, int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT player_uuid, inventory_contents, claimed FROM inventories WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    Player player = Bukkit.getPlayer(playerUuid);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "The player is not online.");
                        return;
                    }

                    byte[] bytes = rs.getBytes("inventory_contents");
                    ItemStack[] items = deserialize(bytes);
                    if (items == null) {
                        sender.sendMessage(ChatColor.RED + "Error loading inventory.");
                        return;
                    }

                    if (rs.getInt("claimed") == 0) {
                        markInventoryAsClaimed(conn, id);
                    }

                    player.getInventory().setContents(items);
                    sender.sendMessage(ChatColor.GREEN + "Inventory loaded successfully to " + player.getName() + ".");
                    player.sendMessage(ChatColor.GREEN + "Your inventory has been restored with the ID of: " + ChatColor.YELLOW + id);
                } else {
                    sender.sendMessage(ChatColor.RED + "Inventory with the specified ID not found.");
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while retrieving the inventory.");
            e.printStackTrace();
        }
    }

    private void markInventoryAsClaimed(Connection conn, int id) throws SQLException {
        try (PreparedStatement updatePs = conn.prepareStatement("UPDATE inventories SET claimed = 1 WHERE id = ?")) {
            updatePs.setInt(1, id);
            updatePs.executeUpdate();
        }
    }

    public ItemStack[] getInventoryContents(int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT inventory_contents FROM inventories WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] bytes = rs.getBytes("inventory_contents");
                    return deserialize(bytes);
                } else {
                    return new ItemStack[0];
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }

    public ItemStack[] deserialize(byte[] data) {
        try {
            String serializedItems = new String(Base64.getDecoder().decode(data));
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(serializedItems);

            List<?> itemList = (List<?>) yaml.get("items");
            if (itemList == null) {
                return new ItemStack[0];
            }

            ItemStack[] items = new ItemStack[itemList.size()];
            for (int i = 0; i < itemList.size(); i++) {
                items[i] = (ItemStack) itemList.get(i);
            }

            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}