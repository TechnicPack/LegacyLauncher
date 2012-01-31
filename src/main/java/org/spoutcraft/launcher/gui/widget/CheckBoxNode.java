package org.spoutcraft.launcher.gui.widget;

public class CheckBoxNode {
	private String	text;
	private boolean	selected;
	private int			groupId;

	public CheckBoxNode(String text, boolean selected) {
		this(text, selected, -1);
	}

	public CheckBoxNode(String text, boolean selected, int groupId) {
		this.text = text;
		this.selected = selected;
		this.groupId = groupId;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean newValue) {
		selected = newValue;
	}

	public String getText() {
		return text;
	}

	public void setText(String newValue) {
		text = newValue;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[" + text + "/" + selected + "]";
	}
}