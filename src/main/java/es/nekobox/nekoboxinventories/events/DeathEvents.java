package es.nekobox.nekoboxinventories.events;

import es.nekobox.nekoboxinventories.utils.SaveInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvents implements Listener {

    private SaveInventory saveInventory;

    public DeathEvents(SaveInventory saveInventory) {
        this.saveInventory = saveInventory;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();
        saveInventory.save(deceased, killer);
    }
}