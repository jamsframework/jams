/*
 * DisplayManager.java
 * Created on 21. November 2008, 15:55
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

import jams.JAMS;
import jams.JAMSException;
import jams.JAMSLogging;
import jams.gui.tools.GUIHelper;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jams.workspace.stores.InputDataStore;
import java.io.FileNotFoundException;
import java.io.IOException;
import jams.explorer.spreadsheet.JAMSSpreadSheet;
import jams.workspace.stores.StandardInputDataStore;
import jams.workspace.stores.TSDataStore;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import optas.data.DataCollection;
import jams.explorer.gui.DataCollectionView;
import jams.explorer.gui.InputDSInfoPanel;
import jams.explorer.gui.TSPanel;
import jams.explorer.gui.TreePanel;
import jams.explorer.spreadsheet.SpreadsheetConstants;
import jams.explorer.tree.DSTreeNode;
import jams.explorer.tree.FileObject;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DisplayManager implements Observer {

    private InputDSInfoPanel inputDSInfoPanel;
    private TSPanel tsPanel;
    private TreePanel treePanel;
    private HashMap<String, Component> dataPanels = new HashMap<String, Component>();
    private JAMSExplorer explorer;
    private JAMSSpreadSheet spreadSheet = null;

    public DisplayManager(JAMSExplorer explorer) {
        this.explorer = explorer;
        treePanel = new TreePanel(explorer);
        inputDSInfoPanel = new InputDSInfoPanel();
        treePanel.getTree().addObserver(this);
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show, Logger.getLogger(DisplayManager.class.getName()));
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
                Logger.getLogger(DisplayManager.class.getName()).log(Level.SEVERE, null, e);
            }
        } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
            //display info dlg
        }
    }

    public synchronized void removeAllDisplays() {
        for (String name : dataPanels.keySet()) {
            removeDisplay(name);
        }
    }

    public void removeDisplay(String name) {
        Component panel = dataPanels.get(name);
        explorer.getExplorerFrame().getTPane().remove(panel);
        dataPanels.remove(name);
    }

    public void removeDisplay(Component panel) {
        for (Map.Entry<String, Component> e : dataPanels.entrySet()) {
            if (e.getValue() == panel) {
                removeDisplay(e.getKey());
            }
        }
    }    
    
    public void displayDS(DSTreeNode node) {

        String dsID;
        File datFile;

        if (node == null) {
            return;
        }
        switch (node.getType()) {

            case DSTreeNode.INPUT_DS:

                dsID = node.toString();
                InputDataStore store = explorer.getWorkspace().getInputDataStore(dsID);

                if (dataPanels.containsKey(dsID)) {

                    Component panel = dataPanels.get(dsID);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(panel);
                    return;

                }

                if (store instanceof TSDataStore) {

                    spreadSheet = new JAMSSpreadSheet(explorer);
                    spreadSheet.init();
                    spreadSheet.setID(dsID);
                    dataPanels.put(dsID, spreadSheet);
                    explorer.getExplorerFrame().getTPane().addTab(dsID, spreadSheet);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(spreadSheet);
                    try {
                        spreadSheet.loadTSDS((TSDataStore) store, explorer.getWorkspace().getInputDirectory());
                    } catch (Throwable e) {
                        GUIHelper.showErrorDlg(explorer.getExplorerFrame(), 
                                JAMS.i18n("AN_ERROR_OCCURED_WHILE_TRYING_TO_READ_FROM_DATASTORE_") 
                                + store.getID() + "\"\n" + e.toString() + "\n" + StringTools.getStackTraceString(e.getStackTrace()),
                                JAMS.i18n("ERROR"));
                    }

                }
                break;

            case DSTreeNode.OUTPUT_DS:

                FileObject fo = (FileObject) node.getUserObject();
                datFile = fo.getFile();

                dsID = getIdFromName(datFile);
                if (dataPanels.containsKey(dsID)) {

                    Component panel = dataPanels.get(dsID);
                    if (explorer.getExplorerFrame().getTPane().indexOfComponent(panel)!=-1)
                        explorer.getExplorerFrame().getTPane().setSelectedComponent(panel);
                    else
                        explorer.getExplorerFrame().getTPane().addTab(dsID, panel);
                    return;

                }

                try {

                    Component outputPanel = OutputPanelFactory.getOutputDSPanel(explorer, fo, dsID);
                    dataPanels.put(dsID, outputPanel);
                    explorer.getExplorerFrame().getTPane().addTab(dsID, outputPanel);
                    explorer.getExplorerFrame().getTPane().setSelectedComponent(outputPanel);

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DisplayManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DisplayManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JAMSException ex) {
                    
                }
                break;
        }
    }

    public DataCollection getCurrentDataCollection(){
        JComponent panel = (JComponent)explorer.getExplorerFrame().getTPane().getSelectedComponent();
        if (panel==null)
            return null;
        if (panel instanceof DataCollectionView){
            DataCollectionView dcView = (DataCollectionView)panel;
            return dcView.getDataCollection();
        }
        return null;
    }

    public void deleteDS(DSTreeNode node) {

        File datFile;

        if (node == null) {
            return;
        }
        switch (node.getType()) {

            case DSTreeNode.INPUT_DS:

//                dsID = node.toString();
//                InputDataStore store = explorer.getWorkspace().getInputDataStore(dsID);
//
//                if (dataPanels.containsKey(dsID)) {
//                    dataPanels.remove(dsID);
//                }
//
//                ttpFile = explorer.getWorkspace().getInputDirectory();
//
//                if(ttpFile.exists()){
//                    ttpFile.delete();
//                }

                break;

            case DSTreeNode.OUTPUT_DS:

                FileObject fo = (FileObject) node.getUserObject();
                datFile = fo.getFile();
                deleteOutputFile(datFile);
                explorer.getExplorerFrame().update();
                break;

            case DSTreeNode.OUTPUT_DIR:
                String delDirName = node.getUserObject().toString();
                File currentOutDir = explorer.getWorkspace().getOutputDataDirectory();
                if (currentOutDir == null || !currentOutDir.getName().equals(delDirName)) {
                    File[] outDirs = explorer.getWorkspace().getOutputDataDirectories();
                    for (File outDir : outDirs) {
                        if (outDir.getName().equals(delDirName)) {
                            //System.out.println("try to delete " + outDir.getName());
                            for (File file : explorer.getWorkspace().getOutputDataFiles(outDir)) {
                                deleteOutputFile(file);
                            }
                            FileTools.deleteFiles(outDir.listFiles()); //may be some db-files left?
                            outDir.delete();
                            explorer.getExplorerFrame().update();
                        }
                    }
                }
                break;
        }
    }

    /**
     * encapsulates special id-stuff
     * @param theFile
     * @return id
     * @todo: this method should be a static method of outbput-datastore or ??
     */
    private String getIdFromName(File theFile) {
        String id = theFile.getName() + " " + theFile.getParentFile().getName();
        return id;
    }

    private void deleteOutputFile(File datFile) {
        String dsID;
        File ttpFile;

        dsID = getIdFromName(datFile);

        if (dataPanels.containsKey(dsID)) {
            removeDisplay(dsID);
            dataPanels.remove(dsID);
        }

        String fileID = datFile.getName();
        StringTokenizer name_tokenizer = new StringTokenizer(fileID, ".");
        String filename = "";
        if (name_tokenizer.hasMoreTokens()) {
            filename = name_tokenizer.nextToken() + SpreadsheetConstants.FILE_ENDING_TTP;
        } else {
            filename = fileID + SpreadsheetConstants.FILE_ENDING_TTP;
        }

        ttpFile = new File(datFile.getParent(), filename);
        if (ttpFile.exists()) {
            ttpFile.delete();
        }
        if (datFile.exists()) {
            datFile.delete();
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

    /**
     * @return the spreadSheet
     */
    public JAMSSpreadSheet getSpreadSheet() {
        return spreadSheet;
    }
}
