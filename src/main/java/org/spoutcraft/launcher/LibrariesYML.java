package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.bukkit.util.config.Configuration;

public class LibrariesYML {
	private static final String LIBRARIES_YML = "libraries.yml";
	private static volatile boolean updated = false;
	private static File librariesYMLFile = new File(PlatformUtils.getWorkingDirectory(), LIBRARIES_YML);
	private static Object key = new Object();

	public static Configuration getLibrariesYML() {
		updateLibrariesYMLCache();
		Configuration config = new Configuration(librariesYMLFile);
		config.load();
		return config;
	}
	
	public static void updateLibrariesYMLCache() {
		if (!updated) {
			synchronized(key) {
				YmlUtils.downloadYmlFile(LIBRARIES_YML, "http://technic.freeworldsgaming.com/libraries.yml", librariesYMLFile);
				updated = true;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String getMD5(String library, String version) {
		Configuration config = getLibrariesYML();
		Map<String, Object> libraries = (Map<String, Object>) config.getProperty(library);
		Map<String, String> versions = (Map<String, String>) libraries.get("versions");
		String result = versions.get(version);
		if (result == null) {
			try {
				result = versions.get(Double.parseDouble(version));
			}
			catch (NumberFormatException ignore) {}
		}
		return result;
	}

}
