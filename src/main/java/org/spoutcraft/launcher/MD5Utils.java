package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.util.config.Configuration;

public class MD5Utils {
	public static String getMD5(File file){
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			String md5Hex = DigestUtils.md5Hex(stream);
			stream.close();
			return md5Hex;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static boolean doMD5sMatch(File file, String md5) {
		return getMD5(file).equalsIgnoreCase(md5);
	}
	
	public static String getMD5(FileType type) {
		return getMD5(type, MinecraftYML.getLatestMinecraftVersion());
	}

	@SuppressWarnings("unchecked")
	public static String getMD5(FileType type, String version) {
		Configuration config = MinecraftYML.getMinecraftYML();
		Map<String, Map<String, String>> builds = (Map<String, Map<String, String>>) config.getProperty("versions");
		if (builds.containsKey(version)) {
			Map<String, String> files = builds.get(version);
			return files.get(type.name());
		}
		return null;
	}
}
