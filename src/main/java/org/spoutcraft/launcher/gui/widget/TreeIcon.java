package org.spoutcraft.launcher.gui.widget;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

class TreeIcon implements Icon {

	private static int	SIZE	= 0;

	public TreeIcon() {
	}

	public int getIconWidth() {
		return SIZE;
	}

	public int getIconHeight() {
		return SIZE;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		//System.out.println(c.getWidth() + " " + c.getHeight() + " " + x + " " + y);
	}
}