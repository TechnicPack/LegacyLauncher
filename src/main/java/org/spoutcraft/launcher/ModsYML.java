package org.spoutcraft.launcher;

import java.io.File;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.util.config.Configuration;
import org.spoutcraft.diff.JBPatch;
import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;

public class ModsYML {
	private static volatile boolean updated = false;
	private static File modsYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "mods.yml");
	private static Object key = new Object();

	public static Configuration getModsYML() {
		updateModsYMLCache();
		Configuration config = new Configuration(modsYML);
		config.load();
		return config;
	}
	
	public static void updateModsYMLCache() {
		if (!updated) {
			synchronized(key) {
				String urlName = MirrorUtils.getMirrorUrl("mods.yml", "http://urcraft.com/technic/mods.yml", null);
				if (urlName != null) {
	
					try {
						URL url = new URL(urlName);
						HttpURLConnection con = (HttpURLConnection)(url.openConnection());
						System.setProperty("http.agent", "");
						con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
						GameUpdater.copy(con.getInputStream(), new FileOutputStream(modsYML));
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
	public static String[][] getTechnicMods()
	{
		Configuration config = getModsYML();
		Map<Integer, Object> mods = (Map<Integer, Object>) config.getProperty("mods");
		
		if(mods != null)
		{
			String[][] results = new String[mods.size()][];
			int index = 0;
			for (Integer i : mods.keySet())
			{
				//i is the number of the mod, while index is i-1
//				results[index] = i.toString();
				Map<String, Object> map = (Map<String, Object>) mods.get(i);
				String name = String.valueOf(map.get("name"));
				String description = String.valueOf(map.get("description"));
				String installtype = String.valueOf(map.get("installtype"));
				String filenames = String.valueOf(map.get("filenames"));
				results[index][0] = i.toString();
				results[index][1] = name;
				results[index][2] = description;
				results[index][3] = installtype;
				results[index][4] = filenames;
				index++;
			}
			return results;
		}
		return null;
		
	}
}
