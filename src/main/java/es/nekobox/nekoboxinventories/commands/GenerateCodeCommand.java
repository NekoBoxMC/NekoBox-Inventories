package es.nekobox.nekoboxinventories.commands;

import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Random;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class GenerateCodeCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final HashMap<String, String> playerCodes;
    private final Random random = new Random();

    public GenerateCodeCommand(JavaPlugin plugin, HashMap<String, String> playerCodes) {
        this.plugin = plugin;
        this.playerCodes = playerCodes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("op")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage("The player is not online.");
                return true;
            }

            String uniqueCode = "";

            if (playerCodes.containsKey(player.getName())) {
                uniqueCode = playerCodes.get(player.getName());
                sender.sendMessage(uniqueCode);
            } else {
                uniqueCode = generateUniqueCode();
                playerCodes.put(player.getName(), uniqueCode);
                sender.sendMessage(uniqueCode);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2.0F, 1.0F);
            String message = ChatColor.AQUA + args[1] + ChatColor.GREEN +
                    " has requested to link their Discord Account to your Minecraft Account. " +
                    "If this was you, please input the following code into the website: \n";

            String copyMessage = ChatColor.GRAY + "\n(click to copy)";

            TextComponent codeComponent = new TextComponent("\n" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + uniqueCode);
            codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uniqueCode));

            player.sendMessage(message);
            player.spigot().sendMessage(codeComponent);
            player.sendMessage(copyMessage);

            sender.sendMessage(uniqueCode);
        } else {
            sender.sendMessage("Usage: /generatecode <name> <discord_name> <discord_id>");
        }

        return true;
    }

    private String generateUniqueCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        while (true) {
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
            String result = code.toString();
            if (!playerCodes.containsValue(result)) {
                return result;
            }
            code.setLength(0);
        }
    }
}