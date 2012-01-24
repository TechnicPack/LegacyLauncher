package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.util.config.Configuration;

public class MinecraftYML {
	private static final String MINECRAFT_YML = "minecraft.yml";
	private static volatile boolean updated = false;
	private static File minecraftYML = new File(GameUpdater.workDir, MINECRAFT_YML);
	private static String latest = null;
	private static String recommended = null;
	private static Object key = new Object();
	
	public static Configuration getMinecraftYML() {
		updateMinecraftYMLCache();
		Configuration config = new Configuration(minecraftYML);
		config.load();
		return config;
	}
	
	public static String getLatestMinecraftVersion() {
		updateMinecraftYMLCache();
		return latest;
	}
	
	public static String getRecommendedMinecraftVersion() {
		updateMinecraftYMLCache();
		return recommended;
	}
	
	public static void setInstalledVersion(String version) {
		Configuration config = getMinecraftYML();
		config.setProperty("current", version);
		config.save();
	}
	
	public static String getInstalledVersion() {
		Configuration config = getMinecraftYML();
		return config.getString("current");
	}
	
	public static void updateMinecraftYMLCache() {
		if (!updated) {
			synchronized(key) {
				String current = null;
				if (minecraftYML.exists()) {
					try {
						Configuration config = new Configuration(minecraftYML);
						config.load();
						current = config.getString("current");
					}
					catch (Exception ex){
						ex.printStackTrace();
					}
				}
				
				if (YmlUtils.downloadYmlFile(MINECRAFT_YML, "http://technic.freeworldsgaming.com/minecraft.yml", minecraftYML)) {
					Configuration config = new Configuration(minecraftYML);
					config.load();
					latest = config.getString("latest");
					recommended = config.getString("recommended");
					if (current != null) {
						config.setProperty("current", current);
						config.save();
					}
				}
				updated = true;
			}
		}
	}
}
