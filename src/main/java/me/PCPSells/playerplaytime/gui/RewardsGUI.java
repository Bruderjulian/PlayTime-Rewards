package me.PCPSells.playerplaytime.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.util.RewardManager;
import me.PCPSells.playerplaytime.util.Text;
import me.PCPSells.playerplaytime.util.Text.Replaceable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardsGUI implements Listener {

  private static YamlConfiguration rewardsConfig;
  private static String title;

  public static void reload() {
    File file = new File(
      PlayerPlayTime.instance.getDataFolder(),
      "rewards.yml"
    );
    rewardsConfig = YamlConfiguration.loadConfiguration(file);
    title =
      Text.format(
        PlayerPlayTime.instance.getConfig().getString("guis.rewards.title"),
        true,
        false
      );
  }

  public static void openRewardsGUI(Player player) {
    Inventory gui = Bukkit.createInventory((InventoryHolder) null, 54, title);
    addRewardsToGUI(player, gui, "constant", 0);
    addRewardsToGUI(player, gui, "unique", 18);
    addRewardsToGUI(player, gui, "per-session", 36);
    player.openInventory(gui);
  }

  private static void addRewardsToGUI(
    Player player,
    Inventory gui,
    String sectionName,
    int startSlot
  ) {
    ConfigurationSection section = rewardsConfig.getConfigurationSection(
      sectionName
    );
    if (section == null) return;
    int slot = startSlot;

    for (String key : section.getKeys(false)) {
      if (slot >= gui.getSize()) {
        break;
      }

      boolean achieved = RewardManager.hasAchieved(player, sectionName, key);
      ConfigurationSection reward = section.getConfigurationSection(key);
      ItemStack item;
      ItemMeta meta;
      if (achieved && reward.isConfigurationSection("achieved")) {
        ConfigurationSection achievedSec = reward.getConfigurationSection(
          "achieved"
        );
        item =
          new ItemStack(
            getMaterial(achievedSec.getString("material", "PAPER"))
          );
        meta = item.getItemMeta();
        meta.setDisplayName(
          Text.format(
            achievedSec.getString("name", "Achieved - %interval%"),
            true,
            false,
            new Replaceable("%interval%", reward.getString("gui-interval", ""))
          )
        );
        meta.setLore(
          replacePlaceholder(
            achievedSec.getStringList("lore"),
            reward,
            sectionName
          )
        );
      } else {
        item =
          new ItemStack(getMaterial(reward.getString("material", "PAPER")));
        meta = item.getItemMeta();
        meta.setDisplayName(
          Text.format(
            reward.getString("name", "Reward"),
            true,
            false,
            new Replaceable("%interval%", reward.getString("gui-interval", ""))
          )
        );
        meta.setLore(
          replacePlaceholder(reward.getStringList("lore"), reward, sectionName)
        );
      }

      item.setItemMeta(meta);
      gui.setItem(slot++, item);
    }
  }

  private static List<String> replacePlaceholder(
    List<String> lore,
    ConfigurationSection reward,
    String type
  ) {
    ArrayList<String> out = new ArrayList<String>();

    for (String line : lore) {
      line = Text.format(line, true, false);
      line = line.replace("%reward%", reward.getString("reward-desc", ""));
      if (type.equals("constant")) {
        line =
          line.replace("%interval%", formatTime(reward.getLong("interval")));
      } else if (type.equals("unique")) {
        line = line.replace("%interval%", formatTime(reward.getLong("at")));
      } else if (type.equals("per-session")) {
        line =
          line.replace("%interval%", formatTime(reward.getLong("interval")));
      }
      out.add(line);
    }

    return out;
  }

  private static Material getMaterial(String name) {
    Material mat = Material.matchMaterial(name);
    return mat == null ? Material.PAPER : mat;
  }

  private static String formatTime(long seconds) {
    long h = seconds / 3600L;
    long m = seconds % 3600L / 60L;
    long s = seconds % 60L;
    return h + "h " + m + "m " + s + "s";
  }

  @EventHandler
  public void onTopPlayTimeGUIInteract(InventoryClickEvent e) {
    if (e.getView().getTitle().equalsIgnoreCase(title)) {
      e.setCancelled(true);
    }
  }
}
