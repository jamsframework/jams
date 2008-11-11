/*
 * SearchDlg.java
 * Created on 10. November 2008, 16:32
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
package juice.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import juice.gui.tree.JAMSNode;
import juice.gui.tree.JAMSTree;
import juice.gui.tree.LibTree;
import juice.gui.tree.ModelTree;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SearchDlg extends JDialog {

    private JAMSTree tree;
    private Enumeration treeEnum;

    public SearchDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JButton findButton = new JButton(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Find"));
        findButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processFind();
            }
        });

        JButton closeButton = new JButton(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Close"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processClose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(findButton);
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
    }

    public void setTree(JAMSTree tree) {
        this.tree = tree;

        if (tree instanceof LibTree) {
            this.setTitle(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Find_in_Libraries"));
        } else if (tree instanceof ModelTree) {
            this.setTitle(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Find_in_Model"));
        }

        JAMSNode rootNode = (JAMSNode) tree.getModel().getRoot();
        treeEnum = rootNode.breadthFirstEnumeration();

    }

    private void processFind() {

        while (treeEnum.hasMoreElements()) {

            JAMSNode node = (JAMSNode) treeEnum.nextElement();
            if ((node.getType() == JAMSNode.COMPONENT_NODE) || (node.getType() == JAMSNode.CONTEXT_NODE)) {
                System.out.println(node);
                return;
            }

        }
    }

    private void processClose() {
        setVisible(false);
    }
}
