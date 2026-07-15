/*
 * TreePanel.java
 * Created on 19. November 2008, 10:46
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
 *
 */

package jams.explorer.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jams.explorer.JAMSExplorer;
import jams.explorer.tree.DSTree;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TreePanel extends JPanel {

    private DSTree tree;

    public TreePanel(JAMSExplorer explorer) {
        super();
        tree = new DSTree(explorer);
        JScrollPane scrollPane = new JScrollPane(tree);
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void update() {
        tree.update();
    }

    /**
     * @return the tree
     */
    public DSTree getTree() {
        return tree;
    }
    
}
