package org.spoutcraft.launcher;

import java.io.File;

public class SettingsUtil {
  public static final String     DEFAULT_LAUNCHER_PROPERTIES = "defaults/launcher.properties";
  public static File             settingsFile                = new File(PlatformUtils.getWorkingDirectory(), "launcher.properties");
  private static SettingsHandler settings                    = new SettingsHandler(DEFAULT_LAUNCHER_PROPERTIES, settingsFile);

  static {
    settings.load();
  }

  public static void reload() {
    settings = new SettingsHandler(DEFAULT_LAUNCHER_PROPERTIES, settingsFile);
    settings.load();
  }

  public static void init() {
    isLatestLWJGL();
    isWorldBackup();
    getLoginTries();
    isRecommendedBuild();
    isDevelopmentBuild();
    getMemorySelection();
  }

  public static boolean isLatestLWJGL() {
    return isProperty("latestLWJGL");
  }

  public static void setLatestLWJGL(boolean value) {
    setProperty("latestLWJGL", value);
  }

  public static boolean isWorldBackup() {
    return isProperty("worldbackup");
  }

  public static void setWorldBackup(boolean value) {
    setProperty("worldbackup", value);
  }

  public static int getLoginTries() {
    return isProperty("retryLogins", true) ? 3 : 1;
  }

  public static void setLoginTries(boolean value) {
    setProperty("retryLogins", value);
  }

  public static boolean isRecommendedBuild() {
    return isProperty("recupdate", true);
  }

  public static void setRecommendedBuild(boolean value) {
    setProperty("recupdate", value);
  }

  public static String getSelectedBuild() {
    return getProperty("custombuild", "'6'");
  }

  public static void setSelectedBuild(String value) {
    setProperty("custombuild", value);
  }

  public static boolean isDevelopmentBuild() {
    return isProperty("devupdate");
  }

  public static void setDevelopmentBuild(boolean value) {
    setProperty("devupdate", value);
  }

  public static boolean hasModPack() {
    return hasProperty("modpack");
  }

  public static String getModPackSelection() {
    return getProperty("modpack", null);
  }

  public static void setModPack(String value) {
    setProperty("modpack", value);
  }

  public static int getMemorySelection() {
    return getProperty("memory", 1);
  }

  public static void setMemorySelection(int value) {
    setProperty("memory", value);
  }

  private static void setProperty(String s, Object value) {
    if (settings.checkProperty(s))
      settings.changeProperty(s, value);
    else
      settings.put(s, value);
  }

  private static boolean isProperty(String s) {
    return isProperty(s, false);
  }

  private static boolean hasProperty(String name) {
    return settings.checkProperty(name);
  }

  private static boolean isProperty(String s, boolean def) {
    if (settings.checkProperty(s)) {
      return settings.getPropertyBoolean(s);
    }
    settings.put(s, def);
    return def;
  }

  private static int getProperty(String s, int def) {
    if (settings.checkProperty(s)) {
      return settings.getPropertyInteger(s);
    }
    settings.put(s, def);
    return def;
  }

  private static String getProperty(String s, String def) {
    if (settings.checkProperty(s)) {
      return settings.getPropertyString(s);
    }
    settings.put(s, def);
    return def;
  }

}
