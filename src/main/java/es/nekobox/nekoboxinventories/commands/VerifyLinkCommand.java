package es.nekobox.nekoboxinventories.commands;

import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class VerifyLinkCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final HashMap<String, String> playerCodes;
    private final Database db;

    public VerifyLinkCommand(JavaPlugin plugin, HashMap<String, String> playerCodes, Database db) {
        this.plugin = plugin;
        this.playerCodes = playerCodes;
        this.db = db;
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
                sender.sendMessage("Not Online");
                return true;
            }

            String discordId = args[1];
            String code = args[2];
            String playerName = player.getName();

            plugin.getLogger().info(discordId);
            plugin.getLogger().info(code);
            plugin.getLogger().info(playerName);
            System.out.println(playerCodes);

            if (playerCodes.containsKey(playerName) && playerCodes.get(playerName).equals(code)) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> syncLink(sender, player, discordId));
                playerCodes.remove(playerName);
            } else {
                sender.sendMessage("False");
            }

        } else {
            sender.sendMessage("Usage: /verifylink <name> <discord_id> <code>");
        }
        return true;
    }

    private void syncLink(CommandSender sender, Player player, String discordId) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, discord_id, player_name FROM player_linking WHERE player_name = ?")) {
            ps.setString(1, player.getName());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                try (PreparedStatement ps2 = conn.prepareStatement("SELECT id, discord_id, player_name FROM player_linking WHERE discord_id = ?")) {
                    ps2.setString(1, discordId);
                    ResultSet rs2 = ps2.executeQuery();

                    if (!rs2.next()) {
                        String query = "INSERT INTO player_linking (discord_id, player_name) VALUES (?, ?)";
                        try (PreparedStatement ps3 = conn.prepareStatement(query)) {
                            ps3.setString(1, discordId);
                            ps3.setString(2, player.getName());
                            ps3.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("True"));
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("Discord Already Linked"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("Minecraft Already Linked"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}