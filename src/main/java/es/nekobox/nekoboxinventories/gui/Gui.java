package es.nekobox.nekoboxinventories.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Gui implements InventoryHolder {
    private final Component title;
    private final Map<Integer, GuiButton> buttons;
    private final Inventory inventory;
    private GuiCloseCallback[] closeCallbacks;

    public Gui(@NotNull Component title, int rows) {
        this.title = title;
        this.buttons = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public Gui(@NotNull String title, int rows) {
        this(MiniMessage.miniMessage().deserialize(title), rows);
    }


    public Component getTitle() {
        return title;
    }

    public void addButton(@NotNull GuiButton button, int slot) {
        this.buttons.put(slot, button);
        this.inventory.setItem(slot, button.icon());
    }

    public void addButton(@NotNull GuiButton button, int x, int y) {
        addButton(button, x + y * 9);
    }

    public void refresh() {
        this.inventory.clear();
        for (Map.Entry<Integer, GuiButton> entry : this.buttons.entrySet()) {
            this.inventory.setItem(entry.getKey(), entry.getValue().icon());
        }
    }

    public @Nullable GuiButton getButton(int slot) {
        return this.buttons.get(slot);
    }

    public @Nullable GuiButton getButton(int x, int y) {
        return this.buttons.get(x + y * 9);
    }

    public void open(@NotNull Player player) {
        player.openInventory(this.inventory);
    }

    public void close(@NotNull Player player, boolean fromEvent) {
        if (!fromEvent) player.closeInventory();
        if (this.closeCallbacks != null) {
            for (GuiCloseCallback callback : this.closeCallbacks) {
                if (callback != null) callback.onClose(this, player);
            }
        }
    }

    public void close(@NotNull Player player) {
        close(player, false);
    }

    public void addCloseCallback(@NotNull GuiCloseCallback callback) {
        if (this.closeCallbacks == null) {
            this.closeCallbacks = new GuiCloseCallback[1];
            this.closeCallbacks[0] = callback;
        }
        else {
            GuiCloseCallback[] newCallbacks = new GuiCloseCallback[this.closeCallbacks.length + 1];
            System.arraycopy(this.closeCallbacks, 0, newCallbacks, 0, this.closeCallbacks.length);
            newCallbacks[this.closeCallbacks.length] = callback;
            this.closeCallbacks = newCallbacks;
        }
    }

    public void clearCloseCallbacks() {
        this.closeCallbacks = null;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
