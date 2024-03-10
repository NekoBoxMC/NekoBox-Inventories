package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.gui.Gui;
import es.nekobox.nekoboxinventories.gui.GuiButton;
import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
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

public class RestoreCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Database db;
    private final Map<UUID, List<String>> playerDeathRecordsMap = new ConcurrentHashMap<>();
    private static final String NEXT_PAGE_NAME = ChatColor.AQUA + "Next Page";
    private static final String PREVIOUS_PAGE_NAME = ChatColor.AQUA + "Previous Page";
    private static final int ITEMS_PER_PAGE = 45;

    public RestoreCommand(JavaPlugin plugin, Database db) {
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
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, date, killer_name FROM inventories WHERE player_name = ? ORDER BY unix_timestamp DESC")) {
            ps.setString(1, restoredPlayer.getName());
            ResultSet rs = ps.executeQuery();

            List<String> deathRecords = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String killerName = rs.getString("killer_name");
                deathRecords.add("ID: " + id + " - Killed By: " + (killerName == null ? "N/A" : killerName) + " - Date: " + (date == null ? "N/A": date));
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
                // DO NOTHING.
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

        meta.setDisplayName(ChatColor.WHITE + idPart);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + killerPart);
        lore.add(ChatColor.GRAY + datePart);
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

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        if (event.getView().getTitle().startsWith("Death Records - Page")) {
//            event.setCancelled(true);
//
//            ItemStack clickedItem = event.getCurrentItem();
//            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
//
//            Player player = (Player) event.getWhoClicked();
//            ItemMeta meta = clickedItem.getItemMeta();
//            if (meta == null || !meta.hasDisplayName()) return;
//
//            String itemName = meta.getDisplayName();
//            if (itemName.equals(NEXT_PAGE_NAME) || itemName.equals(PREVIOUS_PAGE_NAME)) {
//                String title = event.getView().getTitle();
//                int currentPage = Integer.parseInt(title.replaceAll("[^0-9]", ""));
//                List<String> deathRecords = playerDeathRecordsMap.get(player.getUniqueId());
//
//                if (itemName.equals(NEXT_PAGE_NAME) && currentPage < getTotalPages(deathRecords)) {
//                    openInventory(player, currentPage + 1);
//                } else if (itemName.equals(PREVIOUS_PAGE_NAME) && currentPage > 1) {
//                    openInventory(player, currentPage - 1);
//                }
//            }
//        }
//    }

    private int getTotalPages(List<String> deathRecords) {
        if (deathRecords == null) {
            return 0;
        }
        return (int) Math.ceil((double) deathRecords.size() / ITEMS_PER_PAGE);
    }

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        if (event.getView().getTitle().startsWith("Death Records - Page")) {
//            event.setCancelled(true);
//
//            ItemStack clickedItem = event.getCurrentItem();
//            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
//
//            Player player = (Player) event.getWhoClicked();
//            ItemMeta meta = clickedItem.getItemMeta();
//            if (meta == null || !meta.hasDisplayName()) return;
//
//            String itemName = meta.getDisplayName();
//            if (itemName.equals(NEXT_PAGE_NAME) || itemName.equals(PREVIOUS_PAGE_NAME)) {
//                String title = event.getView().getTitle();
//                int currentPage = Integer.parseInt(title.replaceAll("[^0-9]", ""));
//                List<String> deathRecords = playerDeathRecordsMap.get(player.getUniqueId());
//
//                if (deathRecords == null) {
//                    return;
//                }
//
//                if (itemName.equals(NEXT_PAGE_NAME) && currentPage < getTotalPages(deathRecords)) {
//                    openInventory(player, currentPage + 1);
//                } else if (itemName.equals(PREVIOUS_PAGE_NAME) && currentPage > 1) {
//                    openInventory(player, currentPage - 1);
//                }
//            }
//        }
//    }

//    @EventHandler
//    public void onInventoryClose(InventoryCloseEvent event) {
//        if (event.getView().getTitle().startsWith("Death Records - Page")) {
//            playerDeathRecordsMap.remove(event.getPlayer().getUniqueId());
//        }
//    }
}