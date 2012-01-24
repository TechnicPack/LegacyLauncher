package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.util.config.Configuration;

public class MD5Utils {
	private static final String CHECKSUM_MD5 = "CHECKSUM.md5";
	private static final File CHECKSUM_FILE = new File(GameUpdater.workDir, CHECKSUM_MD5);
	private static boolean updated;
	
	private static final Map<String, String> md5Map = new HashMap<String, String>();

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

	public static void updateMD5Cache() {
		if (!updated) {
			updated = true;
			try {
				String url = MirrorUtils.getMirrorUrl(CHECKSUM_MD5, null);
				if (DownloadUtils.downloadFile(url, CHECKSUM_FILE.getPath()).isSuccess()) {
					parseChecksumFile();
				}
			} catch (FileNotFoundException e) {
				Util.log("[Error] Checksum file '%s' not found.", CHECKSUM_FILE.getAbsoluteFile());
				e.printStackTrace();
			} catch (IOException e) {
				Util.log("[Error] Checksum file '%s' threw error.", CHECKSUM_FILE.getAbsoluteFile());
				e.printStackTrace();
			}
		}
	}

	private static void parseChecksumFile() throws FileNotFoundException {
		md5Map.clear();
		Scanner scanner = new Scanner(CHECKSUM_FILE).useDelimiter("\\||\n");
		while (scanner.hasNext()) {
//			String[] tokens = scanner.nextLine().split("\\|");
			String md5 = scanner.next().toLowerCase();// tokens[0];
			String path = scanner.next();//tokens[1];			
			md5Map.put(path, md5);
			scanner.nextLine();
		}
	}

	public static boolean checksumPath(String relativePath) {
		return checksumPath(relativePath, relativePath);
	}

	public static boolean checksumPath(String filePath, String md5Path) {
		File file = new File(filePath);
		if (!file.exists())
			return false;
		if (!md5Map.containsKey(md5Path))
			return false;
		return md5Map.get(md5Path).equalsIgnoreCase(getMD5(file));
	}
}
