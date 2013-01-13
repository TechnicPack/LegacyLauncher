/*
 * This file is part of Spoutcraft Launcher (http://wiki.getspout.org/).
 * 
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.spoutcraft.launcher.async.DownloadListener;
import org.spoutcraft.launcher.exception.UnsupportedOSException;

import SevenZip.LzmaAlone;

public class GameUpdater implements DownloadListener {
  public static final String LAUNCHER_DIRECTORY = "launcher";
  public static final File   WORKING_DIRECTORY  = PlatformUtils.getWorkingDirectory();

  /* Minecraft Updating Arguments */
  public String              user               = "Player";
  public String              downloadTicket     = "1";

  /* Files */
  public static File         modpackDir         = new File(WORKING_DIRECTORY, "");
  public static File         binDir             = new File(WORKING_DIRECTORY, "bin");
  public static final File   cacheDir           = new File(WORKING_DIRECTORY, "cache");
  public static final File   tempDir            = new File(WORKING_DIRECTORY, "temp");
  public static File         backupDir          = new File(WORKING_DIRECTORY, "backups");
  public static final File   workDir            = new File(WORKING_DIRECTORY, LAUNCHER_DIRECTORY);
  public static File         savesDir           = new File(WORKING_DIRECTORY, "saves");
  public static File         modsDir            = new File(WORKING_DIRECTORY, "mods");
  public static File         libsDir            = new File(WORKING_DIRECTORY, "lib");
  public static File         coremodsDir        = new File(WORKING_DIRECTORY, "coremods");
  public static File         modconfigsDir      = new File(WORKING_DIRECTORY, "config");
  public static File         resourceDir        = new File(WORKING_DIRECTORY, "resources");

  /* Minecraft Updating Arguments */
  public final String        baseURL            = "http://s3.amazonaws.com/MinecraftDownload/";
  public final String        latestLWJGLURL     = "http://mirror.technicpack.net/Technic/Libraries/lwjgl/";
  public final String        spoutcraftMirrors  = "http://cdn.getspout.org/mirrors.html";

  private DownloadListener   listener;

  public GameUpdater() {
  }

  public static void setModpackDirectory(String currentModPack) {
    modpackDir = new File(WORKING_DIRECTORY, currentModPack);
    modpackDir.mkdirs();

    binDir = new File(modpackDir, "bin");
    backupDir = new File(modpackDir, "backups");
    savesDir = new File(modpackDir, "saves");
    modsDir = new File(modpackDir, "mods");
    libsDir = new File(modpackDir, "lib");
    coremodsDir = new File(modpackDir, "coremods");
    modconfigsDir = new File(modpackDir, "config");
    resourceDir = new File(modpackDir, "resources");

    binDir.mkdirs();
    backupDir.mkdirs();
    savesDir.mkdirs();
    modsDir.mkdirs();
    libsDir.mkdirs();
    coremodsDir.mkdirs();
    modconfigsDir.mkdirs();
    resourceDir.mkdirs();

    System.setProperty("minecraft.applet.TargetDirectory", modpackDir.getAbsolutePath());
  }

  public void updateMC() throws Exception {

    binDir.mkdir();
    cacheDir.mkdirs();
    // if (tempDir.exists()) FileUtils.deleteDirectory(tempDir);
    tempDir.mkdirs();

    ModpackBuild build = ModpackBuild.getSpoutcraftBuild();
    String minecraftVersion = build.getMinecraftVersion();

    String minecraftMD5 = MD5Utils.getMD5(FileType.minecraft, minecraftVersion);
    String jinputMD5 = MD5Utils.getMD5(FileType.jinput);
    String lwjglMD5 = MD5Utils.getMD5(FileType.lwjgl);
    String lwjgl_utilMD5 = MD5Utils.getMD5(FileType.lwjgl_util);

    // Processs minecraft.jar \\
    File mcCache = new File(cacheDir, "minecraft_" + minecraftVersion + ".jar");
    if (!mcCache.exists() || !minecraftMD5.equals(MD5Utils.getMD5(mcCache))) {
      String minecraftURL = baseURL + "minecraft.jar?user=" + user + "&ticket=" + downloadTicket;
      String output = tempDir + File.separator + "minecraft.jar";
      MinecraftDownloadUtils.downloadMinecraft(minecraftURL, output, build, listener);
    }
    stateChanged("Copying minecraft.jar from cache", 0);
    copy(mcCache, new File(binDir, "minecraft.jar"));
    stateChanged("Copied minecraft.jar from cache", 100);

    File nativesDir = new File(binDir.getPath(), "natives");
    nativesDir.mkdir();

    // Process other Downloads
    mcCache = new File(cacheDir, "jinput.jar");
    String md5 = (SettingsUtil.isLatestLWJGL()) ? MD5Utils.getMD5FromList("Libraries\\lwjgl\\jinput.jar") : jinputMD5;
    if (!mcCache.exists() || !jinputMD5.equals(MD5Utils.getMD5(mcCache))) {
      DownloadUtils.downloadFile(getNativesUrl() + "jinput.jar", binDir.getPath() + File.separator + "jinput.jar", "jinput.jar", md5, listener);
    } else {
      stateChanged("Copying jinput.jar from cache", 0);
      copy(mcCache, new File(binDir, "jinput.jar"));
      stateChanged("Copied jinput.jar from cache", 100);
    }

    mcCache = new File(cacheDir, "lwjgl.jar");
    md5 = (SettingsUtil.isLatestLWJGL()) ? MD5Utils.getMD5FromList("Libraries\\lwjgl\\lwjgl.jar") : lwjglMD5;
    if (!mcCache.exists() || !lwjglMD5.equals(MD5Utils.getMD5(mcCache))) {
      DownloadUtils.downloadFile(getNativesUrl() + "lwjgl.jar", binDir.getPath() + File.separator + "lwjgl.jar", "lwjgl.jar", md5, listener);
    } else {
      stateChanged("Copying lwjgl.jar from cache", 0);
      copy(mcCache, new File(binDir, "lwjgl.jar"));
      stateChanged("Copied lwjgl.jar from cache", 100);
    }

    mcCache = new File(cacheDir, "lwjgl_util.jar");
    md5 = (SettingsUtil.isLatestLWJGL()) ? MD5Utils.getMD5FromList("Libraries\\lwjgl\\lwjgl_util.jar") : lwjgl_utilMD5;
    if (!mcCache.exists() || !lwjgl_utilMD5.equals(MD5Utils.getMD5(mcCache))) {
      DownloadUtils.downloadFile(getNativesUrl() + "lwjgl_util.jar", binDir.getPath() + File.separator + "lwjgl_util.jar", "lwjgl_util.jar", md5, listener);
    } else {
      stateChanged("Copying lwjgl_util.jar from cache", 0);
      copy(mcCache, new File(binDir, "lwjgl_util.jar"));
      stateChanged("Copied lwjgl_util.jar from cache", 100);
    }

    getNatives();

    File nativesZip = new File(GameUpdater.tempDir.getPath() + File.separator + "natives.zip");
    File nativesDirectory = new File(GameUpdater.binDir, "natives");
    extractCompressedFile(nativesDirectory, nativesZip, true);

    MinecraftYML.setInstalledVersion(minecraftVersion);
  }

  public String getNativesUrl() {
    if (SettingsUtil.isLatestLWJGL()) {
      return latestLWJGLURL;
    }
    return baseURL;
  }

  public String getNativesUrl(String fileName) {
    if (SettingsUtil.isLatestLWJGL()) {
      return latestLWJGLURL + fileName + ".zip";
    }
    return baseURL + fileName + ".jar.lzma";
  }

  public boolean checkMCUpdate() {
    if (!GameUpdater.binDir.exists()) {
      Util.log("%s does not exist! Updating..", GameUpdater.binDir.getPath());
      return true;
    }
    File nativesDir = new File(binDir, "natives");
    if (!nativesDir.exists()) {
      Util.log("%s does not exist! Updating..", nativesDir.getPath());
      return true;
    }
    File minecraft = new File(binDir, "minecraft.jar");
    if (!minecraft.exists()) {
      Util.log("%s does not exist! Updating..", minecraft.getPath());
      return true;
    }

    File lib = new File(binDir, "jinput.jar");
    if (!lib.exists()) {
      Util.log("%s does not exist! Updating..", lib.getPath());
      return true;
    }

    lib = new File(binDir, "lwjgl.jar");
    if (!lib.exists()) {
      Util.log("%s does not exist! Updating..", lib.getPath());
      return true;
    }

    lib = new File(binDir, "lwjgl_util.jar");
    if (!lib.exists()) {
      Util.log("%s does not exist! Updating..", lib.getPath());
      return true;
    }

    ModpackBuild build = ModpackBuild.getSpoutcraftBuild();
    String installed = MinecraftYML.getInstalledVersion();
    String required = build.getMinecraftVersion();

    if (!installed.equals(required)) {
      Util.log("Looking for minecraft.jar version %s Found %s Updating..", required, installed);
      return true;
    }
    return false;
  }

  public void extractCompressedFile(File destinationDirectory, File compressedFile) {
    extractCompressedFile(destinationDirectory, compressedFile, false);
  }

  protected void extractCompressedFile(File destinationDirectory, File compressedFile, Boolean deleteOnSuccess) {
    try {
      Util.log("Extracting %s to %s", compressedFile.getPath(), destinationDirectory.getPath());
      if (!compressedFile.exists()) {
        Util.log("[File not Found] Cannot find %s to extract", compressedFile.getPath());
        return;
      }
      if (!destinationDirectory.exists()) {
        Util.log("Creating directory %s", destinationDirectory.getPath());
        destinationDirectory.mkdirs();
      }
      ZipFile zipFile = new ZipFile(compressedFile);
      zipFile.setRunInThread(true);
      zipFile.extractAll(destinationDirectory.getAbsolutePath());
      ProgressMonitor monitor = zipFile.getProgressMonitor();
      while (monitor.getState() == ProgressMonitor.STATE_BUSY) {
        long totalProgress = monitor.getWorkCompleted() / monitor.getTotalWork();
        stateChanged(String.format("Extracting '%s'...", monitor.getFileName()), totalProgress);
      }
      File metainfDirectory = new File(destinationDirectory, "META-INF");
      if (metainfDirectory.exists()) {
        Util.removeDirectory(metainfDirectory);
      }
      stateChanged(String.format("Extracted '%s'", compressedFile.getPath()), 100f);
      if (monitor.getResult() == ProgressMonitor.RESULT_ERROR) {
        if (monitor.getException() != null) {
          monitor.getException().printStackTrace();
        } else {
          Util.log("An error occurred without any exception while extracting %s", compressedFile.getPath());
        }
      }
      Util.log("Extracted %s to %s", compressedFile.getPath(), destinationDirectory.getPath());
    } catch (ZipException e) {
      Util.log("An error occurred while extracting %s", compressedFile.getPath());
      e.printStackTrace();
    }
  }

  private File getNatives() throws Exception {
    String osName = System.getProperty("os.name").toLowerCase();
    String fname;

    if (osName.contains("win")) {
      fname = "windows_natives";
    } else if (osName.contains("mac")) {
      fname = "macosx_natives";
    } else if (osName.contains("solaris") || osName.contains("sunos")) {
      fname = "solaris_natives";
    } else if (osName.contains("linux") || osName.contains("unix")) {
      fname = "linux_natives";
    } else {
      throw new UnsupportedOSException();
    }

    if (!tempDir.exists())
      tempDir.mkdir();

    stateChanged("Downloading Native LWJGL files...", -1);
    DownloadUtils.downloadFile(getNativesUrl(fname), tempDir.getPath() + File.separator + (!SettingsUtil.isLatestLWJGL() ? "natives.jar.lzma" : "natives.zip"));
    stateChanged("Downloaded Native LWJGL files...", 100);

    if (!SettingsUtil.isLatestLWJGL()) {
      stateChanged("Extracting Native LWJGL files...", -1);
      extractLZMA(GameUpdater.tempDir.getPath() + File.separator + "natives.jar.lzma", GameUpdater.tempDir.getPath() + File.separator + "natives.zip");
      stateChanged("Extracted Native LWJGL files...", 100);
    }

    return new File(tempDir.getPath() + File.separator + "natives.jar.lzma");
  }

  public void updateSpoutcraft() throws Exception {
    performBackup();
    ModpackBuild build = ModpackBuild.getSpoutcraftBuild();

    tempDir.mkdirs();
    workDir.mkdirs();

    File mcCache = new File(cacheDir, "minecraft_" + build.getMinecraftVersion() + ".jar");
    File updateMC = new File(tempDir.getPath() + File.separator + "minecraft.jar");
    if (mcCache.exists()) {
      copy(mcCache, updateMC);
    }

    build.install();

    // TODO: remove this once this build has been out for a few weeks
    File spoutcraftVersion = new File(GameUpdater.workDir, "versionLauncher");
    spoutcraftVersion.delete();
  }

  public boolean isSpoutcraftUpdateAvailable() {
    if (!WORKING_DIRECTORY.exists()) {
      Util.log("%s does not exist! Updating..", WORKING_DIRECTORY.getPath());
      return true;
    }
    if (!GameUpdater.workDir.exists()) {
      Util.log("%s does not exist! Updating..", GameUpdater.workDir.getPath());
      return true;
    }

    ModpackBuild build = ModpackBuild.getSpoutcraftBuild();

    if (!build.getBuild().equalsIgnoreCase(build.getInstalledBuild())) {
      Util.log("Modpack version requested '%s' does not match installed version '%s'! Updating..", build.getBuild(), build.getInstalledBuild());
      return true;
    }
    return false;
  }

  public static long copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[1024 * 4];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  public static void copy(File input, File output) {
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;
    try {
      inputStream = new FileInputStream(input);
      outputStream = new FileOutputStream(output);
      copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();
    } catch (Exception e) {
      Util.log("Error copying file %s to %s", input, output);
      e.printStackTrace();
    }
  }

  public void performBackup() throws IOException {
    if (!backupDir.exists()) {
      backupDir.mkdir();
    }

    String date = new StringBuilder(new SimpleDateFormat("yyyy-MM-dd-kk.mm.ss").format(new Date())).toString();
    File zip = new File(GameUpdater.backupDir, date + "-backup.zip");

    if (!zip.exists()) {
      String rootDir = modpackDir + File.separator;
      HashSet<File> exclude = new HashSet<File>();
      exclude.add(GameUpdater.backupDir);
      if (!SettingsUtil.isWorldBackup()) {
        exclude.add(GameUpdater.savesDir);
      }

      File[] existingBackups = backupDir.listFiles();
      (new BackupCleanupThread(existingBackups)).start();
      zip.createNewFile();
      stateChanged(String.format("Backing up previous build to '%s'...", zip.getName()), 0);
      addFilesToExistingZip(zip, getFiles(modpackDir, exclude, rootDir), rootDir, false);
      stateChanged(String.format("Backed up previous build to '%s'...", zip.getName()), 100);

      if (modsDir.exists())
        FileUtils.deleteDirectory(modsDir);

      if (libsDir.exists())
        FileUtils.deleteDirectory(libsDir);

      if (coremodsDir.exists())
        FileUtils.deleteDirectory(coremodsDir);

      if (modconfigsDir.exists())
        FileUtils.deleteDirectory(modconfigsDir);

      if (resourceDir.exists())
        FileUtils.deleteDirectory(resourceDir);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static boolean canPlayOffline() {
    try {
      File path = (File) AccessController.doPrivileged(new PrivilegedExceptionAction() {
        @Override
        public Object run() throws Exception {
          return WORKING_DIRECTORY;
        }
      });
      if (!path.exists()) {
        return false;
      }
      if (!new File(path, "lastlogin").exists()) {
        return false;
      }

      path = new File(path, SettingsUtil.getModPackSelection() + File.separator + "bin");
      if (!path.exists()) {
        return false;
      }
      if (!new File(path, "minecraft.jar").exists()) {
        return false;
      }
      if (!new File(path, "modpack.jar").exists()) {
        return false;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean canPlayOffline(String modPackName) {
    try {
      File path = (File) AccessController.doPrivileged(new PrivilegedExceptionAction<File>() {
        @Override
        public File run() throws Exception {
          return WORKING_DIRECTORY;
        }
      });
      if (!path.exists()) {
        return false;
      }
      if (!new File(path, "lastlogin").exists()) {
        return false;
      }

      path = new File(path, modPackName + File.separator + "bin");
      if (!path.exists()) {
        return false;
      }
      if (!new File(path, "minecraft.jar").exists()) {
        return false;
      }
      if (!new File(path, "modpack.jar").exists()) {
        return false;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public void addFilesToExistingZip(File zipFile, Set<ClassFile> files, String rootDir, boolean progressBar) throws IOException {
    File tempFile = File.createTempFile(zipFile.getName(), null, zipFile.getParentFile());
    tempFile.delete();

    copy(zipFile, tempFile);
    boolean renameOk = zipFile.renameTo(tempFile);
    if (!renameOk) {
      if (tempFile.exists()) {
        zipFile.delete();
      } else {
        throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
      }
    }
    byte[] buf = new byte[1024];

    float progress = 0F;
    float progressStep = 0F;
    if (progressBar) {
      JarFile jarFile = new JarFile(tempFile);
      int jarSize = jarFile.size();
      jarFile.close();
      progressStep = 100F / (files.size() + jarSize);
    }

    ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
    ZipEntry entry = zin.getNextEntry();
    while (entry != null) {
      String name = entry.getName();
      ClassFile entryFile = new ClassFile(name);
      if (!name.contains("META-INF") && !files.contains(entryFile)) {
        out.putNextEntry(new ZipEntry(name));
        int len;
        while ((len = zin.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      }
      entry = zin.getNextEntry();

      progress += progressStep;
      if (progressBar) {
        stateChanged("Merging Modpack Files Into Minecraft Jar...", progress);
      }
    }
    zin.close();
    for (ClassFile file : files) {
      try {
        InputStream in = new FileInputStream(file.getFile());

        String path = file.getPath();
        path = path.replace(rootDir, "");
        path = path.replaceAll("\\\\", "/");
        out.putNextEntry(new ZipEntry(path));

        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }

        progress += progressStep;
        if (progressBar) {
          stateChanged("Merging Modpack Files Into Minecraft Jar...", progress);
        }

        out.closeEntry();
        in.close();
      } catch (IOException e) {
      }
    }

    out.close();
  }

  // I know that is is not the best method but screw it, I am tired of trying
  // to do it myself :P
  private void extractLZMA(String in, String out) throws Exception {
    String[] args = { "d", in, out };
    LzmaAlone.main(args);
  }

  public Set<ClassFile> getFiles(File dir, String rootDir) {
    return getFiles(dir, new HashSet<File>(), rootDir);
  }

  public Set<ClassFile> getFiles(File dir, Set<File> exclude, String rootDir) {
    HashSet<ClassFile> result = new HashSet<ClassFile>();
    for (File file : dir.listFiles()) {
      if (!exclude.contains(dir)) {
        if (file.isDirectory()) {
          result.addAll(this.getFiles(file, exclude, rootDir));
          continue;
        }
        result.add(new ClassFile(file, rootDir));
      }
    }
    return result;
  }

  @Override
  public void stateChanged(String fileName, float progress) {
    fileName = fileName.replace(WORKING_DIRECTORY.getPath(), "");
    this.listener.stateChanged(fileName, progress);
  }

  public void setListener(DownloadListener listener) {
    this.listener = listener;
  }
}
