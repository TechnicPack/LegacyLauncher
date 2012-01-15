package org.spoutcraft.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.util.config.Configuration;
import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class ModPacksYML {
	private static volatile boolean updated = false;
	private static File modpackYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "modpacks.yml");
	private static Object key = new Object();

	public static Configuration getModPacksYML() {
		updateModPacksYMLCache();
		Configuration config = new Configuration(modpackYML);
		config.load();
		return config;
	}
	
	public static void updateModPacksYMLCache() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("modpacks.yml", "http://technic.freeworldsgaming.com/modpacks.yml", null);
				if (urlName != null) {
	
					try {
						URL url = new URL(urlName);
						HttpURLConnection con = (HttpURLConnection)(url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(modpackYML));
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				updated = true;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getModPacks()
	{
		Configuration config = getModPacksYML();
		Map<Integer, Object> modpacks = (Map<Integer, Object>) config.getProperty("modpacks");
		
		List<Map<String, String>> modpackList = new ArrayList<Map<String, String>>();
		
		Map<String, String> modpackDetails = null;
		for (Integer i : modpacks.keySet())
		{
			Map<String, String> map = (Map<String, String>) modpacks.get(i);
			
			modpackDetails = new HashMap<String, String>();
			modpackDetails.put("name", String.valueOf(map.get("name")));
			modpackDetails.put("filenames", String.valueOf(map.get("filename")));
			
			modpackList.add(modpackDetails);
		}
		
		return modpackList;		
	}
}
