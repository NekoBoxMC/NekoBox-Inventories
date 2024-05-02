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
                sender.sendMessage(ChatColor.RED + "Player not online.");
                return true;
            }

            String discordId = args[1];
            String code = args[2];

            if (!playerCodes.getOrDefault(player.getName(), "").equals(code)) {
                sender.sendMessage(ChatColor.RED + "Incorrect code.");
                return true;
            }

            try (Connection conn = db.getConnection()) {
                if (isPlayerLinked(conn, player.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "Minecraft account already linked.");
                } else if (isDiscordLinked(conn, discordId)) {
                    sender.sendMessage(ChatColor.YELLOW + "Discord ID already linked.");
                } else {
                    linkPlayer(conn, discordId, player.getName());
                    sender.sendMessage(ChatColor.GREEN + "Account linked successfully.");
                    playerCodes.remove(player.getName());
                }
            } catch (SQLException e) {
                sender.sendMessage(ChatColor.RED + "Error during database operation.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /verifylink <name> <discord_id> <code>");
        }
        return true;
    }

    private boolean isPlayerLinked(Connection conn, String playerName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM player_linking WHERE player_name = ?")) {
            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isDiscordLinked(Connection conn, String discordId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM player_linking WHERE discord_id = ?")) {
            ps.setString(1, discordId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void linkPlayer(Connection conn, String discordId, String playerName) throws SQLException {
        String query = "INSERT INTO player_linking (discord_id, player_name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, discordId);
            ps.setString(2, playerName);
            ps.executeUpdate();
        }
    }
}