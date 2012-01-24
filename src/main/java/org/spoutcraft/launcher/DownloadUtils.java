package org.spoutcraft.launcher;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.spoutcraft.launcher.async.Download;
import org.spoutcraft.launcher.async.DownloadListener;
import org.yaml.snakeyaml.Yaml;

public class DownloadUtils {
	public static final File cacheDirectory = new File(PlatformUtils.getWorkingDirectory(), "cache");
	
	public static Download downloadFile(String url, String output, String cacheName, String md5, DownloadListener listener) throws IOException {
		int tries = SettingsUtil.getLoginTries();
		File outputFile = new File(output);
		File tempfile = File.createTempFile("Modpack", null);
		Download download = null;
		while (tries > 0) {
			System.out.println("Starting download of " + url + ", with " + tries + " tries remaining");
			tries--;
			download = new Download(url, tempfile.getPath());
			download.setListener(listener);
			download.run();
			if (!download.isSuccess()) {
				if (download.getOutFile() != null) {
					download.getOutFile().delete();
				}
				System.err.println("Download of " + url + " Failed!");
				if (listener != null)
					listener.stateChanged("Download Failed, retries remaining: " + tries, 0F);
			}
			else {
				if (md5 != null) {
					String resultMD5 = MD5Utils.getMD5(download.getOutFile());
					System.out.println("Expected MD5: " + md5 + " Calculated MD5: " + resultMD5);
					if (resultMD5.equals(md5)) {
						GameUpdater.copy(tempfile, outputFile);
						tempfile.delete();
						outputFile = download.getOutFile();
						break;
					}
				}
				else {
					GameUpdater.copy(tempfile, outputFile);
					tempfile.delete();
					outputFile = download.getOutFile();
					break;
				}
			}
		}
		if (outputFile == null) {
			throw new IOException("Failed to download " + url);
		}
		if (cacheName != null) {
			GameUpdater.copy(outputFile, new File(cacheDirectory, cacheName));
		}
		return download;
	}
	
	public static Download downloadFile(String url, String output, String cacheName) throws IOException {
		return downloadFile(url, output, cacheName, null, null);
	}

	public static Download downloadFile(String url, String output) throws IOException {
		return downloadFile(url, output, null, null, null);
	}

	private static int filesToDownload = 0;
	private static int filesDownloaded = 0;
	public static int downloadFiles(Map<String, String> downloadFileList, long timeout, TimeUnit unit) {
		filesToDownload = downloadFileList.size();
		filesDownloaded = 0;
		
		ExecutorService es = Executors.newCachedThreadPool();
		for (final Map.Entry<String, String> file: downloadFileList.entrySet()) {
			es.execute(new Runnable() {
				public void run() {
					Download downloadFile = null;
					try {
						downloadFile = downloadFile(file.getKey(), file.getValue());
						if (downloadFile != null && downloadFile.isSuccess()) {
							filesDownloaded++;
							return;
						}						
					} catch (IOException e) {
						e.printStackTrace();
					}
					Util.log("[Error] file '%s' failed to download.", downloadFile.getOutFile());
				}
			});
		}
		es.shutdown();
		try {
			if (es.awaitTermination(timeout, unit)) {
				return filesDownloaded;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	
	public static boolean downloadFile(String relativePath) {
		if (MD5Utils.checksumPath(relativePath))
			return true;
		
		URL url = null;
		File tempFile = null;
		try {
			String mirrorUrl = MirrorUtils.getMirrorUrl(relativePath, null);
			url = new URL(mirrorUrl);
			URLConnection con = (url.openConnection());
			
			System.setProperty("http.agent", "");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
			
			tempFile = File.createTempFile("Modpack", null);
			
			//Download to temporary file
			OutputStream baos = new FileOutputStream(tempFile);
			//new FileOutputStream(tempFile)
			if (GameUpdater.copy(con.getInputStream(), baos) <= 0) {
				System.out.printf("[Error] Download URL was empty: '%s'/n", url);
				return false;
			}
			
			//If no Exception then file loaded fine, copy to output file
			GameUpdater.copy(tempFile, new File(relativePath));
			tempFile.delete();
			
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
}
