package me.PCPSells.playerplaytime.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.gui.RewardsGUI;
import me.PCPSells.playerplaytime.gui.TopPlaytimeGUI;
import me.PCPSells.playerplaytime.util.PlayTimeManager;
import me.PCPSells.playerplaytime.util.RewardManager;
import me.PCPSells.playerplaytime.util.Text;
import me.PCPSells.playerplaytime.util.Text.Replaceable;
import me.PCPSells.playerplaytime.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class PlayTimeCommand implements TabExecutor {

  private final PlayerPlayTime plugin;

  public PlayTimeCommand(PlayerPlayTime plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    if (args.length == 0) {
      if (!(sender instanceof Player)) {
        Text.send(sender, "messages.player-only");
        return true;
      }
      String playtime = PlayTimeManager.getFormattedPlayTime((Player) sender);
      Text.send(
        sender,
        "messages.playtime.self",
        new Replaceable("%playtime%", playtime)
      );
      return true;
    }

    if (!sender.hasPermission("playtime.use")) {
      Text.send(sender, "messages.no-permission");
    }

    switch (args[0]) {
      case "help":
        Text.send(sender, "messages.help");
        return true;
      case "player":
        if (!sender.hasPermission("playtime.other")) {
          Text.send(sender, "messages.no-permission");
        }
        OfflinePlayer target = Utils.toOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
          Text.send(sender, "messages.invalid-player");
        }
        String playtime = PlayTimeManager.getFormattedPlayTime(
          target.getUniqueId()
        );
        Text.send(
          sender,
          "messages.playtime.other",
          new Replaceable("%player%", target.getName()),
          new Replaceable("%playtime%", playtime)
        );
        return true;
      case "top":
        if (!sender.hasPermission("playtime.top")) {
          Text.send(sender, "messages.no-permission");
        }
        if (!(sender instanceof Player)) {
          Text.send(sender, "messages.player-only");
          return true;
        }
        TopPlaytimeGUI.openTopPlaytimeGUI((Player) sender);
        return true;
      case "rewards":
        if (!sender.hasPermission("playtime.rewards")) {
          Text.send(sender, "messages.no-permission");
        }
        if (!(sender instanceof Player)) {
          Text.send(sender, "messages.player-only");
          return true;
        }
        RewardsGUI.openRewardsGUI((Player) sender);
        return true;
      case "clearrewards":
        if (!sender.hasPermission("playtime.admin")) {
          Text.send(sender, "messages.no-permission");
          return true;
        }
        if (args.length == 1) {
          Text.send(sender, "messages.clear-rewards-breakdown");
        } else if (args.length == 2) {
          clearRewards(sender, args);
        } else if (args.length == 3) {
          clearRewards(sender, args);
        }
        return true;
      case "reload":
        if (!sender.hasPermission("playtime.admin")) {
          Text.send(sender, "messages.no-permission");
          return true;
        }
        this.plugin.reload();
        Text.send(sender, "messages.reloaded");
        return true;
      default:
        Text.send(sender, "messages.help");
        return true;
    }
  }

  public void clearRewards(CommandSender sender, String[] args) {
    OfflinePlayer target = Utils.toOfflinePlayer(args[1]);
    if (!target.hasPlayedBefore() && !target.isOnline()) {
      Text.send(sender, "messages.invalid-player");
      return;
    }
    UUID targetUUID = target.getUniqueId();
    switch (args[2].toLowerCase()) {
      case "unique":
        RewardManager.clearRewardsByType(targetUUID, "unique");
        Text.send(
          sender,
          "messages.unique-rewards-were-cleared",
          new Replaceable("%player%", target.getName())
        );
        return;
      case "per-session":
        RewardManager.clearRewardsByType(targetUUID, "per-session");
        Text.send(
          sender,
          "messages.per-session-rewards-were-cleared",
          new Replaceable("%player%", target.getName())
        );
      case "constant":
        Text.send(
          sender,
          "messages.constant-rewards-cant-be-cleared",
          new Replaceable("%player%", target.getName())
        );
        return;
      case "all":
        RewardManager.clearAllRewards(targetUUID);
        Text.send(
          sender,
          "messages.cleared-players-rewards",
          new Replaceable("%player%", target.getName())
        );
        if (
          this.plugin.getConfig()
            .getBoolean("tell-target-rewards-were-cleared") &&
          target.isOnline()
        ) {
          Player onlineTarget = Bukkit.getPlayer(targetUUID);
          if (onlineTarget != null) return;
          Text.send(onlineTarget, "messages.rewards-were-cleared");
        }
        return;
    }

    Text.send(sender, "messages.invalid-reward-type");
    return;
  }

  public List<String> onTabComplete(
    CommandSender sender,
    Command command,
    String alias,
    String[] args
  ) {
    ArrayList<String> completions = new ArrayList<String>();

    if (args.length == 1) {
      if (sender.hasPermission("playtime.admin")) {
        completions.add("reload");
        completions.add("clearrewards");
        completions.add("help");
        completions.add("player");
      }

      if (sender.hasPermission("playtime.use")) {
        completions.add("top");
        completions.add("rewards");
      }
    } else if (
      args.length == 2 &&
      sender.hasPermission("playtime.admin") &&
      (
        args[0].equalsIgnoreCase("player") ||
        args[0].equalsIgnoreCase("clearrewards")
      )
    ) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        completions.add(p.getName());
      }
    } else if (
      args.length == 3 &&
      args[0].equalsIgnoreCase("clearrewards") &&
      sender.hasPermission("playtime.admin")
    ) {
      completions.add("unique");
      completions.add("all");
      completions.add("per-session");
    }

    return completions;
  }
}
