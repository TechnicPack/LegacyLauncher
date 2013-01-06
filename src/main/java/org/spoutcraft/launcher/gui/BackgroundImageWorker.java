package org.spoutcraft.launcher.gui;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import org.spoutcraft.launcher.async.Download;

public class BackgroundImageWorker extends SwingWorker<Object, Object> {

  private static final String SPLASH_URL = "http://mirror.technicpack.net/Technic/splash/01.png";
  private File                backgroundImage;
  private BackgroundPanel     background;

  public BackgroundImageWorker(File backgroundImage, BackgroundPanel background) {
    this.backgroundImage = backgroundImage;
    this.background = background;
  }

  @Override
  protected Object doInBackground() {
    try {
      if (!backgroundImage.exists()) {
        Download download = new Download(SPLASH_URL, backgroundImage.getPath());
        download.run();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void done() {
    background.setBackgroundImage(new ImageIcon(backgroundImage.getPath()));
  }
}
