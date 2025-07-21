package me.PCPSells.playerplaytime;

import me.PCPSells.playerplaytime.commands.PlayTimeCommand;
import me.PCPSells.playerplaytime.gui.RewardsGUI;
import me.PCPSells.playerplaytime.gui.TopPlaytimeGUI;
import me.PCPSells.playerplaytime.hooks.PlayTimePlaceholder;
import me.PCPSells.playerplaytime.listeners.JoinListener;
import me.PCPSells.playerplaytime.util.PlayTimeManager;
import me.PCPSells.playerplaytime.util.RewardManager;
import me.PCPSells.playerplaytime.util.Text;
import me.PCPSells.playerplaytime.util.UpdateChecker;
import me.PCPSells.playerplaytime.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerPlayTime extends JavaPlugin {

  public static PlayerPlayTime instance;
  public UpdateChecker updateChecker;

  public void onLoad() {
    instance = this;
    this.saveDefaultConfig();
    Text.reload();

    if (this.getConfig().getBoolean("update-check.enabled", true)) {
      this.updateChecker = new UpdateChecker(this);
      this.updateChecker.checkForUpdates();
    }
  }

  public void onEnable() {
    this.getServer()
      .getPluginManager()
      .registerEvents(new JoinListener(), this);
    this.getServer()
      .getPluginManager()
      .registerEvents(new TopPlaytimeGUI(), this);
    this.getServer().getPluginManager().registerEvents(new RewardsGUI(), this);

    registerCommand("playtime", new PlayTimeCommand(this));

    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      (new PlayTimePlaceholder(this)).register();
      Text.info("PlaceholderAPI detected — placeholders registered.");
    }

    Utils.reload();
    RewardsGUI.reload();
    TopPlaytimeGUI.reload();
    RewardManager.init(this);
    PlayTimeManager.init(this);
  }

  public void registerCommand(String name, TabExecutor executor) {
    PluginCommand command = this.getCommand(name);
    if (command == null) {
      Text.warn("Could not register commands!");
      return;
    }
    command.setExecutor(executor);
    command.setTabCompleter(executor);
  }

  public void onDisable() {
    PlayTimeManager.saveAll();
    Text.info("PlayerPlayTime disabled.");
  }

  public void reload() {
    this.reloadConfig();
    Text.reload();
    Utils.reload();
    RewardManager.reload();
    RewardsGUI.reload();
    TopPlaytimeGUI.reload();
  }
}
