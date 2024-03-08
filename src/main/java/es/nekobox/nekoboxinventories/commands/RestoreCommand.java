package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RestoreCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Database db;

    public RestoreCommand(JavaPlugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nekobox.inventories.load")) {
            sender.sendMessage("You do not have permission to use this command.");
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /restore <name>");
            return true;
        }

        String playerName = args[0];
        Player player = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = db.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT id, date, killer_name FROM inventories WHERE player_name = ? ORDER BY unix_timestamp DESC");
                ps.setString(1, playerName);
                ResultSet rs = ps.executeQuery();

                List<String> deathRecords = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String date = rs.getString("date");
                    String killerName = rs.getString("killer_name");
                    deathRecords.add("ID: " + id + " - Killed By: " + (killerName == null ? "N/A" : killerName) + " - Date: " + (date == null ? "N/A": date));
                }

                Bukkit.getScheduler().runTask(plugin, () -> openInventory(player, deathRecords));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    private void openInventory(Player player, List<String> deathRecords) {
        int inventorySize = ((deathRecords.size() / 9) + 1) * 9;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "Death Records");

        for (String record : deathRecords) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            String[] recordParts = record.split(" - ");
            String idPart = recordParts[0];

            // Handle potential missing parts
            String killerPart = recordParts.length > 1 ? recordParts[1] : "Killer: Unknown";
            String datePart = recordParts.length > 2 ? recordParts[2] : "Date: Unknown";

            meta.setDisplayName(ChatColor.GREEN + idPart);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + killerPart);
            lore.add(ChatColor.GRAY + datePart);
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.addItem(item);
        }
        player.openInventory(inventory);
    }
}