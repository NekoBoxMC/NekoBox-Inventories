package es.nekobox.nekoboxinventories.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record GuiButton(@NotNull ItemStack icon, @NotNull GuiClickCallback clickCallback) {

    public GuiButton(@NotNull ItemStack icon) {
        this(icon, (gui, player, clickType) -> {});
    }


    @FunctionalInterface
    public interface GuiClickCallback {

        void onClick(@NotNull Gui gui, @NotNull Player player, @NotNull ClickType clickType);

    }


}
