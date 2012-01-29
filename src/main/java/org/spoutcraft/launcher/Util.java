package org.spoutcraft.launcher;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
		System.out.println(String.format(formatString, params));
	}

	public static void addComboItem(JComboBox combobox, String label, String value) {
		combobox.addItem(new ComboItem(label, value));
	}

	public static void setSelectedComboByLabel(JComboBox<ComboItem> combobox, String label) {
		for (int i = 0; i < combobox.getItemCount(); i++) {
			if ((combobox.getItemAt(i)).getLabel().equalsIgnoreCase(label)) {
				combobox.setSelectedIndex(i);
			}
		}
	}

	public static void setSelectedComboByValue(JComboBox<ComboItem> combobox, String value) {
		for (int i = 0; i < combobox.getItemCount(); i++) {
			if ((combobox.getItemAt(i)).getValue().equalsIgnoreCase(value)) {
				combobox.setSelectedIndex(i);
			}
		}
	}

	public static String getSelectedValue(JComboBox<ComboItem> combobox) {
		return ((ComboItem) combobox.getSelectedItem()).getValue();
	}
}