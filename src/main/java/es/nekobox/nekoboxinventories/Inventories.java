package es.nekobox.nekoboxinventories;

import es.nekobox.nekoboxinventories.commands.LoadInventoryCommand;
import es.nekobox.nekoboxinventories.commands.RestoreCommand;
import es.nekobox.nekoboxinventories.events.DeathEvents;
import es.nekobox.nekoboxinventories.events.GuiListener;
import es.nekobox.nekoboxinventories.utils.DataManager;
import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.SaveInventory;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Inventories extends JavaPlugin {
    private static Inventories instance;
    private Database db;
    public DataManager config;

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
        this.getCommand("loadinventory").setExecutor(new LoadInventoryCommand(this, db));
        this.getCommand("restore").setExecutor(new RestoreCommand(this, db));

        // Events
        getServer().getPluginManager().registerEvents(new DeathEvents(saveInventory), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    @Override
    public void onDisable() {
        db.close();
    }

    private void initDatabase() {
        db.connect();
        Connection conn = db.getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS inventories (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "player_name VARCHAR(255)," +
                "player_uuid VARCHAR(36), " +
                "inventory_contents TEXT, " +
                "unix_timestamp INT," +
                "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "killer_name VARCHAR(255), " +
                "killer_uuid VARCHAR(36))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
