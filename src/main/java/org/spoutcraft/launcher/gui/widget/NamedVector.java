package org.spoutcraft.launcher.gui.widget;

import java.util.Vector;

class NamedVector<T> extends Vector<T> {
  private static final long serialVersionUID = -7265801404397102337L;
  String                    name;

  public NamedVector(String name) {
    this.name = name;
  }

  public NamedVector(String name, T elements[]) {
    this.name = name;
    for (int i = 0, n = elements.length; i < n; i++) {
      add(elements[i]);
    }
  }

  public String toString() {
    return "[" + name + "]";
  }
}