package org.spoutcraft.launcher;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComboBox;

public class Util {

  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ex) {
      // ignore
    }
  }

  public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    java.util.Collections.sort(list);
    return list;
  }

  public static void log(String formatString, Object... params) {
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning(String.format(formatString, params));
  }

  public static void logi(String formatString, Object... params) {
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(String.format(formatString, params));
  }

  public static void addComboItem(JComboBox combobox, String label, String value) {
    combobox.addItem(new ComboItem(label, value));
  }

  public static void setSelectedComboByLabel(JComboBox combobox, String label) {
    for (int i = 0; i < combobox.getItemCount(); i++) {
      if (((ComboItem) combobox.getItemAt(i)).getLabel().equalsIgnoreCase(label)) {
        combobox.setSelectedIndex(i);
      }
    }
  }

  public static void setSelectedComboByValue(JComboBox combobox, String value) {
    for (int i = 0; i < combobox.getItemCount(); i++) {
      if (((ComboItem) combobox.getItemAt(i)).getValue().equalsIgnoreCase(value)) {
        combobox.setSelectedIndex(i);
      }
    }
  }

  public static String getSelectedValue(JComboBox combobox) {
    return ((ComboItem) combobox.getSelectedItem()).getValue();
  }

  public static List<String> readTextFromJar(String s) {
    InputStream is = null;
    BufferedReader br = null;
    String line;
    ArrayList<String> list = new ArrayList<String>();

    try {
      is = FileUtils.class.getResourceAsStream(s);
      br = new BufferedReader(new InputStreamReader(is));
      while (null != (line = br.readLine())) {
        list.add(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (br != null)
          br.close();
        if (is != null)
          is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return list;
  }

  public static String getBuild() {
    List<String> lines = null;
    try {
      lines = readTextFromJar("/META-INF/maven/org.spoutcraft/technic-launcher/pom.properties");
    } catch (NullPointerException e) {
    }
    for (String line : lines) {
      if (line.contains("version")) {
        return line.replace("version=", "");
      }
    }
    return Main.build;
  }

  public static boolean removeDirectory(File directory) {
    if (directory == null)
      return false;
    if (!directory.exists())
      return true;
    if (!directory.isDirectory())
      return false;

    String[] list = directory.list();

    // Some JVMs return null for File.list() when the
    // directory is empty.
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        File entry = new File(directory, list[i]);
        if (entry.isDirectory()) {
          if (!removeDirectory(entry))
            return false;
        } else {
          if (!entry.delete())
            return false;
        }
      }
    }

    return directory.delete();
  }
}