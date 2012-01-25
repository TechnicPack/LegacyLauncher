package org.spoutcraft.launcher.modpacks;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.YmlUtils;

public class ModPackListYML {
	private static final String ICON_ICO = "icon.ico";
	private static final String ICON_ICNS = "icon.icns";
	private static final String ICON_PNG = "icon.png";
	private static final String FAVICON_PNG = "favicon.png";
	private static final String LOGO_PNG = "logo.png";
	
	private static final List<String> RESOURCES = new LinkedList();
	
	private static final String MODPACKS_YML = "modpacks.yml";
	private static final File MODPACKS_YML_FILE = new File(GameUpdater.workDir, MODPACKS_YML);
	
	private static volatile boolean updated = false;
	private static Object key = new Object();

	public static String currentModPack = null;
	public static String currentModPackLabel = null;
	public static File currentModPackDirectory = null;
	
	public static Image favIcon = null;
	public static Image icon = null;
	public static Image logo = null;
	
	static {
		RESOURCES.add(FAVICON_PNG);
		RESOURCES.add(LOGO_PNG);
		RESOURCES.add(getIconName());
	}
	
	public static String getIconName() {
		if (PlatformUtils.getPlatform() == PlatformUtils.OS.windows) {
			return ICON_PNG;
		} else if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
			return ICON_ICNS;
		}
		return ICON_PNG;
	}

	public static Configuration getModPacksYML() {
		updateModPacksYMLCache();
		Configuration config = new Configuration(MODPACKS_YML_FILE);
		config.load();
		return config;
	}
	
	public static void updateModPacksYMLCache() {
		if (!updated) {
			synchronized(key) {
				YmlUtils.downloadRelativeYmlFile(MODPACKS_YML);
				updated = true;
			}
		}
	}
	
	public static void setCurrentModpack() {
		Map<String, String> modPackMap = getModPacks();
		if (!SettingsUtil.hasModPack()) {
			Map.Entry<String, String> modpack = modPackMap.entrySet().iterator().next();
			setModPack(modpack.getKey(), modpack.getValue(), true);
		} else {
			String modPack = SettingsUtil.getModPackSelection();
			setModPack(modPack, modPackMap.get(modPack));
		}
	}

	public static Map<String, String> getModPacks()
	{
		return (Map<String, String>) getModPacksYML().getProperty("modpacks");
	}

	public static boolean setModPack(String modPack, String modPackLabel) {
		return setModPack(modPack, modPackLabel, false);
	}
	
	public static boolean setModPack(String modPack, String modPackLabel, boolean ignoreCheck) {
		if (modPack.equalsIgnoreCase(currentModPack))
			return true;
		
		if (!ignoreCheck) {
			Map<String, String> modPacks = getModPacks();
			if (!modPacks.containsKey(modPack)) {
				//Mod Pack not in list
				Util.log("ModPack '%s' not in '%s' file.", modPackLabel, MODPACKS_YML);
				return false;
			}
		}
		
		SettingsUtil.setModPack(modPack);
		
		currentModPack = modPack;
		currentModPackLabel = modPackLabel;
		currentModPackDirectory = new File(GameUpdater.workDir, currentModPack);
		
		currentModPackDirectory.mkdirs();
		
		//Download Branding Resources
		downloadModPackResources();
		
		//TODO: Issue #9 Swap ModPack Directories
		
		return true;
	}

	public static void downloadModPackResources() {
		Map<String, String> downloadFileList = new HashMap<String, String>();
				
		for (String resource : RESOURCES) {
			String relativeFilePath = currentModPack + "/resources/" + resource;
			
			if (MD5Utils.checksumPath(relativeFilePath)) {
				continue;
			}
			
			File dir = new File(currentModPackDirectory, "resources");
			dir.mkdirs();
			File file = new File(dir, resource);
			String filePath = file.getAbsolutePath();
			
			String fileURL = MirrorUtils.getMirrorUrl(relativeFilePath, null);
			if (fileURL == null) continue;
			downloadFileList.put(fileURL, filePath);
		}
		
		if (downloadFileList.size() > 0 && DownloadUtils.downloadFiles(downloadFileList , 30, TimeUnit.SECONDS) != downloadFileList.size()) {
			Util.log("[Error] Could not download all resources for modpack '%s'.", currentModPackLabel);
		}
	}
}
