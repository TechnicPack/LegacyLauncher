package org.spoutcraft.launcher.gui.widget;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

class TreeIcon implements Icon {

  private static int SIZE = 0;

  public TreeIcon() {
  }

  @Override
  public int getIconWidth() {
    return SIZE;
  }

  @Override
  public int getIconHeight() {
    return SIZE;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    // System.out.println(c.getWidth() + " " + c.getHeight() + " " + x + " "
    // + y);
  }
}