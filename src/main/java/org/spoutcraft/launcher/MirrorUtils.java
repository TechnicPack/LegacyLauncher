package org.spoutcraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.util.config.Configuration;
import org.spoutcraft.launcher.async.DownloadListener;

public class MirrorUtils {
	public static final String[] MIRRORS_URL = {
		"https://raw.github.com/TechnicPack/Technic/master/mirrors.yml", 
		"https://raw.github.com/icew01f/Technic/master/mirrors.yml", 
		"http://technic.freeworldsgaming.com/mirrors.yml"
		};
	public static File mirrorsYML = new File(PlatformUtils.getWorkingDirectory(), "technic" + File.separator + "mirrors.yml");
	
	private static boolean updated = false;
	private static final Random rand = new Random();
	private static final Map<String, String> ymlMD5s = new HashMap<String, String>();

	public static String getYmlMD5(File file) {
		return getYmlMD5(file.getName());
	}
	
	public static String getYmlMD5(String fileName) {
		if (ymlMD5s.containsKey(fileName))
			return ymlMD5s.get(fileName);
		else
			return "";
	}
	
	public static String getMirrorUrl(String mirrorURI, String fallbackUrl, DownloadListener listener) {
		try {
			Map<String, Integer> mirrors = getMirrors();
			Set<Entry<String, Integer>> set = mirrors.entrySet();
			
			ArrayList<String> goodMirrors = new ArrayList<String>(mirrors.size());
			Iterator<Entry<String, Integer>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> e = iterator.next();
				String url = e.getKey();
				String mirror = (!url.contains("github.com")) ? "http://" + e.getKey() + "/" + mirrorURI : "https://" + e.getKey() + "/" + mirrorURI;
				if (isAddressReachable(mirror)) {
					return mirror;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("All mirrors failed, reverting to default");
		return fallbackUrl;
	}
	
	public static String getMirrorUrl(String mirrorURI, String fallbackUrl) {
		return getMirrorUrl(mirrorURI, fallbackUrl, null);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getMirrors() {
		Configuration config = getMirrorsYML();
		return (Map<String, Integer>) config.getProperty("mirrors");
	}
	
	public static boolean isAddressReachable(String url) {
		try {
			if (url.contains("https")) {
				HttpsURLConnection urlConnect = (HttpsURLConnection)(new URL(url).openConnection());
				urlConnect.setInstanceFollowRedirects(false);
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				return (responseCode == HttpURLConnection.HTTP_OK);
			} else {			
				HttpURLConnection urlConnect = (HttpURLConnection)(new URL(url).openConnection());
				urlConnect.setInstanceFollowRedirects(false);
				urlConnect.setRequestMethod("HEAD");
				int responseCode = urlConnect.getResponseCode();
				return (responseCode == HttpURLConnection.HTTP_OK);
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	public static Configuration getMirrorsYML() {
		updateMirrorsYMLCache();
		Configuration config = new Configuration(mirrorsYML);
		config.load();
		if (ymlMD5s.size() <= 0)
			updateYmlMD5Map(config);
		return config;
	}
	
	private static void updateYmlMD5Map(Configuration config) {
		try {
			ymlMD5s.putAll((Map<String, String>) config.getProperty("yaml"));
		} catch (NullPointerException e) {
			System.out.print("[Error] MD5's missing from mirrors.yml!");
			e.printStackTrace();
		}
	}

	public static void updateMirrorsYMLCache() {
		if (!updated) {
			updated = true;
			for (String urlentry : MIRRORS_URL) {
				if (YmlUtils.downloadMirrorsYmlFile(urlentry)) {
					ymlMD5s.clear();
					return;
				}
			}
		}
	}
}
