package org.spoutcraft.launcher.technic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.LibrariesYML;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SpoutcraftBuild;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.exception.NoMirrorsAvailableException;

public class TechnicUpdater extends GameUpdater {
	private static volatile boolean updated = false;

	private static File baseTechnicDirectory = new File(PlatformUtils.getWorkingDirectory(), "technic");
	private static File technicModsDirectory = new File(baseTechnicDirectory, "mods");
	private static File technicModsYML = new File(baseTechnicDirectory, "modlist.yml");	

	private static String baseTechnicURL = "http://dl.dropbox.com/u/182999/technic/";
	private static String technicModsURL = baseTechnicURL + "mods/";
	
	private static Object key = new Object();
	
	public static void updateTechnicModsYML() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("modlist.yml", baseTechnicURL + "modlist.yml", null);
				if (urlName != null) {
	
					try {
						//int selected = -1;
						if (technicModsYML.exists()) {
							try {
								Configuration config = new Configuration(technicModsYML);
								config.load();
								//selected = config.getInt("current", -1);
							}
							catch (Exception ex){
								ex.printStackTrace();
							}
						}
	
						URL url = new URL(urlName);
						HttpURLConnection con = (HttpURLConnection)(url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(technicModsYML));
	
						Configuration config = new Configuration(technicModsYML);
						config.load();
						//config.setProperty("current", selected);
						//config.setProperty("launcher", Main.build);
						//config.save();
						
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				updated = true;
			}
		}
	}

	public static Configuration getTechnicModsYML() {
		updateTechnicModsYML();
		Configuration config = new Configuration(technicModsYML);
		config.load();
		return config;
	}

	public void updateTechnicMods() throws Exception {
		
		Download download;
		
		technicModsDirectory.mkdirs();
		
		Map<String, Object> modLibrary = (Map<String, Object>) getTechnicModsYML().getProperty("mods");

		Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
		for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
			String modName = modEntry2.getKey();
			
			if (!modLibrary.containsKey(modName))
				throw new IOException("Mod is missing from the mod library");

			Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
			Map<String, String> modVersions = (Map<String, String>) modProperties.get("versions");
			
			String version = modEntry2.getValue().toString();
						
			if (!modVersions.containsKey(version))
				throw new IOException("Mod version is missing from the mod library");
					

			File modDirectory = new File(technicModsDirectory, modName);
			modDirectory.mkdirs();

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;
			String md5 = modVersions.get(version);
			
			//If local mods md5 hash is not the same as server version then delete to update.
			File modFile = new File(modDirectory, fullFilename);
			if (modFile.exists()) {
				String computedMD5  = MD5Utils.getMD5(modFile);
				if (!computedMD5.equals(md5)) {
					modFile.delete();
				}
			}
			
			if (!modFile.exists()) {
				String mirrorURL = "/Libraries/" + fullFilename;
				String fallbackURL = technicModsURL + modName + "/" + fullFilename;
				String url = MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, this);
				download = DownloadUtils.downloadFile(url, modFile.getPath(), fullFilename, md5, this);
			}
		}	
	}
	
	public boolean isTechnicUpdateAvailable() throws IOException {
		//If no technic mods directory then update
		if (!technicModsDirectory.exists())
			return true;
		
		Map<String, Object> modLibrary = (Map<String, Object>) getTechnicModsYML().getProperty("mods");

		Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
		for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
			String modName = modEntry2.getKey();
			
			if (!modLibrary.containsKey(modName))
				throw new IOException("Mod is missing from the mod library");
			
			Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
			Map<String, String> modVersions = (Map<String, String>) modProperties.get("versions");
			
			String version = modEntry2.getValue().toString();
						
			if (!modVersions.containsKey(version))
				throw new IOException("Mod version is missing from the mod library");
					

			File modDirectory = new File(technicModsDirectory, modName);

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;
			String md5 = modVersions.get(version);
			
			File modFile = new File(modDirectory, fullFilename);
			if (!modFile.exists()) {
				return true;
			}
			
			String computedMD5  = MD5Utils.getMD5(modFile);
			if (!computedMD5.equals(md5)) {
				return true;
			}
		}
		
		return false;
	}

}
