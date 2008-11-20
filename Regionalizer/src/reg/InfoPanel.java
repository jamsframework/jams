/*
 * InfoPanel.java
 * Created on 19. November 2008, 10:47
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
package reg;

import jams.workspace.stores.DataStore;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import reg.tree.DSTreeNode;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class InfoPanel extends JPanel {

    private JLabel idLabel = new JLabel();

    public InfoPanel() {
        super();
        this.add(idLabel);
        Regionalizer.getTree().addObserver(new Observer() {

            public void update(Observable o, Object arg) {
                DSTreeNode node = (DSTreeNode) arg;
                System.out.println(node);
                if (node.getType() == DSTreeNode.INPUT_DS) {
                    DataStore store = Regionalizer.getTree().getWorkspace().getInputDataStore(node.toString());
                    InfoPanel.this.update(store);
                } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
//                    Regionalizer.getTree().getWorkspace().getO(node.toString());
                }

            }
        });
    }

    private void update(DataStore store) {
        idLabel.setText(store.getID());
    }
}
