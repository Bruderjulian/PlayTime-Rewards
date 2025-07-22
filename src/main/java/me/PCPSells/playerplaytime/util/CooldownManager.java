package me.PCPSells.playerplaytime.util;

import java.util.HashMap;
import java.util.UUID;
import me.PCPSells.playerplaytime.PlayerPlayTime;

public class CooldownManager {

  private static final HashMap<UUID, Long> cooldowns = new HashMap<>();
  private static long COOLDOWN_MILLIS = 2000; // 2 seconds
  private static boolean isCooldownEnabled = true;

  public static void reload() {
    cooldowns.clear();
    isCooldownEnabled =
      PlayerPlayTime.instance.getConfig().getBoolean("cooldown.enabled", true);
    COOLDOWN_MILLIS =
      PlayerPlayTime.instance.getConfig().getInt("cooldown.time", 2000) * 1000;
  }

  public static boolean isOnCooldown(UUID playerUUID) {
    if (!isCooldownEnabled || !cooldowns.containsKey(playerUUID)) {
      return false;
    }
    long cooldownEndTime = cooldowns.get(playerUUID);
    return System.currentTimeMillis() < cooldownEndTime;
  }

  public static long getCooldownLeft(UUID playerUUID) {
    if (!isCooldownEnabled || !cooldowns.containsKey(playerUUID)) {
      return 0;
    }
    long cooldownEndTime = cooldowns.get(playerUUID);
    long remainingTime = cooldownEndTime - System.currentTimeMillis();
    return Math.max(0, Math.round(remainingTime / 1000));
  }

  public static void start(UUID playerUUID) {
    if (!isCooldownEnabled) return;
    long cooldownEndTime = System.currentTimeMillis() + COOLDOWN_MILLIS;
    cooldowns.put(playerUUID, cooldownEndTime);
  }

  public static void remove(UUID playerUUID) {
    if (!isCooldownEnabled) return;
    cooldowns.remove(playerUUID);
  }

  public static void clear() {
    cooldowns.clear();
  }
}
