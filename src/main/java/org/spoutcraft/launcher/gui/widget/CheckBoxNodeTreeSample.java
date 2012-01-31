package org.spoutcraft.launcher.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

public class CheckBoxNodeTreeSample {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		class MJFrame extends JFrame implements WindowListener {

			public MJFrame(String string) {
				setTitle(string);
				addWindowListener(this);
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {

			}

			@Override
			public void windowDeiconified(WindowEvent e) {

			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

		}

		MJFrame frame = new MJFrame("CheckBox Tree");

		CheckBoxNode accessibilityOptions[] = { new CheckBoxNode("Move system caret with focus/selection changes", false), new CheckBoxNode("Always expand alt text for images", true) };
		CheckBoxNode browsingOptions[] = { new CheckBoxNode("Notify when downloads complete", true, 0), new CheckBoxNode("Disable script debugging", true, 0), new CheckBoxNode("Use AutoComplete", true), new CheckBoxNode("Browse in a new process", false) };
		Vector accessVector = new NamedVector("Accessibility", accessibilityOptions);
		Vector browseVector = new NamedVector("Browsing", browsingOptions);
		Object rootNodes[] = { accessVector, browseVector };
		Vector rootVector = new NamedVector("Root", rootNodes);

		Icon empty = new TreeIcon();
		UIManager.put("Tree.closedIcon", empty);
		UIManager.put("Tree.openIcon", empty);
		UIManager.put("Tree.collapsedIcon", empty);
		UIManager.put("Tree.expandedIcon", empty);

		JTree tree = new JTree(rootVector) {
			@Override
			protected void setExpandedState(TreePath path, boolean state) {
				if (state) {
					super.setExpandedState(path, state);
				}
			}
		};

		tree.getModel().addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				int a = 1;
				TreePath path = e.getTreePath();
				Object[] children = e.getChildren();
				if (children.length == 1) {
					if (children[0] instanceof DynamicUtilTreeNode) {
						DynamicUtilTreeNode node = (DynamicUtilTreeNode) children[0];
						if (((CheckBoxNode) node.getUserObject()).getGroupId() >= 0) {
							// Vector<DynamicUtilTreeNode> siblings =
							// node.getParent().children();
						}
					}
				}
			}
		});

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		tree.setCellRenderer(renderer);

		tree.setCellEditor(new CheckBoxNodeEditor(tree));
		tree.setEditable(true);

		JScrollPane scrollPane = new JScrollPane(tree);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.setSize(300, 150);
		frame.setVisible(true);
	}
}