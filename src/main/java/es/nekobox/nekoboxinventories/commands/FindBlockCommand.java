package es.nekobox.nekoboxinventories.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FindBlockCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public FindBlockCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("op")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /findblock <block> <name>");
            return true;
        }

        Material blockType;
        try {
            blockType = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid block type.");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Location blockLocation = findBlock(blockType);
                if (blockLocation == null) {
                    player.sendMessage(ChatColor.RED + "No " + blockType.name().toLowerCase() + " block found.");
                } else {
                    // Ensuring the teleport runs on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            targetPlayer.teleport(blockLocation.add(0.5, 1, 0.5));
                            targetPlayer.sendMessage(ChatColor.GREEN + "Teleported to the nearest " + blockType.name().toLowerCase() + " block!");
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    private Location findBlock(Material blockType) {
        for (Chunk chunk : Bukkit.getWorlds().get(0).getLoadedChunks()) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == blockType) {
                            return block.getLocation();
                        }
                    }
                }
            }
        }
        return null;
    }
}