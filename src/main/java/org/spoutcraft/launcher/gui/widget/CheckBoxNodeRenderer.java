package org.spoutcraft.launcher.gui.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

class CheckBoxNodeRenderer implements TreeCellRenderer {
	private final JToggleButton						leafCheckBoxRenderer		= new JCheckBox();
	private final JToggleButton						leafRadioButtonRenderer	= new JRadioButton();
	private JToggleButton									leafRenderer;
	private final DefaultTreeCellRenderer	nonLeafRenderer					= new DefaultTreeCellRenderer();
	private final Color										selectionBorderColor;
	private final Color										selectionForeground;
	private final Color										selectionBackground;
	private final Color										textForeground;
	private final Color										textBackground;
	private final Insets									insets									= new Insets(0, 0, 0, 0);

	protected JToggleButton getLeafRenderer() {
		return leafRenderer;
	}

	public CheckBoxNodeRenderer() {
		Font fontValue;
		fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) {
			leafCheckBoxRenderer.setFont(fontValue);
			leafRadioButtonRenderer.setFont(fontValue);
		}
		Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		leafCheckBoxRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
		leafCheckBoxRenderer.setMargin(insets);
		leafRadioButtonRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
		leafRadioButtonRenderer.setMargin(insets);

		selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		Component returnValue;
		if (leaf) {
			if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof CheckBoxNode) {
					CheckBoxNode node = (CheckBoxNode) userObject;
					leafRenderer = node.getGroupId() == -1 ? leafCheckBoxRenderer : leafRadioButtonRenderer;
					leafRenderer.putClientProperty("groupId", node.getGroupId());
				}
			}
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
			leafRenderer.setText(stringValue);
			leafRenderer.setSelected(false);

			leafRenderer.setEnabled(tree.isEnabled());

			if (selected) {
				leafRenderer.setForeground(selectionForeground);
				leafRenderer.setBackground(selectionBackground);
			} else {
				leafRenderer.setForeground(textForeground);
				leafRenderer.setBackground(textBackground);
			}

			if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof CheckBoxNode) {
					CheckBoxNode node = (CheckBoxNode) userObject;
					leafRenderer.setText(node.getText());
					leafRenderer.setSelected(node.isSelected());
				}
			}
			returnValue = leafRenderer;
		} else {
			returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		return returnValue;
	}
}