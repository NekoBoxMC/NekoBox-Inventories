package es.nekobox.nekoboxinventories.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Quests {
    private Database db;

    public Quests(Database db) {
        this.db = db;
    }

    public Map<String, Object> getQuest(Player player) {
        Map<String, Object> questData = new HashMap<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT quest_id, notified FROM player_quests WHERE player_uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    questData.put("questId", rs.getInt("quest_id"));
                    questData.put("notified", rs.getBoolean("notified"));
                    return questData;
                } else {
                    createQuestProfile(player);
                    return getQuest(player);
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while retrieving the quest.");
            questData.put("error", true);
            return questData;
        }
    }

    private void createQuestProfile(Player player) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO player_quests (player_name, player_uuid, quest_id, notified) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, player.getName());
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, 1);
            ps.setBoolean(4, false);

            ps.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while setting up your quest profile.");
        }
    }
}