package es.nekobox.nekoboxinventories;

import es.nekobox.nekoboxinventories.commands.LoadInventoryCommand;
import es.nekobox.nekoboxinventories.commands.RestoreCommand;
import es.nekobox.nekoboxinventories.events.DeathEvents;
import es.nekobox.nekoboxinventories.events.RestoreInventoryEvents;
import es.nekobox.nekoboxinventories.utils.Database;
import es.nekobox.nekoboxinventories.utils.SaveInventory;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.SQLException;

public final class Inventories extends JavaPlugin {
    private Database db;

    @Override
    public void onEnable() {
        db = new Database();
        initDatabase();

        SaveInventory saveInventory = new SaveInventory(db);

        // Commands
        this.getCommand("loadinventory").setExecutor(new LoadInventoryCommand(this, db));
        this.getCommand("restore").setExecutor(new RestoreCommand(this, db));

        // Events
        getServer().getPluginManager().registerEvents(new DeathEvents(saveInventory), this);
        getServer().getPluginManager().registerEvents(new RestoreInventoryEvents(db), this);
    }

    @Override
    public void onDisable() {
        try {
            db.getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDatabase() {
        try (Connection conn = db.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS inventories (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "player_name VARCHAR(255)," +
                    "player_uuid VARCHAR(36), " +
                    "inventory_contents TEXT, " +
                    "unix_timestamp INT," +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "killer_name VARCHAR(255), " +
                    "killer_uuid VARCHAR(36))";
            conn.prepareStatement(sql).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
