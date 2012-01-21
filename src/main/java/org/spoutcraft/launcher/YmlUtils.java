package org.spoutcraft.launcher;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.yaml.snakeyaml.Yaml;

public class YmlUtils {
	private static File tempDir = new File(PlatformUtils.getWorkingDirectory(), "temp");
	
	public static boolean downloadMirrorsYmlFile(String mirrorYmlUrl) {
		return downloadYmlFile(mirrorYmlUrl, null, MirrorUtils.mirrorsYML);
	}
	
	public static boolean downloadRelativeYmlFile(String relativePath) {
		return downloadYmlFile(relativePath, null, new File(relativePath));
	}
	
	public static boolean downloadYmlFile(String ymlUrl, String fallbackUrl, File ymlFile) {
		boolean isRelative = !ymlUrl.contains("http");
		
		if (isRelative && ymlFile.exists() && MD5Utils.checksumPath(ymlUrl))
			return true;
		
		URL url = null;
		File tempFile = null;
		try {
			if (isRelative && !MirrorUtils.isAddressReachable(ymlUrl)) {
				return false;
			} else {
				ymlUrl = MirrorUtils.getMirrorUrl(ymlUrl, fallbackUrl);
			}
			
			url = new URL(ymlUrl);
			URLConnection con = (url.openConnection());
			
			System.setProperty("http.agent", "");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
			
			tempFile = File.createTempFile("launcherYml", null);
			
			//Download to temporary file
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//new FileOutputStream(tempFile)
			if (GameUpdater.copy(con.getInputStream(), baos) <= 0) {
				System.out.printf("[Error] Download URL was empty: '%s'/n", url);
				return false;
			}

			byte[] yamlData = baos.toByteArray();
			
			//Test yml loading
			Yaml yamlFile = new Yaml();
			yamlFile.load(new BufferedInputStream(new ByteArrayInputStream(yamlData)));
			
			//If no Exception then file loaded fine, copy to output file
			GameUpdater.copy(tempFile, ymlFile);
			
			return true;
		} catch (MalformedURLException e) {
			System.out.printf("[Error] Download URL badly formed: '%s'/n", url);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.printf("[Error] Could not write to temp file: '%s'/n", tempFile);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean md5HashMatches(File file) {
		return MirrorUtils.getYmlMD5(file).equalsIgnoreCase(MD5Utils.getMD5(file));
	}
}
