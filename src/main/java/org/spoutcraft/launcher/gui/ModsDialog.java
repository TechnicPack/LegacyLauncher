package org.spoutcraft.launcher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

public class ModsDialog extends JDialog implements ActionListener {
  private static final long           serialVersionUID = -6662344712140426961L;
  private final JPanel                contentPanel     = new JPanel();
  protected JToggleButton[]           modLists;
  protected Map<Integer, ButtonGroup> groups;

  /**
   * Create the dialog.
   */
  public ModsDialog(List<Map<String, String>> modNameList) {
    setTitle("Select Mods to Install");
    setBounds(100, 100, 616, 492);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new BorderLayout());
    contentPanel.setOpaque(false);

    groups = new HashMap<Integer, ButtonGroup>();

    if (modNameList != null) {
      modLists = new JToggleButton[modNameList.size()];
      JToggleButton item = null;

      for (int i = 0; i < modNameList.size(); i++) {
        Map<String, String> modDetails = modNameList.get(i);
        if (modDetails.containsKey("groupid")) {
          item = new JRadioButton(modDetails.get("name"), false);
          int groupid = Integer.parseInt(modDetails.get("groupid"));
          if (!groups.containsKey(groupid)) {
            groups.put(groupid, new ButtonGroup());
          }
          groups.get(groupid).add(item);
        } else
          item = new JCheckBox(modDetails.get("name"), false);

        item.setOpaque(false);
        item.setFocusPainted(false);
        item.setHorizontalAlignment(SwingConstants.LEFT);

        modLists[i] = item;
      }
    } else {
      modLists = new JCheckBox[0];
    }

    getContentPane().add(contentPanel, BorderLayout.CENTER);
    {
      CheckBoxList modList = new CheckBoxList();
      modList.setOpaque(false);
      modList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      modList.setModel(new AbstractListModel<JToggleButton>() {
        private static final long serialVersionUID = 6496577973696318501L;

        public int getSize() {
          return modLists.length;
        }

        public JToggleButton getElementAt(int index) {
          return modLists[index];
        }
      });
      contentPanel.add(modList);
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton installButton = new JButton("Install");
        installButton.setActionCommand("Install");
        installButton.addActionListener(this);
        buttonPane.add(installButton);
      }
      {
        JButton uninstallButton = new JButton("Uninstall");
        uninstallButton.setActionCommand("Uninstall");
        uninstallButton.addActionListener(this);
        buttonPane.add(uninstallButton);
      }
      {
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton);
      }
    }
  }

  public List<Boolean> ModsSelected = new ArrayList<Boolean>();

  public void actionPerformed(ActionEvent evt) {
    String id = evt.getActionCommand();
    if (id.equals("OK")) {
      ModsSelected.clear();
      for (int i = 0; i < modLists.length; i++) {
        AbstractButton button = modLists[i];
        ModsSelected.add(button.isSelected());
      }

      this.setVisible(false);
      this.dispose();
    } else if (id.equals("Cancel")) {
      this.setVisible(false);
      this.dispose();
    }
  }

}
