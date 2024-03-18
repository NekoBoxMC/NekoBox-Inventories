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
import java.util.HashMap;

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

            if (playerCodes.containsKey(player.getName()) && playerCodes.get(player.getName()).equals(code)) {
                Connection conn = db.getConnection();
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT id, discord_id, player_name FROM player_linking WHERE player_name = ?");
                    ps.setString(1, player.getName());
                    ResultSet rs = ps.executeQuery();

                    String message;
                    if (!rs.next()) {
                        PreparedStatement ps2 = conn.prepareStatement("SELECT id, discord_id, player_name FROM player_linking WHERE discord_id = ?");
                        ps2.setString(1, discordId);
                        ResultSet rs2 = ps2.executeQuery();

                        if (!rs2.next()) {
                            String query = "INSERT INTO player_linking (discord_id, player_name) VALUES (?, ?)";
                            PreparedStatement ps3 = conn.prepareStatement(query);
                            ps3.setString(1, discordId);
                            ps3.setString(2, player.getName());
                            ps3.executeUpdate();
                            message = "True";
                            playerCodes.remove(player.getName());
                        } else {
                            message = "Discord Already Linked";
                        }
                    } else {
                        message = "Minecraft Already Linked";
                    }

                    String finalMessage = message;
                    sender.sendMessage(finalMessage);

                } catch (SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage("Error");
                }
            } else {
                sender.sendMessage("False");
            }

        } else {
            sender.sendMessage("Usage: /verifylink <name> <discord_id> <code>");
        }
        return true;
    }
}