package me.PCPSells.playerplaytime.util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Utils {

  public static List<String> configuredUnits;
  public static boolean pluralize;

  public static void reload() {
    configuredUnits =
      PlayerPlayTime.instance.getConfig().getStringList("time-format-units");
    if (configuredUnits.isEmpty()) {
      configuredUnits = List.of("day", "hour", "minute", "second");
    }
    pluralize =
      PlayerPlayTime.instance
        .getConfig()
        .getBoolean("append-formats-with-an-s", true);
  }

  @SuppressWarnings("deprecation")
  public static OfflinePlayer toOfflinePlayer(String name) {
    try {
      OfflinePlayer player = Bukkit.getOfflinePlayer(name);
      if (player == null) {
        player = Bukkit.getOfflinePlayer(UUID.fromString(name));
      }
      return player;
    } catch (Exception e) {
      return null;
    }
  }

  public static OfflinePlayer toOfflinePlayer(UUID uuid) {
    try {
      return Bukkit.getOfflinePlayer(uuid);
    } catch (Exception e) {
      return null;
    }
  }

  public static String formatTime(long seconds) {
    long days = seconds / 86400L;
    seconds %= 86400L;
    long hours = seconds / 3600L;
    seconds %= 3600L;
    long minutes = seconds / 60L;
    seconds %= 60L;
    PlayerPlayTime plugin = PlayerPlayTime.instance;

    HashMap<String, Long> units = new HashMap<>();
    units.put("day", days);
    units.put("hour", hours);
    units.put("minute", minutes);
    units.put("second", seconds);

    StringBuilder sb = new StringBuilder();
    for (String unit : configuredUnits) {
      long value = (Long) units.getOrDefault(unit, 0L);
      if (value <= 0L) continue;

      String label = plugin
        .getConfig()
        .getString("time-formats." + unit, " " + unit);
      sb.append(value).append(label);
      if (pluralize && value != 1L) {
        sb.append("s");
      }
      sb.append(" ");
    }

    if (sb.length() == 0) {
      sb
        .append("0")
        .append(plugin.getConfig().getString("time-formats.second", " second"));
      if (pluralize) sb.append("s");
    }

    return sb.toString().trim();
  }

  public static long parseTime(String string) {
    long time = 0L;
    String[] parts = string.split(" ");
    for (String part : parts) {
      String[] split = part.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
      long value = Long.parseLong(split[0]);
      String unit = split[1];
      switch (unit) {
        case "d":
          time += value * 86400L;
          break;
        case "h":
          time += value * 3600L;
          break;
        case "m":
          time += value * 60L;
          break;
        case "s":
          time += value;
          break;
      }
    }
    return time;
  }
}
