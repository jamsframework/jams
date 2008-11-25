/*
 * DisplayManager.java
 * Created on 21. November 2008, 15:55
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

import jams.gui.LHelper;
import reg.spreadsheet.JAMSSpreadSheet;
import jams.workspace.stores.DataStore;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.Observable;
import java.util.Observer;
import reg.gui.InfoPanel;
import reg.gui.TSPanel;
import reg.gui.TreePanel;
import reg.tree.DSTreeNode;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DisplayManager implements Observer {

    private InfoPanel infoPanel;
    private TSPanel tsPanel;
    private TreePanel treePanel;
    private JAMSSpreadSheet spreadsheet;

    public DisplayManager() {
        treePanel = new TreePanel();
        infoPanel = new InfoPanel();
        treePanel.getTree().addObserver(this);

        String[] default_headers = {""};
        spreadsheet = new JAMSSpreadSheet(Regionalizer.getRegionalizerFrame(), default_headers);
        spreadsheet.init();
    }

    // handle selection of tree nodes and show metadata
    public void update(Observable o, Object arg) {
        if (arg == null) {
            infoPanel.updateDS(null);
            return;
        }
        DSTreeNode node = (DSTreeNode) arg;
        if (node.getType() == DSTreeNode.INPUT_DS) {
            try {
                DataStore store = Regionalizer.getRegionalizerFrame().getWorkspace().getInputDataStore(node.toString());
                infoPanel.updateDS(store);
            } catch (Exception e) {
                Regionalizer.getRuntime().sendErrorMsg(e.toString());
                e.printStackTrace();
            }
        } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
//                    Regionalizer.getTree().getWorkspace().getO(node.toString());
        }

    }

    public void displayDS(DSTreeNode node) {
        switch (node.getType()) {
            case DSTreeNode.INPUT_DS:
                InputDataStore store = Regionalizer.getRegionalizerFrame().getWorkspace().getInputDataStore(node.toString());
                if (store instanceof TSDataStore) {
                    Regionalizer.getRegionalizerFrame().updateMainPanel(spreadsheet.getPanel());
                    try {
                        spreadsheet.loadTSDS((TSDataStore) store);
                    } catch (Exception e) {
                        LHelper.showErrorDlg(Regionalizer.getRegionalizerFrame(), "An error occured while trying to read from datastore \"" + store.getID() + "\"", "Error");
                        e.printStackTrace();
                    }
                }
                break;
            case DSTreeNode.OUTPUT_DS:
                //handle output nodes here
                break;
        }
    }

    /**
     * @return the infoPanel
     */
    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    /**
     * @return the treePanel
     */
    public TreePanel getTreePanel() {
        return treePanel;
    }

    /**
     * @return the tSPanel
     */
    public TSPanel getTSPanel() {
        if (tsPanel == null) {
            tsPanel = new TSPanel();
        }
        return tsPanel;
    }
}
