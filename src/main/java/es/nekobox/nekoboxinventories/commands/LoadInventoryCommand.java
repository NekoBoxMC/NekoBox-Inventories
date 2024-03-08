package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.LoadInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class LoadInventoryCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private Database db;

    public LoadInventoryCommand(JavaPlugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nekobox.inventories.load")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /loadinventory <id>");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid ID. Please enter a numeric ID.");
            return true;
        }

        LoadInventory loadInventory = new LoadInventory(db);
        loadInventory.loadInventory(sender, id);

        return true;
    }
}