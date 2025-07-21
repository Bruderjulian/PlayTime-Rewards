package me.PCPSells.playerplaytime.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RewardManager {

  private static final HashMap<UUID, Set<String>> achievedUnique = new HashMap<UUID, Set<String>>();
  private static final HashMap<UUID, Set<String>> achievedDaily = new HashMap<UUID, Set<String>>();
  private static File rewardsFile;
  private static YamlConfiguration rewardsConfig;
  private static File achievedFile;
  private static YamlConfiguration achievedConfig;

  public static void init(PlayerPlayTime plugin) {
    rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
    if (!rewardsFile.exists()) {
      plugin.saveResource("rewards.yml", false);
    }

    rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    achievedFile = new File(plugin.getDataFolder(), "achieved-rewards.yml");
    if (!achievedFile.exists()) {
      try {
        achievedFile.createNewFile();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    achievedConfig = YamlConfiguration.loadConfiguration(achievedFile);
    loadClaimedData();
    Text.info("RewardManager data initialized.");
  }

  public static void reload() {
    rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    achievedUnique.clear();
    achievedDaily.clear();
    loadClaimedData();
  }

  private static void loadClaimedData() {
    for (String uuidStr : achievedConfig.getKeys(false)) {
      UUID uuid = UUID.fromString(uuidStr);
      achievedUnique.put(
        uuid,
        new HashSet<>(achievedConfig.getStringList(uuidStr + ".unique"))
      );
      achievedDaily.put(
        uuid,
        new HashSet<>(achievedConfig.getStringList(uuidStr + ".daily"))
      );
    }
  }

  private static void saveClaimedData() {
    for (UUID uuid : achievedUnique.keySet()) {
      String path = uuid.toString();
      achievedConfig.set(
        path + ".unique",
        new ArrayList<>(achievedUnique.get(uuid))
      );
      achievedConfig.set(
        path + ".daily",
        new ArrayList<>(achievedDaily.getOrDefault(uuid, new HashSet<String>()))
      );
    }

    try {
      achievedConfig.save(achievedFile);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void handleSecond(
    Player player,
    long totalSeconds,
    long sessionSeconds
  ) {
    handleConstant(player, totalSeconds);
    handleUnique(player, totalSeconds);
    handlePerSession(player, sessionSeconds);
  }

  private static void handleConstant(Player player, long totalSeconds) {
    ConfigurationSection section = rewardsConfig.getConfigurationSection(
      "constant"
    );
    if (section == null) return;
    for (String key : section.getKeys(false)) {
      long interval = section.getLong(key + ".interval");
      if (interval <= 0L || totalSeconds % interval != 0L) {
        continue;
      }

      List<String> permissionsRequired = section.getStringList(
        key + ".permissions-required"
      );
      boolean hasAll = true;
      if (!permissionsRequired.isEmpty()) {
        for (String perm : permissionsRequired) {
          if (!player.hasPermission(perm)) {
            hasAll = false;
            break;
          }
        }
      }

      if (hasAll) runReward(player, section, key);
    }
  }

  private static void handleUnique(Player player, long totalSeconds) {
    ConfigurationSection section = rewardsConfig.getConfigurationSection(
      "unique"
    );
    if (section == null) return;
    UUID uuid = player.getUniqueId();
    achievedUnique.putIfAbsent(uuid, new HashSet<>());

    for (String key : section.getKeys(false)) {
      if (((Set<String>) achievedUnique.get(uuid)).contains(key)) continue;
      long threshold = section.getLong(key + ".at");
      if (totalSeconds < threshold) continue;
      List<String> permissionsRequired = section.getStringList(
        key + ".permissions-required"
      );
      boolean hasAll = true;
      if (!permissionsRequired.isEmpty()) {
        for (String perm : permissionsRequired) {
          if (!player.hasPermission(perm)) {
            hasAll = false;
            break;
          }
        }
      }

      if (hasAll) {
        runReward(player, section, key);
        ((Set<String>) achievedUnique.get(uuid)).add(key);
        saveClaimedData();
      }
    }
  }

  private static void handlePerSession(Player player, long sessionSeconds) {
    ConfigurationSection section = rewardsConfig.getConfigurationSection(
      "per-session"
    );
    if (section == null) return;
    UUID uuid = player.getUniqueId();
    achievedDaily.putIfAbsent(uuid, new HashSet<>());
    String todayKey = LocalDate.now().toString();

    for (String key : section.getKeys(false)) {
      String dailyKey = todayKey + "_" + key;
      if (((Set<String>) achievedDaily.get(uuid)).contains(dailyKey)) continue;
      long interval = section.getLong(key + ".interval");
      if (sessionSeconds < interval) continue;
      List<String> permissionsRequired = section.getStringList(
        key + ".permissions-required"
      );
      boolean hasAll = true;
      if (!permissionsRequired.isEmpty()) {
        for (String perm : permissionsRequired) {
          if (!player.hasPermission(perm)) {
            hasAll = false;
            break;
          }
        }
      }

      if (hasAll) {
        runReward(player, section, key);
        ((Set<String>) achievedDaily.get(uuid)).add(dailyKey);
        saveClaimedData();
      }
    }
  }

  private static void runReward(
    Player player,
    ConfigurationSection section,
    String key
  ) {
    String command = section.getString(key + ".command");
    if (command != null) {
      Bukkit.dispatchCommand(
        Bukkit.getConsoleSender(),
        command.replace("%player%", player.getName())
      );
    }

    String message = section.getString(key + ".message");
    if (message != null) {
      Text.send(player, message);
    }
  }

  public static boolean hasAchieved(
    Player player,
    String sectionName,
    String key
  ) {
    switch (sectionName) {
      case "unique":
        return (
          (Set<String>) achievedUnique.getOrDefault(
            player.getUniqueId(),
            Set.of()
          )
        ).contains(key);
      case "constant":
        return false;
      case "per-session":
        String todayKey = LocalDate.now().toString() + "_" + key;
        return (
          (Set<String>) achievedDaily.getOrDefault(
            player.getUniqueId(),
            Collections.emptySet()
          )
        ).contains(todayKey);
    }

    return false;
  }

  public static void clearAllRewards(UUID uuid) {
    achievedUnique.remove(uuid);
    achievedDaily.remove(uuid);
    achievedConfig.set(uuid.toString(), null);
    saveClaimedData();
  }

  public static void clearRewardsByType(UUID uuid, String type) {
    switch (type.toLowerCase()) {
      case "unique":
        achievedUnique.remove(uuid);
        achievedConfig.set(uuid.toString() + ".unique", null);
        break;
      case "per-session":
        achievedDaily.remove(uuid);
        achievedConfig.set(uuid.toString() + ".daily", null);
        break;
    }

    saveClaimedData();
  }

  public static YamlConfiguration getRewardsConfig() {
    return rewardsConfig;
  }
}
