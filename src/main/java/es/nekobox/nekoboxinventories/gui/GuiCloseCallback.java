package es.nekobox.nekoboxinventories.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface GuiCloseCallback {

    void onClose(@NotNull Gui gui, @NotNull Player player);

}
