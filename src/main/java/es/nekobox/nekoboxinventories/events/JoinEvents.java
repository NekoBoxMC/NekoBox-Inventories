package es.nekobox.nekoboxinventories.events;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.Via;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinEvents implements Listener {

    private final JavaPlugin plugin;

    private static final int MIN_PROTOCOL_VERSION = 766; // Protocol version for 1.20.6

    public JoinEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ViaAPI api = Via.getAPI();
        int playerProtocolVersion = api.getPlayerVersion(event.getPlayer());

        new BukkitRunnable() {
            @Override
            public void run() {

                if (playerProtocolVersion < MIN_PROTOCOL_VERSION) {
                    // Play a sound
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                    // Send a title message
                    event.getPlayer().sendTitle(
                            ChatColor.RED + "Unsupported Minecraft Version",
                            ChatColor.YELLOW + "Please use 1.20.6+ for the best experience.",
                            10,
                            70,
                            20
                    );

                    // Send formatted chat messages
                    event.getPlayer().sendMessage(ChatColor.GOLD + "--------------------------------");
                    event.getPlayer().sendMessage("");
                    event.getPlayer().sendMessage(ChatColor.RED + "Warning: You are using an outdated version of Minecraft. Please update to 1.20.6 or higher for the best experience.");
                    event.getPlayer().sendMessage("");
                    event.getPlayer().sendMessage(ChatColor.RED + "Shops will not work properly. Click on the empty box to receive the shop item if it doesn't display!");
                    event.getPlayer().sendMessage("");
                    event.getPlayer().sendMessage(ChatColor.GOLD + "--------------------------------");
                }
            }
        }.runTaskLater(plugin, 60L); // 60 ticks = 3 seconds
    }
}