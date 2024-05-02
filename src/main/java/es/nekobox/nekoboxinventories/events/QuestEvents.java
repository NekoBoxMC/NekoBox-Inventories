package es.nekobox.nekoboxinventories.events;

import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import es.nekobox.nekoboxinventories.utils.Quests;

import java.util.Map;

public class QuestEvents implements Listener {

    private Quests quests;

    public QuestEvents(Database db) {
        this.quests = new Quests(db);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            String name = ChatColor.translateAlternateColorCodes('&', "&aQuests");
            if (name.equals(villager.getCustomName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String name = ChatColor.translateAlternateColorCodes('&', "&aQuests");

            if (name.equals(villager.getCustomName())) {
                Player player = event.getPlayer();
                Map<String, Object> questData = quests.getQuest(player);

                if (questData.containsKey("error")) {
                    player.sendMessage(ChatColor.RED + "An error occurred while retrieving the quest.");
                    return;
                }

                int questId = (Integer) questData.get("questId");
                boolean notified = (Boolean) questData.get("notified");

                if (!notified) {

                } else {
                    return;
                }

                player.sendMessage(ChatColor.GOLD + "ID: " + questId);
                player.sendMessage(ChatColor.YELLOW + "Notified: " + notified);
                player.sendMessage(villager.getCustomName());
            }
        }
    }
}