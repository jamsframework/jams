/*
 * TreePanel.java
 * Created on 27. Dezember 2006, 13:13
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import org.unijena.jams.gui.LHelper;
import org.unijena.juice.*;
import org.unijena.juice.gui.tree.JAMSNode;
import org.unijena.juice.gui.tree.JAMSTree;

/**
 *
 * @author S. Kralisch
 */
public class TreePanel extends JPanel {
    
    private static final Dimension BUTTON_DIMENSION = new Dimension(40,20);
    
    private JScrollPane treeScrollPane = new JScrollPane();
    private JAMSTree tree;
    private JTextField searchText;
    private Enumeration nodeEnum = null;
    private String searchString = "";
    
    
    /**
     * Creates a new instance of TreePanel
     */
    public TreePanel() {
        super();
        
        this.setLayout(new BorderLayout());
        
        JPanel searchPanel = new JPanel();
        JPanel collapsePanel = new JPanel();
        
        searchText = new JTextField();
        searchText.setEditable(true);
        searchText.setColumns(10);
        searchText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TreePanel.this.searchText.selectAll();
                searchComponent();
            }
        });
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchComponent();
            }
        });
        
        searchPanel.add(searchText);
        searchPanel.add(searchButton);
        
        
        JButton expandButton = new JButton("+");
        expandButton.setMargin(new Insets(4, 4, 4, 4));
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tree != null) {
                    tree.expandAll();
                }
            }
        });
        expandButton.setPreferredSize(BUTTON_DIMENSION);
        expandButton.setToolTipText("Expand Tree");
        
        JButton collapseButton = new JButton("-");
        collapseButton.setMargin(new Insets(4, 4, 4, 4));
        collapseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tree != null) {
                    tree.collapseAll();
                }
            }
        });
        collapseButton.setPreferredSize(BUTTON_DIMENSION);
        collapseButton.setToolTipText("Collapse Tree");

        collapsePanel.add(expandButton);
        collapsePanel.add(collapseButton);
        
        
        this.add(searchPanel, BorderLayout.NORTH);
        this.add(treeScrollPane, BorderLayout.CENTER);
        this.add(collapsePanel, BorderLayout.SOUTH);
    }
    
    private void searchComponent() {
        
        String newSearchString =  TreePanel.this.searchText.getText();
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
        
        LHelper.showInfoDlg(JUICE.getJuiceFrame(), "Could not find further occurrences of \"" + searchString + "\".", "Info");
        
    }
    
    public void setTree(JAMSTree tree) {
        this.tree = tree;
        this.tree.setExpandsSelectedPaths(true);
        treeScrollPane.setViewportView(tree);
    }
    
}
