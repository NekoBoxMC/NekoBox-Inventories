package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class QuestsCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private Database db;

    public QuestsCommand(JavaPlugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        if (!sender.hasPermission("op")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();
        Villager questVillager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        questVillager.setAI(false);
        questVillager.setInvulnerable(true);
        questVillager.setGravity(false);
        questVillager.setCanPickupItems(false);
        questVillager.setGlowing(true);
        questVillager.setCustomName(ChatColor.GREEN + "Quests");
        questVillager.setCustomNameVisible(true);
        questVillager.setRotation(loc.getYaw(), loc.getPitch());

        player.sendMessage(ChatColor.GREEN + "Quest villager spawned!");

        return true;
    }
}