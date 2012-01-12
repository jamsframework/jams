/*
 * TreePanel.java
 * Created on 27. Dezember 2006, 13:13
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jamsui.juice.gui.tree.JAMSTree;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class TreePanel extends JPanel {

    private static final Dimension BUTTON_DIMENSION = new Dimension(40, 20);
    private JScrollPane treeScrollPane = new JScrollPane();
    private JAMSTree tree;
    private JPanel lowerPanel = new JPanel();
    private JButton expandButton, collapseButton;
    

    /**
     * Creates a new instance of TreePanel
     */
    public TreePanel() {
        super();

        this.setLayout(new BorderLayout());

        expandButton = new JButton(JAMS.i18n("+"));
        expandButton.setMargin(new Insets(4, 4, 4, 4));
        expandButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree != null) {
                    tree.expandAll();
                }
            }
        });
        expandButton.setPreferredSize(BUTTON_DIMENSION);
        expandButton.setToolTipText(JAMS.i18n("Expand_Tree"));

        collapseButton = new JButton(JAMS.i18n("-"));
        collapseButton.setMargin(new Insets(4, 4, 4, 4));
        collapseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree != null) {
                    tree.collapseAll();
                }
            }
        });
        collapseButton.setPreferredSize(BUTTON_DIMENSION);
        collapseButton.setToolTipText(JAMS.i18n("Collapse_Tree"));

        lowerPanel.add(expandButton);
        lowerPanel.add(collapseButton);

        this.add(treeScrollPane, BorderLayout.CENTER);
        this.add(lowerPanel, BorderLayout.SOUTH);
    }

    public void addCustomButton(JButton button) {
        //button.setPreferredSize(new Dimension(width, BUTTON_DIMENSION.height));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, BUTTON_DIMENSION.height));
        lowerPanel.add(button);
        lowerPanel.updateUI();
    }

    public void setEnabled(boolean enabled) {
        for (Component c : lowerPanel.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    public void setTree(JAMSTree tree) {
        this.tree = tree;
        this.tree.setExpandsSelectedPaths(true);
        treeScrollPane.setViewportView(tree);
    }
}
