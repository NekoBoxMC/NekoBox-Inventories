package es.nekobox.nekoboxinventories.events;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinEvents implements Listener {

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;

    private static final int MIN_PROTOCOL_VERSION = 766; // Protocol version for 1.20.6

    public JoinEvents(JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int playerProtocolVersion = protocolManager.getProtocolVersion(event.getPlayer());

        if (playerProtocolVersion < MIN_PROTOCOL_VERSION) {
            event.getPlayer().sendMessage(ChatColor.RED + "Warning: You are using an outdated version of Minecraft. Please update to 1.20.6 or higher for the best experience.");
            event.getPlayer().sendMessage(ChatColor.RED + "Shops will not work properly. Click on the empty box to receive the shop item if it doesn't display!");
        }
    }
}