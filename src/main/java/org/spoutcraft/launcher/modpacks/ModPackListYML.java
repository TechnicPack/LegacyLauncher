package org.spoutcraft.launcher.modpacks;

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SettingsUtil;
import org.spoutcraft.launcher.Util;
import org.spoutcraft.launcher.YmlUtils;

public class ModPackListYML {
	private static final String ICON_PNG = "icon.png";
	private static final String FAVICON_PNG = "favicon.png";
	private static final String LOGO_PNG = "logo.png";
	
	private static final String[] RESOURCES = new String[] { ICON_PNG, FAVICON_PNG, LOGO_PNG };
	
	private static final String MODPACKS_YML = "modpacks.yml";
	private static final File MODPACKS_YML_FILE = new File(PlatformUtils.getWorkingDirectory(), MODPACKS_YML);
	
	private static volatile boolean updated = false;
	private static Object key = new Object();

	public static String currentModPack = null;
	public static String currentModPackLabel = null;
	public static File currentModPackDirectory = null;
	
	public static Image favIcon = null;
	public static Image icon = null;
	public static Image logo = null;

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

	public static Map<String, String> getModPacks()
	{
		return (Map<String, String>) getModPacksYML().getProperty("modpacks");
	}
	
	public static boolean setModPack(String modPack, String modPackLabel) {
		if (modPack.equalsIgnoreCase(currentModPack))
			return true;
		
		Map<String, String> modPacks = getModPacks();
		if (!modPacks.containsKey(modPack)) {
			//Mod Pack not in list
			Util.log("ModPack '%s' not in '%s' file.", modPackLabel, MODPACKS_YML);
			return false;
		}
		
		SettingsUtil.setModPackSelection(modPack);
		
		currentModPack = modPack;
		currentModPackLabel = modPackLabel;
		currentModPackDirectory = new File(PlatformUtils.getWorkingDirectory(), currentModPack);
		
		downloadModPackResources();
		
		Main.loginForm.updateBranding();
		
		return true;
	}

	public static void downloadModPackResources() {
		Map<String, String> downloadFileList = new HashMap<String, String>();
		for (String resource : RESOURCES) {
			String relativeFilePath = currentModPack + File.separator + ICON_PNG;
			String fileURL = MirrorUtils.getMirrorUrl(relativeFilePath, null);
			if (fileURL == null) continue;
			String filePath = new File(ModPackYML.getModPackDirectory(), ICON_PNG).getAbsolutePath();
			downloadFileList.put(fileURL, filePath);
		}
		if (DownloadUtils.downloadFiles(downloadFileList , 30, TimeUnit.SECONDS) != downloadFileList.size()) {
			Util.log("[Error] Could not download all resources for modpack '%s'.", currentModPackLabel);
		}
	}
}
