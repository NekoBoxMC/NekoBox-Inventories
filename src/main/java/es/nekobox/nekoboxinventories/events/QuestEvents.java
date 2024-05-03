package es.nekobox.nekoboxinventories.events;

import es.nekobox.nekoboxinventories.utils.Database;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import es.nekobox.nekoboxinventories.utils.Quests;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Map;

public class QuestEvents implements Listener {

    private Quests quests;

    public QuestEvents(Database db) {
        this.quests = new Quests(db);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            String name = ChatColor.translateAlternateColorCodes('&', "&aQuests");
            if (name.equals(villager.getCustomName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            String name = ChatColor.translateAlternateColorCodes('&', "&aQuests");

            if (name.equals(villager.getCustomName())) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                Map<String, Object> questData = quests.getQuest(player);

                if (questData.containsKey("error")) {
                    player.sendMessage(ChatColor.RED + "An error occurred while retrieving the quest.");
                    return;
                }

                int questId = (Integer) questData.get("questId");
                boolean notified = (Boolean) questData.get("notified");

                if (!notified) {
                    if (questId >= 6) {
                        String message = "&d&lQuests &7» &aYou've completed all the quests. Be sure to check back soon for more!";
                        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                        player.sendMessage(coloredMessage);
                    } else {
                        quests.updateQuestNotify(player, true);
                        if (questId == 1) {
                            String message = "&d&lQuests &7» &aYou're going to need gear! First, bring me &e&l16 Honey Blocks&a. &eHoney Blocks &acan be found right behind spawn and will be necessary for progressing!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else if (questId == 2) {
                            String message = "&d&lQuests &7» &aYou're going to need gear! Gather and wear a full set of &e&lVenus Gear&a. &eVenus Gear &acan be found at the villager where you mined the Honey Blocks!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else if (questId == 3) {
                            String message = "&d&lQuests &7» &aYou're now ready to fight! Get a total of &e&l3 Kills&a!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else if (questId == 4) {
                            String message = "&d&lQuests &7» &aSometimes you're not as powerful as you need to be. Get a total of &e&l1 Death&a!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else if (questId == 5) {
                            String message = "&d&lQuests &7» &e&lBeacons &aare a powerful item. They can be sold for coins to buy miscellaneous items which are essential to beat other players. Gather &e&l1 Beacon&a.";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    }
                } else {
                    if (questId == 1) {
                        if (player.getInventory().containsAtLeast(new ItemStack(Material.HONEY_BLOCK), 16)) {
                            player.getInventory().removeItem(new ItemStack(Material.HONEY_BLOCK, 16));

                            quests.updateQuestNotify(player, false);
                            quests.incrementQuestId(player);

                            String message = "&d&lQuests &7» &aCongratulations, you have completed this quest! You have been awarded &e&lX&a. If you wish to do another quest, click me again!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else {
                            String message = "&d&lQuests &7» &cYou do not have &e&l16 Honey Blocks &cfor me!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    } else if (questId == 2) {
                        if (checkVenusArmor(player)) {
                            quests.updateQuestNotify(player, false);
                            quests.incrementQuestId(player);

                            String message = "&d&lQuests &7» &aCongratulations, you have completed this quest! You have been awarded &e&lX&a. If you wish to do another quest, click me again!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else {
                            String message = "&d&lQuests &7» &cYou are not wearing a full set of &e&lVenus Gear&c.";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    } else if (questId == 3) {
                        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
                        if (kills >= 3) {
                            quests.updateQuestNotify(player, false);
                            quests.incrementQuestId(player);

                            String message = "&d&lQuests &7» &aCongratulations, you have completed this quest! You have been awarded &e&lX&a. If you wish to do another quest, click me again!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else {
                            String message = "&d&lQuests &7» &cYou do not have &e&l3 Kills&c!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    } else if (questId == 4) {
                        int deaths = player.getStatistic(Statistic.DEATHS);
                        if (deaths >= 1) {
                            quests.updateQuestNotify(player, false);
                            quests.incrementQuestId(player);

                            String message = "&d&lQuests &7» &aCongratulations, you have completed this quest! You have been awarded &e&lX&a. If you wish to do another quest, click me again!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else {
                            String message = "&d&lQuests &7» &cYou do not have &e&l1 Death&c!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    } else if (questId == 5) {
                        if (player.getInventory().containsAtLeast(new ItemStack(Material.BEACON), 1)) {

                            quests.updateQuestNotify(player, false);
                            quests.incrementQuestId(player);

                            String message = "&d&lQuests &7» &aCongratulations, you have completed this quest! You have been awarded &e&lX&a. If you wish to do another quest, click me again!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        } else {
                            String message = "&d&lQuests &7» &cYou do not have &e&l1 Beacon&c!";
                            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                            player.sendMessage(coloredMessage);
                        }
                    }
                }

//                player.sendMessage(ChatColor.GOLD + "ID: " + questId);
//                player.sendMessage(ChatColor.YELLOW + "Notified: " + notified);
//                player.sendMessage(villager.getCustomName());
            }
        }
    }

    public boolean checkVenusArmor(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        boolean hasHelmet = false;
        boolean hasElytra = false;
        boolean hasLeggings = false;
        boolean hasBoots = false;

        for (ItemStack item : armorContents) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    String name = ChatColor.stripColor(meta.getDisplayName());
                    System.out.println(name);
                    if (item.getType() == Material.LEATHER_HELMET && name.equals("ᴠᴇɴᴜꜱ ʜᴇʟᴍᴇᴛ")) hasHelmet = true;
                    if (item.getType() == Material.ELYTRA && name.equals("ᴠᴇɴᴜꜱ ᴇʟʏᴛʀᴀ")) hasElytra = true;
                    if (item.getType() == Material.LEATHER_LEGGINGS && name.equals("ᴠᴇɴᴜꜱ ᴘᴀɴᴛꜱ")) hasLeggings = true;
                    if (item.getType() == Material.LEATHER_BOOTS && name.equals("ᴠᴇɴᴜꜱ ʙᴏᴏᴛꜱ")) hasBoots = true;
                }
            }
        }

        if (!hasHelmet) hasHelmet = containsNamedItem(inventory, Material.LEATHER_HELMET, "ᴠᴇɴᴜꜱ ʜᴇʟᴍᴇᴛ");
        if (!hasElytra) hasElytra = containsNamedItem(inventory, Material.ELYTRA, "ᴠᴇɴᴜꜱ ᴇʟʏᴛʀᴀ");
        if (!hasLeggings) hasLeggings = containsNamedItem(inventory, Material.LEATHER_LEGGINGS, "ᴠᴇɴᴜꜱ ᴘᴀɴᴛꜱ");
        if (!hasBoots) hasBoots = containsNamedItem(inventory, Material.LEATHER_BOOTS, "ᴠᴇɴᴜꜱ ʙᴏᴏᴛꜱ");

        return hasHelmet && hasElytra && hasLeggings && hasBoots;
    }

    private boolean containsNamedItem(PlayerInventory inventory, Material material, String name) {
        for (ItemStack item : inventory.all(material).values()) {
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}