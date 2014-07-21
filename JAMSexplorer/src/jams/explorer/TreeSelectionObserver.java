/*
 * TreeSelectionObserver.java
 * Created on 21. November 2008, 13:07
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
package jams.explorer;

import jams.workspace.JAMSWorkspace;
import jams.workspace.stores.DataStore;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jams.explorer.tree.DSTreeNode;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TreeSelectionObserver implements Observer {

    private JAMSWorkspace workspace;

    private JAMSExplorer explorer;

    public TreeSelectionObserver(JAMSWorkspace workspace) {
        this.workspace = workspace;
//        JAMSExplorer.getExplorerFrame().getTreePanel().getTree().addObserver(this);
    }

    public void update(Observable o, Object arg) {
        if (arg == null) {
//            JAMSExplorer.getExplorerFrame().getInfoPanel().updateDS(null);
            return;
        }
        DSTreeNode node = (DSTreeNode) arg;
        if (node.getType() == DSTreeNode.INPUT_DS) {
            try {
                DataStore store = workspace.getInputDataStore(node.toString());
//                JAMSExplorer.getExplorerFrame().getInfoPanel().updateDS(store);
            } catch (Exception e) {
                explorer.getRuntime().sendErrorMsg(e.toString());
                Logger.getLogger(TreeSelectionObserver.class.getName()).log(Level.SEVERE, null, e);
            }
        } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
//                    JAMSExplorer.getTree().getWorkspace().getO(node.toString());
        }

    }
}
