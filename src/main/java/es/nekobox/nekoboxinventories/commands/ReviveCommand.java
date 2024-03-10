package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.gui.Gui;
import es.nekobox.nekoboxinventories.gui.GuiButton;
import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.LoadInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Database db;
    private final Map<UUID, List<String>> playerDeathRecordsMap = new ConcurrentHashMap<>();
    private static final String NEXT_PAGE_NAME = ChatColor.AQUA + "Next Page";
    private static final String PREVIOUS_PAGE_NAME = ChatColor.AQUA + "Previous Page";
    private static final int ITEMS_PER_PAGE = 45;

    public ReviveCommand(JavaPlugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nekobox.inventories.restore")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /restore <name>");
            return true;
        }
        Player restoredPlayer = Bukkit.getPlayer(args[0]);

        if (restoredPlayer == null) {
            sender.sendMessage(ChatColor.RED + "The player is not online.");
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> syncFetch(restoredPlayer, player));
        return true;
    }

    private void syncFetch(Player restoredPlayer, Player player) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, date, killer_name, claimed FROM inventories WHERE player_name = ? ORDER BY unix_timestamp DESC")) {
            ps.setString(1, restoredPlayer.getName());
            ResultSet rs = ps.executeQuery();

            List<String> deathRecords = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String killerName = rs.getString("killer_name");
                Integer claimed = rs.getInt("claimed");
                String claimedString = "";
                if (claimed == 0) {
                    claimedString = "False";
                } else {
                    claimedString = "True";
                }
                deathRecords.add("ID: " + id + " - Killed By: " + (killerName == null ? "N/A" : killerName) + " - Date: " + (date == null ? "N/A": date) + " - Claimed: " + claimedString);
            }

            if (deathRecords.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "No death records found for " + restoredPlayer.getName()));
                return;
            }

            playerDeathRecordsMap.put(player.getUniqueId(), deathRecords);

            Bukkit.getScheduler().runTask(plugin, () -> openInventory(player, 1));
        } catch (SQLException e) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "An error occurred while retrieving death records."));
            throw new RuntimeException(e);
        }
    }

    private void openInventory(Player player, int page) {
        List<String> deathRecords = playerDeathRecordsMap.get(player.getUniqueId());
        if (deathRecords == null) {
            player.sendMessage(ChatColor.RED + "Error: Death records not found.");
            return;
        }

        int totalItems = deathRecords.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));

        Gui gui = new Gui("<gold>Death Records - Page <yellow>" + page, 6);

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = createRecordItem(deathRecords.get(i));
            GuiButton button = new GuiButton(item, (g1, p1, clickType) -> {
                int id = Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace("ID: ", ""));
                ItemStack[] items = new LoadInventory(db).getInventoryContents(id);
                if (items != null) {
                    openItemsInventory(player, items, id);
                } else {
                    player.sendMessage(ChatColor.RED + "Error retrieving inventory contents.");
                }
            });
            gui.addButton(button, i - startIndex);
        }

        for (int i = endIndex - startIndex; i < ITEMS_PER_PAGE; i++) {
            gui.addButton(new GuiButton(createPlaceholderItem()), i);
        }

        if (page > 1) {
            ItemStack item = createControlItem(PREVIOUS_PAGE_NAME);
            int finalPage = page;
            GuiButton button = new GuiButton(item, (g1, p1, clickType) -> {
                openInventory(player, finalPage - 1);
            });
            gui.addButton(button, 45);
        }
        if (page < totalPages) {
            ItemStack item = createControlItem(NEXT_PAGE_NAME);
            int finalPage = page;
            GuiButton button = new GuiButton(item, (g1, p1, clickType) -> {
                openInventory(player, finalPage + 1);
            });
            gui.addButton(button, 53);
        }
        gui.open(player);
    }

    private ItemStack createRecordItem(String record) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        String[] parts = record.split(" - ");
        String idPart = parts[0];
        String killerPart = parts.length > 1 ? parts[1] : "Killer: Unknown";
        String datePart = parts.length > 2 ? parts[2] : "Date: Unknown";
        String claimedPart = parts.length > 3 ? parts[3] : "Date: Unknown";

        meta.setDisplayName(ChatColor.WHITE + idPart);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + killerPart);
        lore.add(ChatColor.GRAY + datePart);
        lore.add(ChatColor.GRAY + claimedPart);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlaceholderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createControlItem(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private void openItemsInventory(Player player, ItemStack[] items, int inventoryId) {
        Gui gui = new Gui("<gold>Inventory Contents", 6);
        for (int i = 0; i < items.length; i++) {
            if (i >= 45) break;
            ItemStack item = items[i];
            if (item != null) {
                GuiButton button = new GuiButton(item);
                gui.addButton(button, i);
            }
        }

        ItemStack restoreButtonItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta restoreButtonMeta = restoreButtonItem.getItemMeta();
        restoreButtonMeta.setDisplayName(ChatColor.GREEN + "Restore Inventory");
        restoreButtonItem.setItemMeta(restoreButtonMeta);

        GuiButton restoreButton = new GuiButton(restoreButtonItem, (g, p, clickType) -> {
            new LoadInventory(db).loadInventory(player, inventoryId);
            player.sendMessage(ChatColor.GREEN + "Inventory restored successfully!");
            player.closeInventory();
        });

        gui.addButton(restoreButton, 49);

        gui.open(player);
    }
}