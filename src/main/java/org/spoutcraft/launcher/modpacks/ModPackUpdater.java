package org.spoutcraft.launcher.modpacks;

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
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.MirrorUtils;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.SpoutcraftBuild;
import org.spoutcraft.launcher.async.Download;

public class ModPackUpdater extends GameUpdater {
	static volatile boolean updated = false;

	public static final String defaultModPackName = "technicssp";
	
	private static String baseFallbackURL = "http://technic.freeworldsgaming.com/";
	private static String fallbackModsURL = baseFallbackURL + "mods/";

	static Object key = new Object();

	public void updateModPackMods() {		
		try {

			Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
			Map<String, Object> currentModList = SpoutcraftBuild.getSpoutcraftBuild().getMods();
			
			//Remove Mods no longer in previous version
			removeOldMods(currentModList.keySet());
			
			for (Map.Entry<String, Object> modEntry2 : currentModList.entrySet()) {
				String modName = modEntry2.getKey();

				if (!modLibrary.containsKey(modName))
					throw new IOException(String.format("Mod '%s' is missing from the mod library", modName));

				Map<String, Object> modProperties = (Map<String, Object>) modLibrary.get(modName);
				Map<String, Object> modVersions = (Map<String, Object>) modProperties.get("versions");

				String version = modEntry2.getValue().toString();

				if (!modVersions.containsKey(version))
					throw new IOException(String.format("Mod '%s' version '%s' is missing from the mod library", modName, version));

				String installType = modProperties.get("installtype").toString();
				String fullFilename = modName + "-" + version + "." + installType;
				Boolean isOptional = modProperties.containsKey("optional") ? (Boolean) modProperties.get("optional") : false;

				Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
				String modMD5 = (String)versionProperties.get("md5");
				modMD5 = modMD5.toLowerCase();
				
				String installedModVersion = InstalledModsYML.getInstalledModVersion(modName);

				//If installed mods md5 hash is the same as server's version then go to next mod.
				if (installedModVersion != null && installedModVersion.equals(version)) {
					String cacheModPath = String.format("cache%s%s", File.separator, fullFilename);
					String md5ModPath = String.format("mods%s%s%s%s", File.separator, modName, File.separator, fullFilename);
					if (MD5Utils.checksumPath(cacheModPath, md5ModPath)) {
						continue;
					}
				}

				File modFile = File.createTempFile("launcherMod", null);

				//If have the mod file then update
				if (downloadModPackage(modName, fullFilename, modMD5, modFile))
					updateMod(modFile, modName, version);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeOldMods(Set<String> modsToInstall) {
		Map<String, String> installedMods = InstalledModsYML.getInstalledMods();

		if (installedMods == null || modsToInstall == null || modsToInstall.size() <= 0)
			return;

		Set<String> modsToRemove = installedMods.keySet();
		modsToRemove.removeAll(modsToInstall);
		
		for (String modName : modsToRemove)
		{
			removePreviousModVersion(modName, installedMods.get(modName));
		}
	}

	public boolean downloadModPackage(String name, String filename, String fileMD5, File downloadedFile) {
		try {
			//Install from cache if md5 matches otherwise download and insert to cache
			File modCache = new File(DownloadUtils.cacheDirectory, filename);
			if (modCache.exists() && fileMD5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
				copy(modCache, downloadedFile);
				return true;
			} else {						
				String mirrorURL = "mods/" + name + "/" + filename;
				String fallbackURL = fallbackModsURL + name + "/" + filename;
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

	public void updateMod(File modFile, String modName, String modVersion) {
		//Check if previous version of mod is installed
		String installedVersion = InstalledModsYML.getInstalledModVersion(modName);

		if (installedVersion != null)
			removePreviousModVersion(modName, installedVersion);

		stateChanged("Extracting Files ...", 0);
		// Extract Natives
		extractNatives2(PlatformUtils.getWorkingDirectory(), modFile);	

		InstalledModsYML.setInstalledModVersion(modName, modVersion);

		modFile.delete();
	}

	private void removePreviousModVersion(String modName, String installedVersion) {
		try {
			//Mod has been previously installed uninstall previous version
			File previousModZip = new File(DownloadUtils.cacheDirectory, modName + "-" + installedVersion + ".zip");
			ZipFile zf = new ZipFile(previousModZip);
			Enumeration<? extends ZipEntry> entries = zf.entries();
			//Go through zipfile of previous version and delete all file from Madpack that exist in the zip
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

	public boolean isModpackUpdateAvailable() throws IOException {

		Map<String, Object> modLibrary = (Map<String, Object>) ModLibraryYML.getModLibraryYML().getProperty("mods");
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

			String installType = modProperties.get("installtype").toString();
			String fullFilename = modName + "-" + version + "." + installType;

			Map<String, Object> versionProperties = (Map<String, Object>) modVersions.get(version);
			String md5 = (String)versionProperties.get("md5");

			File modCache = new File(DownloadUtils.cacheDirectory, fullFilename);
			if (!modCache.exists() || !md5.equalsIgnoreCase(MD5Utils.getMD5(modCache))) {
				return true;
			}
		}

		return false;
	}

}
