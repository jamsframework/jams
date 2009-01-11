/*
 * TreeSearchDlg.java
 * Created on 21. Juni 2008, 01:03
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.juice.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import jams.gui.LHelper;
import jams.juice.JUICE;
import jams.juice.gui.tree.JAMSNode;
import jams.juice.gui.tree.JAMSTree;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TreeSearchDlg extends JDialog {

    private JTextField searchText;
    private String searchString = "";
    private JAMSTree tree;
    private Enumeration nodeEnum = null;
    private JButton searchButton;

    public TreeSearchDlg() {
        searchText = new JTextField();
        searchText.setBorder(BorderFactory.createEtchedBorder());
        searchText.setEditable(true);
        searchText.setColumns(20);
        searchText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                TreeSearchDlg.this.searchText.selectAll();
                searchComponent();
            }
        });
        
        searchButton = new JButton(JUICE.resources.getString("Search"));
        searchButton.setMargin(new Insets(4, 4, 4, 4));
        searchButton.setPreferredSize(new Dimension(60, 20));
        searchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                searchComponent();
            }
        });        
    }

    private void searchComponent() {

        String newSearchString = this.searchText.getText();
        if (newSearchString.equals("")) {
            return;
        }
        JAMSNode rootNode = (JAMSNode) tree.getModel().getRoot();

        if (!newSearchString.equals(searchString) || nodeEnum == null) {
            searchString = newSearchString;
            nodeEnum = rootNode.breadthFirstEnumeration();
        }

        Object nodeObject;
        JAMSNode jamsNode;

        while (nodeEnum.hasMoreElements()) {
            nodeObject = nodeEnum.nextElement();
            if (nodeObject.toString().toLowerCase().contains(searchString.toLowerCase())) {
                jamsNode = (JAMSNode) nodeObject;
                if ((jamsNode.getType() == JAMSNode.COMPONENT_NODE) || (jamsNode.getType() == JAMSNode.CONTEXT_NODE)) {
                    TreePath resultPath = new TreePath(jamsNode.getPath());
                    tree.scrollPathToVisible(resultPath);
                    tree.setSelectionPath(resultPath);
                    return;
                }
            }
        }

        nodeEnum = rootNode.depthFirstEnumeration();

        LHelper.showInfoDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Could_not_find_further_occurrences_of_") + "\"" + searchString + "\".", JUICE.resources.getString("Info"));

    }
}
