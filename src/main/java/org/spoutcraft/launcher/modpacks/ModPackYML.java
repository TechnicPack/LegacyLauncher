package org.spoutcraft.launcher.modpacks;

import java.awt.Image;
import java.io.File;
import java.util.List;
import java.util.Map;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.Main;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.YmlUtils;

public class ModPackYML {
	private static final String MODPACK_YML = "modpack.yml";
	private static final String FALLBACK_URL = String.format("http://technic.freeworldsgaming.com/%s", MODPACK_YML);
	private static volatile boolean updated = false;
	private static File modPackYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + MODPACK_YML);
	private static Object key = new Object();

	private static File getModPackYMLFile() {
		return modPackYML;
	}

	public static File getModPackDirectory() {
		return new File(Main.basePath, ModPackListYML.currentModPack);
	}

	private static void setModPackYML(String modPack) {
		ModPackYML.modPackYML = new File(PlatformUtils.getWorkingDirectory(), modPack + File.separator + MODPACK_YML);
	}

	public static Configuration getModPackYML() {
		updateModPackYML();
		Configuration config = new Configuration(getModPackYMLFile());
		config.load();
		return config;
	}
	
	public static void updateModPackYML() {
		if (!updated) {
			synchronized(key) {
				String selected = getSelectedBuild();
				
				YmlUtils.downloadYmlFile(MODPACK_YML, FALLBACK_URL, getModPackYMLFile());
				
				Configuration config = new Configuration(getModPackYMLFile());
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
		if (getModPackYMLFile().exists()) {
			try {
				Configuration config = new Configuration(getModPackYMLFile());
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

	public static List<Map<String, String>> getModList() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getModPackIcon() {
		return new File(getModPackDirectory(), "resources" + File.separator + "icon.png").getAbsolutePath();
	}

	public static String getModPackLogo() {
		return new File(getModPackDirectory(), "resources" + File.separator + "logo.png").getAbsolutePath();
	}

	public static String getModPackFavIcon() {
		return new File(getModPackDirectory(), "resources" + File.separator + "logo.png").getAbsolutePath();
	}
}
