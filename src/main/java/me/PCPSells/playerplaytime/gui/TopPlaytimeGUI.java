package me.PCPSells.playerplaytime.gui;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.util.PlayTimeManager;
import me.PCPSells.playerplaytime.util.Text;
import me.PCPSells.playerplaytime.util.Text.Replaceable;
import me.PCPSells.playerplaytime.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class TopPlaytimeGUI implements Listener {

  private static String title;

  public static void reload() {
    title =
      Text.format(
        PlayerPlayTime.instance
          .getConfig()
          .getString("guis.top.title", "&bTop Playtimes"),
        true,
        false
      );
  }

  public static void openTopPlaytimeGUI(Player viewer) {
    int amount = PlayerPlayTime.instance
      .getConfig()
      .getInt("guis.top.amount", 10);
    Map<UUID, Long> top = PlayTimeManager.getTopPlaytimes(amount);
    int size = ((top.size() - 1) / 9 + 1) * 9;

    Inventory gui = Bukkit.createInventory((InventoryHolder) null, size, title);
    int index = 0;
    int position = 1;

    for (Entry<UUID, Long> entry : top.entrySet()) {
      OfflinePlayer offlinePlayer = Utils.toOfflinePlayer(
        (UUID) entry.getKey()
      );
      ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) skull.getItemMeta();
      if (meta != null) {
        meta.setOwningPlayer(offlinePlayer);
        String nameTemplate = PlayerPlayTime.instance
          .getConfig()
          .getString("guis.top.player-name", "&b%position%. &f%player%");
        String playtimeTemplate = PlayerPlayTime.instance
          .getConfig()
          .getString("guis.top.player-playtime", "&7%playtime%");
        String name = Text.format(
          nameTemplate,
          true,
          false,
          new Replaceable("%position%", String.valueOf(position)),
          new Replaceable(
            "%player%",
            offlinePlayer.getName() == null
              ? "Unknown"
              : offlinePlayer.getName()
          )
        );
        String playtime = Text.format(
          playtimeTemplate,
          true,
          false,
          new Replaceable(
            "%playtime%",
            Utils.formatTime((Long) entry.getValue())
          )
        );
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(playtime));
        skull.setItemMeta(meta);

        position++;
      }

      gui.setItem(index++, skull);
    }
    viewer.openInventory(gui);
  }

  @EventHandler
  public void onTopPlayTimeGUIInteract(InventoryClickEvent e) {
    if (e.getView().getTitle().equalsIgnoreCase(title)) {
      e.setCancelled(true);
    }
  }
}
