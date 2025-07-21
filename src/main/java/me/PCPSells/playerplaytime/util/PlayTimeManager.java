package me.PCPSells.playerplaytime.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import me.PCPSells.playerplaytime.hooks.EssentialsHook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayTimeManager {

  private static final Map<UUID, Long> playtimes = new ConcurrentHashMap<>();
  private static File dataFile;
  private static FileConfiguration dataConfig;
  private static final Map<UUID, Long> todaySeconds = new ConcurrentHashMap<>();
  private static final Map<UUID, LocalDate> todayDate = new ConcurrentHashMap<>();
  private static EssentialsHook essentialsHook;

  public static void init(PlayerPlayTime plugin) {
    dataFile = new File(plugin.getDataFolder(), "data.yml");
    if (!dataFile.exists()) {
      try {
        dataFile.createNewFile();
      } catch (IOException ex) {
        Text.warn("Failed to create data.yml");
        ex.printStackTrace();
      }

      if (
        PlayerPlayTime.instance
          .getConfig()
          .getBoolean("essentials-integration.enabled", false)
      ) {
        essentialsHook = new EssentialsHook();
      }
    }

    dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    for (String uuidStr : dataConfig.getKeys(false)) {
      playtimes.put(UUID.fromString(uuidStr), dataConfig.getLong(uuidStr));
    }

    Bukkit
      .getScheduler()
      .runTaskTimer(
        plugin,
        () -> {
          LocalDate now = LocalDate.now();
          for (Player p : Bukkit.getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            long total = (Long) playtimes.merge(uuid, 1L, Long::sum);
            if (!now.equals(todayDate.get(uuid))) {
              todayDate.put(uuid, now);
              todaySeconds.put(uuid, 0L);
            }

            long session = (Long) todaySeconds.merge(uuid, 1L, Long::sum);
            RewardManager.handleSecond(p, total, session);
          }
        },
        20L,
        20L
      );
  }

  public static String getFormattedPlayTime(Player p) {
    long playtime = getPlayTime(p.getUniqueId());
    return Utils.formatTime(playtime);
  }

  public static String getFormattedPlayTime(UUID uuid) {
    long playtime = getPlayTime(uuid);
    return Utils.formatTime(playtime);
  }

  public static long getPlayTime(UUID uuid) {
    if (essentialsHook != null && essentialsHook.isAvailable()) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null && player.isOnline()) {
        long essTime = essentialsHook.getPlayTimeSeconds(player);
        if (essTime >= 0L) return essTime;
      }
    }

    return (Long) playtimes.getOrDefault(uuid, 0L);
  }

  public static void saveAll() {
    for (Entry<UUID, Long> entry : playtimes.entrySet()) {
      dataConfig.set(((UUID) entry.getKey()).toString(), entry.getValue());
    }

    try {
      dataConfig.save(dataFile);
    } catch (IOException ex) {
      Text.warn("Failed to save data.yml");
      ex.printStackTrace();
    }
  }

  public static Map<UUID, Long> getTopPlaytimes(int amount) {
    return playtimes
      .entrySet()
      .stream()
      .sorted((a, b) -> {
        return Long.compare((Long) b.getValue(), (Long) a.getValue());
      })
      .limit((long) amount)
      .collect(
        LinkedHashMap::new,
        (map, entry) -> {
          map.put((UUID) entry.getKey(), (Long) entry.getValue());
        },
        Map::putAll
      );
  }
}
