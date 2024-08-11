package es.nekobox.nekoboxinventories.events;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoosterEvents implements Listener {
    private final JavaPlugin plugin;

    public BoosterEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final String BOOSTER_NAME = ChatColor.translateAlternateColorCodes('&', "&d&lBlock Booster");
    private static final Pattern BOOSTER_LORE_PATTERN = Pattern.compile("(\\d+)min of x(\\d+) Drops");
    private Set<UUID> boostedPlayers = new HashSet<>();
    private Map<UUID, Integer> playerBoostMultipliers = new HashMap<>();

    @EventHandler
    public void onPlayerUseBooster(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.PAPER) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                String displayName = meta.getDisplayName();
                List<String> lore = meta.getLore();

                if (BOOSTER_NAME.equals(displayName)) {
                    for (String loreLine : lore) {
                        Matcher matcher = BOOSTER_LORE_PATTERN.matcher(ChatColor.stripColor(loreLine));
                        if (matcher.matches()) {

                            // Check if the player already has an active booster
                            if (boostedPlayers.contains(playerUUID)) {
                                player.sendMessage(ChatColor.RED + "You already have an active booster. Wait until it expires before activating another one.");
                                return;
                            }

                            int durationMinutes = Integer.parseInt(matcher.group(1));
                            int dropMultiplier = Integer.parseInt(matcher.group(2));
                            activateBooster(player, item, durationMinutes, dropMultiplier);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void activateBooster(Player player, ItemStack boosterItem, int durationMinutes, int dropMultiplier) {
        // Remove one item from the stack
        boosterItem.setAmount(boosterItem.getAmount() - 1);

        // Notify the player
        player.sendMessage(ChatColor.GREEN + "Booster activated! You have " + durationMinutes + " minutes of x" + dropMultiplier + " drops.");

        // Add the player to the boostedPlayers set with the corresponding multiplier
        UUID playerUUID = player.getUniqueId();
        boostedPlayers.add(playerUUID);
        playerBoostMultipliers.put(playerUUID, dropMultiplier);

        // Start the booster effect
        new BukkitRunnable() {
            @Override
            public void run() {
                // End the booster effect
                player.sendMessage(ChatColor.RED + "Your booster has expired.");
                boostedPlayers.remove(playerUUID);
                playerBoostMultipliers.remove(playerUUID);
            }
        }.runTaskLater(this.plugin, durationMinutes * 60 * 20L); // Convert minutes to ticks
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check if the player is in survival mode
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return; // Exit the method if the player is not in survival mode
        }

        // Get the block type and determine the smelted result
        Material blockType = event.getBlock().getType();
        Material resultType = getSmeltedMaterial(blockType);

        if (resultType == null) {
            resultType = blockType; // If the block cannot be smelted, use the block type itself
        }

        // Cancel the default block drop
        event.setDropItems(false);

        // Calculate the number of drops based on the Fortune level
        int fortuneLevel = player.getInventory().getItemInMainHand().getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS);
        int totalDropAmount = calculateFortuneDrops(fortuneLevel);

        // If the player has the booster, multiply the drop amount by the booster multiplier
        if (boostedPlayers.contains(player.getUniqueId())) {
            int dropMultiplier = playerBoostMultipliers.getOrDefault(player.getUniqueId(), 1);
            totalDropAmount *= dropMultiplier;
        }

        // Handle adding items in stacks of 64
        while (totalDropAmount > 0) {
            int amountToAdd = Math.min(totalDropAmount, 64); // Limit to 64 per stack
            ItemStack resultDrop = new ItemStack(resultType, amountToAdd);
            totalDropAmount -= amountToAdd;

            // Try to add the item to the player's inventory
            player.getInventory().addItem(resultDrop);
        }
    }

    private Material getSmeltedMaterial(Material blockType) {
        return switch (blockType) {
            case IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE -> Material.GOLD_INGOT;
            case SAND -> Material.GLASS;
            case COBBLESTONE -> Material.STONE;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            // Add more cases as needed for other blocks
            default -> null;
        };
    }

    private int calculateFortuneDrops(int fortuneLevel) {
        if (fortuneLevel > 0) {
            // Fortune level increases the chances of additional drops
            int multiplier = 1 + fortuneLevel;
            return (int) (Math.random() * multiplier) + 1;
        } else {
            return 1;
        }
    }
}