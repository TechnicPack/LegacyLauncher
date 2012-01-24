package org.spoutcraft.launcher.modpacks;

import java.io.File;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.PlatformUtils;
import org.spoutcraft.launcher.YmlUtils;

public class ModLibraryYML {
	public static final String MODLIBRARY_YML = "modlibrary.yml";
	public static final File modLibraryYML = new File(PlatformUtils.getWorkingDirectory(), MODLIBRARY_YML);

	public static void updateModLibraryYML() {
		if (ModPackUpdater.updated) return;
		synchronized(ModPackUpdater.key) {				
			YmlUtils.downloadRelativeYmlFile(ModLibraryYML.MODLIBRARY_YML);
			ModPackUpdater.updated = true;		
		}
	}

	public static Configuration getModLibraryYML() {
		updateModLibraryYML();
		Configuration config = new Configuration(modLibraryYML);
		config.load();
		return config;
	}

}
