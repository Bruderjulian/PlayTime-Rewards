package me.PCPSells.playerplaytime.hooks;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

public class EssentialsHook {

  private final File userdataFolder;
  private final boolean available;

  public EssentialsHook() {
    File essentialsFolder = null;
    if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
      essentialsFolder =
        Bukkit.getPluginManager().getPlugin("Essentials").getDataFolder();
    }

    if (essentialsFolder != null && essentialsFolder.exists()) {
      this.userdataFolder = new File(essentialsFolder, "userdata");
      this.available = this.userdataFolder.exists();
    } else {
      this.userdataFolder = null;
      this.available = false;
    }
  }

  public boolean isAvailable() {
    return this.available;
  }

  public long getPlayTimeSeconds(OfflinePlayer player) {
    if (!this.available || player == null) return -1L;
    UUID uuid = player.getUniqueId();
    File userFile = new File(
      this.userdataFolder,
      String.valueOf(uuid) + ".yml"
    );
    if (!userFile.exists()) return -1L;

    try {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(userFile);
      Map<String, Object> data = config.getValues(true);

      Object timestampsObj = data.get("timestamps");
      if (!(timestampsObj instanceof Map)) return -1L;
      Object playtimeObj = ((Map<?, ?>) timestampsObj).get("playtime");

      if (playtimeObj instanceof Number) {
        return ((Number) playtimeObj).longValue();
      }
      if (playtimeObj instanceof Long) {
        return ((Long) playtimeObj).longValue();
      }
      if (playtimeObj instanceof Double) {
        return ((Double) playtimeObj).longValue();
      }
      if (!(playtimeObj instanceof String)) {
        return -1L;
      }
      return Long.parseLong((String) playtimeObj);
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1L;
    }
  }
}
