package me.PCPSells.playerplaytime.hooks;

import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.util.PlayTimeManager;
import me.PCPSells.playerplaytime.util.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlayTimePlaceholder extends PlaceholderExpansion {

  private final PlayerPlayTime plugin;

  public PlayTimePlaceholder(PlayerPlayTime plugin) {
    this.plugin = plugin;
  }

  public String getIdentifier() {
    return "playtime";
  }

  public String getAuthor() {
    return "PCPSells";
  }

  public String getVersion() {
    return this.plugin.getDescription().getVersion();
  }

  public String onRequest(OfflinePlayer player, String identifier) {
    if (player == null || !player.hasPlayedBefore()) return "";

    if (identifier.equalsIgnoreCase("playtime")) {
      return PlayTimeManager.getFormattedPlayTime(player.getUniqueId());
    }
    try {
      OfflinePlayer target = Utils.toOfflinePlayer(
        identifier.replace("_playtime", "")
      );
      if (target != null && (target.isOnline() || target.hasPlayedBefore())) {
        return PlayTimeManager.getFormattedPlayTime(target.getUniqueId());
      }
    } catch (Exception ex) {}
    return "";
  }
}
