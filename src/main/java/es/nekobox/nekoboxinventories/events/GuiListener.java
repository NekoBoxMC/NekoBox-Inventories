package es.nekobox.nekoboxinventories.events;

import es.nekobox.nekoboxinventories.gui.Gui;
import es.nekobox.nekoboxinventories.gui.GuiButton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Gui gui) {
            gui.close((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Gui gui) {
            GuiButton button = gui.getButton(event.getSlot());
            if (button == null) return;
            event.setCancelled(true);
            button.clickCallback().onClick(gui,(Player) event.getWhoClicked(), event.getClick());
        }
    }

}


