package org.spoutcraft.launcher.modpacks;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.YmlUtils;

@SuppressWarnings("unchecked")
public class ModPackYML {

  private static final String     MODPACK_YML  = "modpack.yml";
  private static final String     FALLBACK_URL = String.format("http://technic.freeworldsgaming.com/%s", MODPACK_YML);

  private static volatile boolean updated      = false;
  private static final Object     key          = new Object();

  private static String           recommendedBuild, latestBuild;

  private static File getModPackYMLFile() {
    return new File(ModPackListYML.currentModPackDirectory, MODPACK_YML);
  }

  public static Configuration getModPackYML() {
    updateModPackYML();
    Configuration config = new Configuration(getModPackYMLFile());
    config.load();
    return config;
  }

  public static void updateModPackYML() {
    updateModPackYML(false);
  }

  public static void updateModPackYML(boolean doUpdate) {
    if (doUpdate || !updated) {
      synchronized (key) {
        String selected = getSelectedBuild();

        YmlUtils.downloadYmlFile(ModPackListYML.currentModPack + "/" + MODPACK_YML, FALLBACK_URL, getModPackYMLFile());

        Configuration config = new Configuration(getModPackYMLFile());
        config.load();
        config.setProperty("current", selected);
        config.setProperty("launcher", Main.build);
        config.save();

        updated = true;
      }
    }
  }

  private static String getSelectedBuild() {
    String selected = null;
    if (getModPackYMLFile().exists()) {
      try {
        Configuration config = new Configuration(getModPackYMLFile());
        config.load();
        selected = config.getString("current");
        if (selected == null || !isValidBuild(selected)) {
          selected = config.getString("recommended");
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return selected;
  }

  private static boolean isValidBuild(String selected) {
    return !selected.equals("-1");
  }

  public static List<Map<String, String>> getModList() {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getModPackIcon() {
    return Util.getResourceFile(ModPackListYML.getIconName()).getAbsolutePath();
  }

  public static String getModPackLogo() {
    return Util.getResourceFile("logo.png").getAbsolutePath();
  }

  public static String getModPackFavIcon() {
    return Util.getResourceFile("favicon.png").getAbsolutePath();
  }

  public static String getLatestBuild() {
    return getModPackYML().getString("latest", null);
  }

  public static String getRecommendedBuild() {
    return getModPackYML().getString("recommended", null);
  }

  public static String[] getModpackBuilds() {
    Configuration config = getModPackYML();
    Map<String, Object> builds = (Map<String, Object>) config.getProperty("builds");
    latestBuild = config.getString("latest", null);
    recommendedBuild = config.getString("recommended", null);

    if (builds != null) {
      String[] results = new String[builds.size()];
      int index = 0;
      for (String i : builds.keySet()) {
        results[index] = i.toString();
        Map<String, Object> map = (Map<String, Object>) builds.get(i);
        String version = String.valueOf(map.get("minecraft"));
        results[index] += "| " + version;
        if (i.equals(latestBuild)) {
          results[index] += " | Latest";
        }
        if (i.equals(recommendedBuild)) {
          results[index] += " | Rec. Build";
        }
        index++;
      }
      return results;
    }
    return null;
  }
}
