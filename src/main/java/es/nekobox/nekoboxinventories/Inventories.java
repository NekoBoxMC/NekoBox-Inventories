package es.nekobox.nekoboxinventories;

import es.nekobox.nekoboxinventories.commands.*;
import es.nekobox.nekoboxinventories.events.BoosterEvents;
import es.nekobox.nekoboxinventories.events.DeathEvents;
import es.nekobox.nekoboxinventories.events.GuiListener;
import es.nekobox.nekoboxinventories.events.QuestEvents;
import es.nekobox.nekoboxinventories.utils.DataManager;
import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.SaveInventory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.HashMap;

public final class Inventories extends JavaPlugin {
    private static Inventories instance;
    private Database db;
    public DataManager config;
    private HashMap<String, String> playerCodes = new HashMap<>();

    @Override
    public void onEnable() {
        // General Initializers
        if (instance == null) instance = this;

        // Config
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        this.config = new DataManager(this, "config.yml");

        // Database
        db = new Database(this);
        initDatabase();

        SaveInventory saveInventory = new SaveInventory(db);

        // Commands
        this.getCommand("findblock").setExecutor(new FindBlockCommand(this));
        this.getCommand("generatecode").setExecutor(new GenerateCodeCommand(this, playerCodes));
        this.getCommand("loadinventory").setExecutor(new LoadInventoryCommand(db));
        //this.getCommand("spawnquestvillager").setExecutor(new QuestsCommand(this, db));
        this.getCommand("revive").setExecutor(new ReviveCommand(this, db));
        this.getCommand("verifylink").setExecutor(new VerifyLinkCommand(this, playerCodes, db));

        // Events
        getServer().getPluginManager().registerEvents(new DeathEvents(saveInventory), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        //getServer().getPluginManager().registerEvents(new QuestEvents(db), this);
        getServer().getPluginManager().registerEvents(new BoosterEvents(this), this);

        scheduleDailyTask(12, 0, "nbc key giveall daily 1");
    }

    @Override
    public void onDisable() {
        db.close();
    }

    private void initDatabase() {
        try (Connection conn = db.getConnection()) {
            String sqlInventories = "CREATE TABLE IF NOT EXISTS inventories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(255), " +
                    "player_uuid VARCHAR(36), " +
                    "inventory_contents TEXT, " +
                    "unix_timestamp INT, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "killer_name VARCHAR(255), " +
                    "killer_uuid VARCHAR(36), " +
                    "claimed INT DEFAULT 0)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInventories)) {
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create inventories table", e);
            }

            String sqlPlayerLinking = "CREATE TABLE IF NOT EXISTS player_linking (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "discord_id VARCHAR(255), " +
                    "player_name VARCHAR(36))";
            try (PreparedStatement ps = conn.prepareStatement(sqlPlayerLinking)) {
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create player_linking table", e);
            }

            String sqlPlayerQuests = "CREATE TABLE IF NOT EXISTS player_quests (" +
                    "player_name VARCHAR(255), " +
                    "player_uuid VARCHAR(36), " +
                    "notified BOOLEAN DEFAULT false, " +
                    "quest_id INT)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPlayerQuests)) {
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create player_quests table", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    private void scheduleDailyTask(int hour, int minute, String command) {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                if (now.getHour() == hour && now.getMinute() == minute) {
                    // Run the command
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }.runTaskTimer(this, 0, 20 * 60); // Check every minute (20 ticks per second * 60 seconds)
    }
}