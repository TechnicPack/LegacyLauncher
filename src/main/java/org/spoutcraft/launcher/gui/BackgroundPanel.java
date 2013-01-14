package org.spoutcraft.launcher.gui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

class BackgroundPanel extends JPanel {
  private static final long serialVersionUID    = 1L;
  private ImageIcon         backgroundImageIcon = null;
  private Image             backgroundImage     = null;

  public void setBackgroundImage(ImageIcon imageIcon) {
    backgroundImageIcon = imageIcon;
    backgroundImage = backgroundImageIcon.getImage();
    validate();
    repaint(0, 0, backgroundImageIcon.getIconWidth(), backgroundImageIcon.getIconHeight());
  }

  @Override
  public void paint(Graphics g) {
    // Draws the img to the BackgroundPanel.
    if (backgroundImage != null)
      g.drawImage(backgroundImage, 0, 0, null);
    super.paint(g);
  }
}