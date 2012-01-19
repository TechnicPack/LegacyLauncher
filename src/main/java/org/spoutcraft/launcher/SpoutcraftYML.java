package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.util.config.Configuration;

public class SpoutcraftYML {
	private static volatile boolean updated = false;
	private static File spoutcraftYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "technic.yml");
	private static Object key = new Object();

	public static Configuration getSpoutcraftYML() {
		updateSpoutcraftYMLCache();
		Configuration config = new Configuration(spoutcraftYML);
		config.load();
		return config;
	}
	
	public static void updateSpoutcraftYMLCache() {
		if (!updated) {
			synchronized(key) {
				String selected = getSelectedBuild();
				
				YmlUtils.downloadYmlFile("technic.yml", "http://technic.freeworldsgaming.com/technic.yml", spoutcraftYML);
				
				Configuration config = new Configuration(spoutcraftYML);
				config.load();
				config.setProperty("current", selected);
				config.setProperty("launcher", Main.build);
				config.save();
				
				updated = true;
			}
		}
	}

	private static String getSelectedBuild() {
		String selected = null;
		if (spoutcraftYML.exists()) {
			try {
				Configuration config = new Configuration(spoutcraftYML);
				config.load();
				selected = config.getString("current");
				if (selected == null || !isValidBuild(selected))
					selected = config.getString("recommended");
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
		return selected;
	}

	private static boolean isValidBuild(String selected) {
		return !selected.equals("-1");
	}
}
