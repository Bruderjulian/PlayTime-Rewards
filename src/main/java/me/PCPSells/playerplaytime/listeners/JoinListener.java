package me.PCPSells.playerplaytime.listeners;

import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.util.Text;
import me.PCPSells.playerplaytime.util.Text.Replaceable;
import me.PCPSells.playerplaytime.util.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    PlayerPlayTime plugin = PlayerPlayTime.instance;
    if (
      !plugin.getConfig().getBoolean("update-check.enabled", true) ||
      !player.hasPermission("playtime.admin")
    ) {
      return;
    }

    UpdateChecker checker = plugin.updateChecker;
    if (checker == null || !checker.isUpdateAvailable()) return;

    String msg = plugin.getConfig().getString("update-check.message");
    Text.send(
      player,
      msg,
      new Replaceable("%latest%", checker.latestVersion),
      new Replaceable("%current%", checker.currectVersion)
    );
  }
}
