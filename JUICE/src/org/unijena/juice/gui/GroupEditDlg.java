/*
 * GroupEditDlg.java
 * Created on 14. März 2007, 07:44
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.juice.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import org.unijena.jams.gui.LHelper;

/**
 *
 * @author Sven Kralisch
 */
public class GroupEditDlg extends JDialog {

    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(18, 10, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(18, 10, Image.SCALE_SMOOTH));
    private JList groupList = new JList();
    private String[] groupArray = new String[0];

    public GroupEditDlg(Frame owner) {
        super(owner);
        init();
    }

    private void init() {
        setModal(true);
        this.setTitle("");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        contentPanel.add(new JLabel("Groups:"), BorderLayout.NORTH);

        groupList.setModel(new AbstractListModel() {

            public int getSize() {
                return groupArray.length;
            }

            public Object getElementAt(int i) {
                return groupArray[i];
            }
        });
        contentPanel.add(groupList, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JButton upButton = new JButton();
        upButton.setToolTipText("Move up");
        upButton.setIcon(UP_ICON);
        upButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveUp();
            }
        });

        JButton downButton = new JButton();
        downButton.setToolTipText("Move down");
        downButton.setIcon(DOWN_ICON);
        downButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveDown();
            }
        });

        JPanel buttonPanel = new JPanel();
        GridBagLayout gbl2 = new GridBagLayout();
        buttonPanel.setLayout(gbl2);
        LHelper.addGBComponent(buttonPanel, gbl2, upButton, 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(buttonPanel, gbl2, downButton, 0, 1, 1, 1, 0, 0);

        JPanel okPanel = new JPanel();
        okPanel.add(okButton);

        getContentPane().add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        getContentPane().add(okPanel, BorderLayout.SOUTH);
        getContentPane().add(buttonPanel, BorderLayout.EAST);
        pack();
    }

    private void moveUp() {
        int index = groupList.getSelectedIndex();
        if (index > 0) {
            String tmp = groupArray[index - 1];
            groupArray[index - 1] = groupArray[index];
            groupArray[index] = tmp;
            groupList.setSelectedIndex(index - 1);
            groupList.updateUI();
        }
    }

    private void moveDown() {
        int index = groupList.getSelectedIndex();
        if (index < groupArray.length - 1) {
            String tmp = groupArray[index + 1];
            groupArray[index + 1] = groupArray[index];
            groupArray[index] = tmp;
            groupList.setSelectedIndex(index + 1);
            groupList.updateUI();
        }
    }

    private void newGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Group name:");
        ListModel model = groupList.getModel();
        String[] newArray = new String[model.getSize() + 1];
        for (int i = 0; i < model.getSize(); i++) {
            newArray[i] = (String) model.getElementAt(i);
        }
        newArray[model.getSize()] = groupName;
        update(newArray);
    }

    public void update(String[] groups) {
        groupArray = groups;
        groupList.updateUI();
        pack();
    }

    public String[] getGroups() {
        return groupArray;
    }

    public static void main(String[] args) {
        GroupEditDlg dlg = new GroupEditDlg(null);

        String[] groups = {"a", "b", "c"};

        dlg.update(groups);

        dlg.setVisible(true);
        System.exit(0);
    }
}
