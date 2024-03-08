package es.nekobox.nekoboxinventories.utils;

import org.bukkit.Bukkit;
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
        try (Connection conn = db.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT player_uuid, inventory_contents FROM inventories WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String playerUuidStr = rs.getString("player_uuid");
                UUID playerUuid = UUID.fromString(playerUuidStr);
                Player player = Bukkit.getPlayer(playerUuid);
                if (player == null) {
                    sender.sendMessage("The player is not online.");
                    return;
                }

                byte[] bytes = rs.getBytes("inventory_contents");
                ItemStack[] items = deserialize(bytes);
                if (items != null) {
                    player.getInventory().setContents(items);
                    sender.sendMessage("Inventory loaded successfully to " + player.getName() + ".");
                } else {
                    sender.sendMessage("Error loading inventory.");
                }
            } else {
                sender.sendMessage("Inventory with the specified ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An error occurred while loading the inventory.");
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