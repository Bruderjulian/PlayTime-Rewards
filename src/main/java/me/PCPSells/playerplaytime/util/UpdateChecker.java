package me.PCPSells.playerplaytime.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import me.PCPSells.playerplaytime.PlayerPlayTime;
import org.bukkit.Bukkit;

public class UpdateChecker {

  private final PlayerPlayTime plugin;
  public String latestVersion;
  public String currectVersion;

  public UpdateChecker(PlayerPlayTime plugin) {
    this.plugin = plugin;
  }

  public void checkForUpdates() {
    Bukkit
      .getScheduler()
      .runTaskAsynchronously(
        plugin,
        () -> {
          try {
            URL url = java.net.URI
              .create(
                "https://api.spigotmc.org/legacy/update.php?resource=44852"
              )
              .toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            Scanner scanner = new Scanner(conn.getInputStream());
            if (scanner.hasNext()) {
              latestVersion = scanner.next();
              Text.info("Latest version on SpigotMC: " + latestVersion);
            }
            scanner.close();
            currectVersion = plugin.getDescription().getVersion();
          } catch (Exception e) {
            Text.warn("Could not check for updates: " + e.getMessage());
            e.printStackTrace();
          }
        }
      );
  }

  public boolean isUpdateAvailable() {
    if (latestVersion == null) {
      return false;
    }
    return !currectVersion.equalsIgnoreCase(latestVersion);
  }
}
