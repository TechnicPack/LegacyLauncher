package org.spoutcraft.launcher.technic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.DownloadUtils;
import org.spoutcraft.launcher.FileUtils;
import org.spoutcraft.launcher.GameUpdater;
import org.spoutcraft.launcher.MD5Utils;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SpoutcraftBuild;
import org.spoutcraft.launcher.YmlUtils;
import org.spoutcraft.launcher.async.Download;

public class ModPackUpdater extends GameUpdater {
	private static volatile boolean updated = false;

	private static File baseModPackDirectory = new File(PlatformUtils.getWorkingDirectory(), "technic");
	private static File technicModsDirectory = new File(baseModPackDirectory, "mods");
	private static File technicModsYML = new File(baseModPackDirectory, "modlist.yml");	

	private static String baseTechnicURL = "http://technic.freeworldsgaming.com/";
	private static String technicModsURL = baseTechnicURL + "mods/";

	private static Object key = new Object();

	private static File installedTechnicModsYML = new File(baseModPackDirectory, "installedMods.yml");	

	public static void updateTechnicModsYML() {
		if (updated) return;
		synchronized(key) {				
			YmlUtils.downloadYmlFile("modlist.yml", baseTechnicURL + "modlist.yml", technicModsYML);
			updated = true;		
		}
	}
	
	public void switchToCurrentModPack() {
		//String modPackName = 
	}

	public static Configuration getTechnicModsYML() {
		updateTechnicModsYML();
		Configuration config = new Configuration(technicModsYML);
		config.load();
		return config;
	}

	public void updateTechnicMods() {		
		try {
			Configuration modsConfig = new Configuration(installedTechnicModsYML);
			modsConfig.load();
			

			technicModsDirectory.mkdirs();

			Map<String, Object> modLibrary = (Map<String, Object>) getTechnicModsYML().getProperty("mods");
			Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
			
			//Remove Mods no longer in previous version
			removeOldMods(modsConfig, currentModList.keySet());
			
			for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
				String modName = modEntry2.getKey();

				if (!modLibrary.containsKey(modName))
					throw new IOException(String.format("Mod '%s' is missing from the mod library", modName));

				Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
				Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

				String version = modEntry2.getValue().toString();

				if (!modVersions.containsKey(version))
					throw new IOException(String.format("Mod '%s' version '%s' is missing from the mod library", modName, version));


				File modDirectory = new File(technicModsDirectory, modName);
				modDirectory.mkdirs();

				String installType = modProperties.get("installtype").toString();
				String fullFilename = modName + "-" + version + "." + installType;
				Boolean isOptional = modProperties.containsKey("optional") ? (Boolean) modProperties.get("optional") : false;

				Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
				String modMD5 = (String)versionProperties.get("md5");
				modMD5 = modMD5.toLowerCase();

				//If local mods md5 hash is not the same as server version then delete to update.
				File modFile = new File(modDirectory, fullFilename);

				//If Mod file does not exist or md5's dont't match or the mod file does not delete abort
				if (modFile.exists() && !MD5Utils.doMD5sMatch(modFile, modMD5) && !modFile.delete())
					continue;

				//If have the mod file then update
				if (downloadModPackage(modName, fullFilename, modMD5, modFile))
					updateMod(modFile, modName, version, modsConfig);
			}

			modsConfig.save();
			
			FileUtils.deleteQuietly(technicModsDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeOldMods(Configuration modsConfig, Set<String> modsToInstall) {
		if (modsConfig.getProperty("mods") == null)
			return;
		Map<String, String> installedMods = (Map<String, String>)modsConfig.getProperty("mods");
		Set<String> modsToRemove = installedMods.keySet();

		if (modsToInstall == null || modsToInstall.size() <= 0)
			return;
		
		modsToRemove.removeAll(modsToInstall);
		
		for (String modName : modsToRemove)
		{
			removePreviousModVersion(modName, installedMods.get(modName));
		}
	}
	
	

	public boolean downloadModPackage(String name, String filename, String fileMD5, File downloadedFile) {
		try {
			//Install from cache if md5 matches otherwise download and insert to cache
			File modCache = new File(binCacheDir, filename);
			if (modCache.exists() && fileMD5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
				copy(modCache, downloadedFile);
				return true;
			} else {						
				String mirrorURL = "mods/" + name + "/" + filename;
				String fallbackURL = technicModsURL + name + "/" + filename;
				String url = MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL, this);
				Download download = DownloadUtils.downloadFile(url, downloadedFile.getPath(), filename, fileMD5, this);
				return download.isSuccess();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createJar(File jarFilename, File... filesToAdd) {
		try {
			FileOutputStream stream = new FileOutputStream(jarFilename);
			JarOutputStream out = new JarOutputStream(stream, new Manifest());
			BufferedOutputStream bos = new BufferedOutputStream(out);
			for (File fileToAdd : filesToAdd) {
				if (fileToAdd == null || !fileToAdd.exists() || fileToAdd.isDirectory())
					continue; // Just in case...
				JarEntry jarAdd = new JarEntry(fileToAdd.getName());
				jarAdd.setTime(fileToAdd.lastModified());
				out.putNextEntry(jarAdd);
				FileInputStream in = new FileInputStream(fileToAdd);
				BufferedInputStream bis = new BufferedInputStream(in);
				int data;
				while ((data = bis.read()) != -1)
					bos.write(data);
				bis.close();
				in.close();
			}
			bos.close();
			out.close();
			stream.close();
		} catch (FileNotFoundException e) {
			// skip not found file
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void updateMod(File modFile, String modName, String modVersion, Configuration modsConfig) {
		//Check if previous version of mod is installed
		String modPath = String.format("mods.%s", modName);
		String installedVersion = (String)modsConfig.getProperty(modPath);

		if (installedVersion != null)
			removePreviousModVersion(modName, installedVersion);

		stateChanged("Extracting Files ...", 0);
		// Extract Natives
		extractNatives2(PlatformUtils.getWorkingDirectory(), modFile);	

		modsConfig.setProperty(modPath, modVersion);

		modFile.delete();
	}

	private void removePreviousModVersion(String modName, String installedVersion) {
		try {
			//Mod has been previously installed uninstall previous version
			File previousModZip = new File(new File(technicModsDirectory, modName), modName + "-" + installedVersion + ".zip");
			//String previousModVersionFileName = String.format("%s\\%s\\%s-%v.zip", technicModsDirectory.getAbsolutePath(), modName, modName, modVersion);
			ZipFile zf = new ZipFile(previousModZip);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			//Go through zipfile of previous version and delete all file from technic that exist in the zip
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) continue;
				File file = new File(PlatformUtils.getWorkingDirectory(), entry.getName());					
				if (file.exists()) {
					//File from mod exists.. delete it
					file.delete();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
			Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

			String version = modEntry2.getValue().toString();

			if (!modVersions.containsKey(version))
				throw new IOException("Mod version is missing from the mod library");


			File modDirectory = new File(technicModsDirectory, modName);

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;

			Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
			String md5 = (String)versionProperties.get("md5");

			File modCache = new File(binCacheDir, fullFilename);
			if (!modCache.exists() || !md5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
				return true;
			}
		}

		return false;
	}

}
