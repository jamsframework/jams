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

import jams.tools.GUIHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import reg.spreadsheet.JAMSSpreadSheet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.StandardInputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import reg.gui.InputDSInfoPanel;
import reg.gui.TSPanel;
import reg.gui.TreePanel;
import reg.tree.DSTreeNode;
import reg.tree.FileObject;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DisplayManager implements Observer {

    private InputDSInfoPanel inputDSInfoPanel;

    private TSPanel tsPanel;

    private TreePanel treePanel;

    private HashMap<String, JPanel> dataPanels = new HashMap<String, JPanel>();

    private JAMSExplorer explorer;

    public DisplayManager(JAMSExplorer explorer) {
        this.explorer = explorer;
        treePanel = new TreePanel(explorer);
        inputDSInfoPanel = new InputDSInfoPanel();
        treePanel.getTree().addObserver(this);
    }

    // handle selection of tree nodes and show metadata
    public void update(Observable o, Object arg) {
        if (arg == null) {
            inputDSInfoPanel.updateDS(null);
            return;
        }
        DSTreeNode node = (DSTreeNode) arg;
        if (node.getType() == DSTreeNode.INPUT_DS) {
            try {
                StandardInputDataStore store = (StandardInputDataStore) explorer.getWorkspace().getInputDataStore(node.toString());
                inputDSInfoPanel.updateDS(store);
            } catch (Exception e) {
                explorer.getRuntime().sendErrorMsg(e.toString());
                e.printStackTrace();
            }
        } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
            //display info dlg
        }
    }

    public void removeAllDisplays() {
        for (String name : dataPanels.keySet()) {
            removeDisplay(name);
        }
    }

    public void removeDisplay(String name) {
        JPanel panel = dataPanels.get(name);
        explorer.getExplorerFrame().getTPane().remove(panel);
        dataPanels.remove(name);
    }

    public void displayDS(DSTreeNode node) {

        String dsID;

        if (node == null) {
            return;
        }
        switch (node.getType()) {

            case DSTreeNode.INPUT_DS:

                dsID = node.toString();
                InputDataStore store = explorer.getWorkspace().getInputDataStore(dsID);

                if (dataPanels.containsKey(dsID)) {

                    JPanel panel = dataPanels.get(dsID);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(panel);
                    return;

                }

                if (store instanceof TSDataStore) {

                    JAMSSpreadSheet spreadSheet = new JAMSSpreadSheet(explorer);
                    spreadSheet.init();
                    spreadSheet.setID(dsID);
                    dataPanels.put(dsID, spreadSheet);
                    explorer.getExplorerFrame().getTPane().addTab(dsID, spreadSheet);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(spreadSheet);
                    try {
                        spreadSheet.loadTSDS((TSDataStore) store, explorer.getWorkspace().getInputDirectory());
                    } catch (Exception e) {
                        GUIHelper.showErrorDlg(explorer.getExplorerFrame(), "An error occured while trying to read from datastore \"" + store.getID() + "\"", "Error");
                        e.printStackTrace();
                    }

                }
                break;

            case DSTreeNode.OUTPUT_DS:

                FileObject fo = (FileObject) node.getUserObject();

                dsID = fo.getFile().getName() + " " + fo.getFile().getParentFile().getName();

                if (dataPanels.containsKey(dsID)) {

                    JPanel panel = dataPanels.get(dsID);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(panel);
                    return;

                }

                try {

                    JPanel outputPanel = OutputPanelFactory.getOutputDSPanel(explorer, fo.getFile(), dsID);
                    dataPanels.put(dsID, outputPanel);
                    explorer.getExplorerFrame().getTPane().addTab(dsID, outputPanel);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(outputPanel);

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    /**
     * @return the infoPanel
     */
    public InputDSInfoPanel getInputDSInfoPanel() {
        return inputDSInfoPanel;
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
            tsPanel = new TSPanel(explorer);
        }

        return tsPanel;
    }
}
