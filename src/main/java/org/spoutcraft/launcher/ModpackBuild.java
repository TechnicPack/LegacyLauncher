package org.spoutcraft.launcher;

import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.async.DownloadListener;
import org.spoutcraft.launcher.modpacks.ModPackYML;

public class ModpackBuild {

  private final String     minecraftVersion;
  private final String     latestVersion;
  private final String     build;
  Map<String, Object>      libraries;
  Map<String, Object>      mods;
  private DownloadListener listener = null;

  private ModpackBuild(String minecraft, String latest, String build,
      Map<String, Object> libraries, Map<String, Object> mods) {
    this.minecraftVersion = minecraft;
    this.latestVersion = latest;
    this.build = build;
    this.libraries = libraries;
    this.mods = mods;
  }

  public String getBuild() {
    return build;
  }

  public String getMinecraftVersion() {
    return minecraftVersion;
  }

  public String getLatestMinecraftVersion() {
    return latestVersion;
  }

  public String getMinecraftURL(String user) {
    return "http://s3.amazonaws.com/MinecraftDownload/minecraft.jar?user="
        + user + "&ticket=1";
  }

  public void setDownloadListener(DownloadListener listener) {
    this.listener = listener;
  }

  public void install() {
    Configuration config = ModPackYML.getModPackYML();
    config.setProperty("current", getBuild());
    config.save();
  }

  public String getInstalledBuild() {
    Configuration config = ModPackYML.getModPackYML();
    return config.getString("current");
  }

  public String getPatchURL() {
    // String mirrorURL = "Patches/Minecraft/minecraft_";
    // mirrorURL += getLatestMinecraftVersion();
    // mirrorURL += "-" + getMinecraftVersion() + ".patch";
    // String fallbackURL =
    // "http://spout.thomasc.co.uk/Patches/Minecraft/minecraft_";
    // fallbackURL += getLatestMinecraftVersion();
    // fallbackURL += "-" + getMinecraftVersion() + ".patch";
    // return MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, listener);
    return getPatchURL(getLatestMinecraftVersion(), getMinecraftVersion());
  }

  public String getPatchURL(String oldVersion, String newVersion) {
    String mirrorURL = "Patches/Minecraft/minecraft_";
    mirrorURL += oldVersion;
    mirrorURL += "-" + newVersion + ".patch";
    String fallbackURL = "http://spout.thomasc.co.uk/Patches/Minecraft/minecraft_";
    fallbackURL += oldVersion;
    fallbackURL += "-" + newVersion + ".patch";
    return MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, listener);
  }

  public Map<String, Object> getLibraries() {
    return libraries;
  }

  public Map<String, Object> getMods() {
    return mods;
  }

  @SuppressWarnings("unchecked")
  public static ModpackBuild getSpoutcraftBuild() {
    Configuration config = ModPackYML.getModPackYML();
    Map<String, Object> builds = (Map<String, Object>) config
        .getProperty("builds");
    String latest = config.getString("latest", null);
    String recommended = config.getString("recommended", null);
    String selected = SettingsUtil.getSelectedBuild();

    String buildName = selected;
    if (SettingsUtil.isRecommendedBuild()) {
      buildName = recommended;
    } else if (SettingsUtil.isDevelopmentBuild()) {
      buildName = latest;
    }

    Map<String, Object> build = (Map<String, Object>) builds.get(buildName);
    Map<String, Object> libs = (Map<String, Object>) build.get("libraries");
    Map<String, Object> mods = (Map<String, Object>) build.get("mods");
    String minecraftVersion = build.get("minecraft").toString();
    return new ModpackBuild(minecraftVersion,
        MinecraftYML.getLatestMinecraftVersion(), buildName, libs, mods);
  }
}
