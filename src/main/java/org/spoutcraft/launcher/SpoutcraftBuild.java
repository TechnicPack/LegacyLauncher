package org.spoutcraft.launcher;

import java.util.Map;
import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.async.DownloadListener;

public class SpoutcraftBuild {
	private String minecraftVersion;
	private String latestVersion;
	private String build;
	Map<String, Object> libraries;
	Map<String, Object> mods;
	private DownloadListener listener = null;

	private SpoutcraftBuild(String minecraft, String latest, String build, Map<String, Object> libraries, Map<String, Object> mods) {
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
		return "http://s3.amazonaws.com/MinecraftDownload/minecraft.jar?user=" + user + "&ticket=1";
	}

	public String getSpoutcraftURL() {
		return MirrorUtils.getMirrorUrl("technic/" + build + "/technic.jar", null, listener);
	}
	
	public String getTechnicZipURL() {
		return MirrorUtils.getMirrorUrl("technic/" + build + "/technic.zip", null, listener);
	}

	public void setDownloadListener(DownloadListener listener) {
		this.listener = listener;
	}

	public void install() {
		Configuration config = SpoutcraftYML.getSpoutcraftYML();
		config.setProperty("current", getBuild());
		config.save();
	}

	public String getInstalledBuild() {
		Configuration config = SpoutcraftYML.getSpoutcraftYML();
		return config.getString("current", null);
	}

	public String getPatchURL() {
		String mirrorURL = "/Patches/Minecraft/minecraft_";
		mirrorURL += getLatestMinecraftVersion();
		mirrorURL += "-" + getMinecraftVersion() + ".patch";
		String fallbackURL = "http://urcraft.com/technic/Patches/Minecraft/minecraft_";
		fallbackURL += getLatestMinecraftVersion();
		fallbackURL += "-" + getMinecraftVersion() + ".patch";
		return MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, listener);
	}
	
	public Map<String, Object> getLibraries() {
		return libraries;
	}
	
	public Map<String, Object> getMods() {
		return mods;
	}

	@SuppressWarnings("unchecked")
	public static SpoutcraftBuild getSpoutcraftBuild() {
		Configuration config = SpoutcraftYML.getSpoutcraftYML();
		Map<String, Object> builds = (Map<String, Object>) config.getProperty("builds");
		String latest = config.getString("latest", null);
		String recommended = config.getString("recommended", null);
		String selected = SettingsUtil.getSelectedBuild();
		
		String buildSelection = selected;
		
		if (SettingsUtil.isRecommendedBuild()) {
			buildSelection = recommended;
		} else if (SettingsUtil.isDevelopmentBuild()) {
			buildSelection = latest;
		}

		Map<String, Object> build = (Map<String, Object>) builds.get(buildSelection);
		Map<String, Object> libs = (Map<String, Object>) build.get("libraries");
		Map<String, Object> mods = (Map<String, Object>) build.get("mods");
		
		return new SpoutcraftBuild((String)build.get("minecraft"), MinecraftYML.getLatestMinecraftVersion(), buildSelection, libs, mods);
	}
}
