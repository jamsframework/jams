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

import jams.gui.GUIHelper;
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
import javax.swing.JTabbedPane;
import reg.gui.InputDSInfoPanel;
import reg.gui.OutputDSPanel;
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
    
    private JTabbedPane spreadSheetTabs; 

    private HashMap<String, JPanel> spreadSheets = new HashMap<String, JPanel>();

    private JAMSExplorer regionalizer;

    public DisplayManager(JAMSExplorer regionalizer) {
        this.regionalizer = regionalizer;
        treePanel = new TreePanel(regionalizer);
        inputDSInfoPanel = new InputDSInfoPanel();
        treePanel.getTree().addObserver(this);
        spreadSheetTabs = new JTabbedPane();
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
                StandardInputDataStore store = (StandardInputDataStore) regionalizer.getWorkspace().getInputDataStore(node.toString());
                inputDSInfoPanel.updateDS(store);
            } catch (Exception e) {
                regionalizer.getRuntime().sendErrorMsg(e.toString());
                e.printStackTrace();
            }
        } else if (node.getType() == DSTreeNode.OUTPUT_DS) {
            //display info dlg
        }

    }
    
    public HashMap<String, JPanel> getSpreadSheets(){
        return spreadSheets;
    }
    
    public void displayDS(DSTreeNode node) {
        if (node == null) {
            return;
        }
        switch (node.getType()) {
            case DSTreeNode.INPUT_DS:
                InputDataStore store = regionalizer.getWorkspace().getInputDataStore(node.toString());
                if (store instanceof TSDataStore) {

                    JAMSSpreadSheet spreadSheet = new JAMSSpreadSheet(regionalizer);
                    spreadSheet.init();
                    spreadSheet.setID(node.toString());
                    
                    if(!spreadSheets.containsKey(node.toString())){
                        
                        spreadSheets.put(node.toString(), spreadSheet);
                        regionalizer.getExplorerFrame().addToTabbedPane(node.toString(), spreadSheet.getPanel());
                    }else{
                        if(spreadSheets.get(node.toString()) instanceof JAMSSpreadSheet){
                            spreadSheet = (JAMSSpreadSheet) spreadSheets.get(node.toString());
                            regionalizer.getExplorerFrame().showTab(spreadSheet.getPanel());
                        }
                    }
//                    spreadSheetTabs.add(node.toString(), spreadSheet.getPanel());
//
//                    JAMSExplorer.getExplorerFrame().updateMainPanel(spreadSheetTabs);
//                    JAMSExplorer.getExplorerFrame().updateMainPanel(spreadSheet.getPanel());
                    try {
                        spreadSheet.loadTSDS((TSDataStore) store, regionalizer.getWorkspace().getInputDirectory());
                    } catch (Exception e) {
                        GUIHelper.showErrorDlg(regionalizer.getExplorerFrame(), "An error occured while trying to read from datastore \"" + store.getID() + "\"", "Error");
                        e.printStackTrace();
                    }
                }
                break;
            case DSTreeNode.OUTPUT_DS:
                FileObject fo = (FileObject) node.getUserObject();
//                OutputDSPanel odsPanel = OutputDSPanel.createPanel(fo.getFile());
//                JAMSExplorer.getExplorerFrame().updateMainPanel(odsPanel);
                try {
                    JPanel outputPanel = OutputPanelFactory.getOutputDSPanel(regionalizer, fo.getFile());
                    if(outputPanel instanceof OutputDSPanel){
                        OutputDSPanel odspanel = (OutputDSPanel) outputPanel;
                        //JAMSSpreadSheet spreadsheet = odspanel.getSpreadsheet();
                    
//                    regionalizer.getExplorerFrame().addToTabbedPane(node.toString(),outputPanel);
                    
                        if(!spreadSheets.containsKey(fo.getFile().getName())){

                            spreadSheets.put(fo.getFile().getName(), odspanel);
    //                        regionalizer.getExplorerFrame().addToTabbedPane(node.toString(), outputPanel);
                            regionalizer.getExplorerFrame().addToTabbedPane(fo.getFile().getName(), odspanel);
                        }else{
                            outputPanel = spreadSheets.get(fo.getFile().getName());
                            regionalizer.getExplorerFrame().showTab(outputPanel);
                        }
                        
                    } else {
                        if(!spreadSheets.containsKey(fo.getFile().getName())){

                            spreadSheets.put(fo.getFile().getName(), outputPanel);
    //                        regionalizer.getExplorerFrame().addToTabbedPane(node.toString(), outputPanel);
                            regionalizer.getExplorerFrame().addToTabbedPane(fo.getFile().getName(), outputPanel);
                        }else{
                            regionalizer.getExplorerFrame().showTab(spreadSheets.get(fo.getFile().getName()));
                        }
                    }
                    
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
            tsPanel = new TSPanel(regionalizer);
        }
        return tsPanel;
    }

//    /**
//     * @return the spreadSheets
//     */
//    public HashMap<String, JAMSSpreadSheet> getSpreadSheets() {
//        return spreadSheets;
//    }
}
