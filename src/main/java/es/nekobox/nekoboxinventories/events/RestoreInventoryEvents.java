package es.nekobox.nekoboxinventories.events;

import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.LoadInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RestoreInventoryEvents implements Listener {
    private final Database db;

    public RestoreInventoryEvents(Database db) {
        this.db = db;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Death Records - Page")) {
            event.setCancelled(true);
            event.getInventory().close();

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null) {
                if (clickedItem.getType() == Material.PAPER) {
                    if (clickedItem.hasItemMeta()) {
                        ItemMeta meta = clickedItem.getItemMeta();
                        if (meta.hasDisplayName()) {
                            String idString = ChatColor.stripColor(meta.getDisplayName()).replace("ID: ", "");
                            LoadInventory loadInventory = new LoadInventory(db);
                            loadInventory.loadInventory(event.getWhoClicked(), Integer.parseInt(idString));
                        }
                    }
                }
            }
        }
    }
}