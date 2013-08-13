/*
 * DSTree.java
 * Created on 19. November 2008, 17:58
 *
 * This file is part of JAMS
 * Copyright (C) 2008 FSU Jena
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
package reg.tree;

import jams.JAMS;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.J2KTSDataStore;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import optas.data.DataCollection;
import reg.JAMSExplorer;
import reg.gui.ImportMonteCarloDataPanel;

/**
 *
 * @author S. Kralisch
 */
public class DSTree extends JAMSTree {

    private static final String ROOT_NAME = JAMS.i18n("DATENSPEICHER"), INPUT_NAME = JAMS.i18n("EINGABEDATEN"), OUTPUT_NAME = JAMS.i18n("AUSGABEDATEN");

    private JPopupMenu popupDS;    
    private JPopupMenu popupDir;

    private NodeObservable nodeObservable = new NodeObservable();

    private JAMSExplorer explorer;

    JMenuItem addToEnsembleItem = new JMenuItem(JAMS.i18n("ADD_TO_ENSEMBLE"));

    public DSTree(JAMSExplorer explorer) {
        super();

        this.explorer = explorer;
        setEditable(false);

        JMenuItem detailItem = new JMenuItem(JAMS.i18n("ZEIGE_DATEN"));
        //detailItem.setAccelerator(KeyStroke.getKeyStroke('D'));
        detailItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                displayDSData();
            }
        });

        JMenuItem deleteFileItem = new JMenuItem(JAMS.i18n("DELETE"));
        deleteFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteFileItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteDSFile();
            }
        });

        JMenuItem deleteDirItem = new JMenuItem(JAMS.i18n("DELETE"));
        deleteDirItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteDirItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteDSDir();
            }
        });

        
        addToEnsembleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
        addToEnsembleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToEnsemble();
            }
        });
        
        popupDS = new JPopupMenu();
        popupDS.add(detailItem);
        popupDS.add(deleteFileItem);
        popupDS.add(addToEnsembleItem);
        
        popupDir = new JPopupMenu();
        popupDir.add(deleteDirItem);

        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    showPopup(evt);
                }
            }

            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    displayDSData();
                }
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (getSelectionPath() != null) {
                    nodeObservable.setNode((DSTreeNode) getLastSelectedPathComponent());
                } else {
                    nodeObservable.setNode(null);
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_F5){
                    update();
                }
            }
        });
        this.setVisible(false);
    }

    private DSTreeNode createInputNode() {
        DSTreeNode inputRoot = new DSTreeNode(INPUT_NAME, DSTreeNode.INPUT_ROOT);
        Set<String> inIDs = explorer.getWorkspace().getInputDataStoreIDs();
        List<String> inIDList = explorer.getWorkspace().getSortedInputDataStoreIDs();
        for (String id : inIDList) {
            DSTreeNode dsNode = new DSTreeNode(id, DSTreeNode.INPUT_DS);
            inputRoot.add(dsNode);
        }
        return inputRoot;
    }

    private DSTreeNode createOutputNode() {
        DSTreeNode outputRoot = new DSTreeNode(OUTPUT_NAME, DSTreeNode.OUTPUT_ROOT);

        File[] outputDirs = explorer.getWorkspace().getOutputDataDirectories();
        for (File dir : outputDirs) {
            DSTreeNode outputDataDirNode = new DSTreeNode(dir.getName(), DSTreeNode.OUTPUT_DIR);
            for (File file : explorer.getWorkspace().getOutputDataFiles(dir)) {
                DSTreeNode outputDataStoreNode = new DSTreeNode(new FileObject(file), DSTreeNode.OUTPUT_DS);
                outputDataDirNode.add(outputDataStoreNode);
            }
            outputRoot.add(outputDataDirNode);
        }

//        List<String> outIDList = new ArrayList<String>(workspace.getOutputDataStoreIDs());
//        Collections.sort(outIDList);
//        for (String id : outIDList) {
//            DSTreeNode dsNode = new DSTreeNode(id, DSTreeNode.OUTPUT_DS);
//            outputRoot.add(dsNode);
//            //!!!take care of different result sets here!!!
//        }
        return outputRoot;
    }

    private boolean isNodeSuitableForEnsemble(DSTreeNode node){
        switch(node.getType()){

            case DSTreeNode.OUTPUT_DIR:
                return false;
            case DSTreeNode.INPUT_DS:
                return false;
            default:return true;
        }
    }

    private void addToEnsemble(){
        DSTreeNode node = (DSTreeNode) getLastSelectedPathComponent();
        //this shoulb be checked before calling that function, so that this message should never popup
        if (!isNodeSuitableForEnsemble(node)){
            JOptionPane.showMessageDialog(popupDS, "this element cannot be added to an ensemble");
        }
        optas.data.DataCollection collection = explorer.getDisplayManager().getCurrentDataCollection();
        if (collection==null){
            JOptionPane.showMessageDialog(popupDS, "there is no data collection selected");
        }
        FileObject fo = (FileObject) node.getUserObject();
        if (fo.getFile().getAbsolutePath().endsWith("cdat")){
            mergeEnsembles(fo, collection);
        }else{
            ImportMonteCarloDataPanel importer = new ImportMonteCarloDataPanel(this.explorer.getExplorerFrame(), collection, fo.getFile());
            JDialog dialog = importer.getDialog();
            dialog.setLocationRelativeTo(null);
            dialog.setModal(true);
            dialog.setVisible(true);
        }
    }

    private void mergeEnsembles(FileObject fo, DataCollection dc){
        DataCollection src = DataCollection.createFromFile(fo.getFile());
        if (src == null){
            JOptionPane.showMessageDialog(popupDS, "failed to load data collection file!");
            return;
        }
        Object[] values = new Object[]{"Attach collection", "Unify collection"};
        JOptionPane.showInputDialog(popupDS, "Please select merging mode!", "Merge mode selection", JOptionPane.QUESTION_MESSAGE, null, values, values[0] );
        dc.mergeDataCollections(src);
    }

    private void displayDSData() {
        explorer.getDisplayManager().displayDS((DSTreeNode) getLastSelectedPathComponent());
    }

    private void deleteDSFile(){
        explorer.getDisplayManager().deleteDS((DSTreeNode) getLastSelectedPathComponent());
    }
    private void deleteDSDir(){
        explorer.getDisplayManager().deleteDS((DSTreeNode) getLastSelectedPathComponent());
    }

    private void updatePopupMenu(DSTreeNode node){
        if (!this.isNodeSuitableForEnsemble(node)){
            addToEnsembleItem.setEnabled(false);
            return;
        }
        if (this.explorer.getDisplayManager().getCurrentDataCollection()==null){
            addToEnsembleItem.setEnabled(false);
            return;
        }
        addToEnsembleItem.setEnabled(true);
    }

    private void showPopup(MouseEvent evt) {
        TreePath p = this.getClosestPathForLocation(evt.getX(), evt.getY());
        this.setSelectionPath(p);
        DSTreeNode node = (DSTreeNode) this.getLastSelectedPathComponent();

        if ((node != null) && ((node.getType() == DSTreeNode.INPUT_DS) || (node.getType() == DSTreeNode.OUTPUT_DS))) {
            updatePopupMenu(node);
            if ((node != null) && ((node.getType() == DSTreeNode.INPUT_DS))) {
                InputDataStore store = explorer.getWorkspace().getInputDataStore(node.toString());
                updatePopupMenu(node);
                if (store instanceof J2KTSDataStore){
                    
                } else
                    popupDS.show(this, evt.getX(), evt.getY());
            }else
                popupDS.show(this, evt.getX(), evt.getY());
        }
        if ((node != null) && ((node.getType() == DSTreeNode.OUTPUT_DIR))) {
            updatePopupMenu(node);
            popupDir.show(this, evt.getX(), evt.getY());
        }
    }

    public void update() {
        this.setVisible(false);
        DSTreeNode root = createIOTree();
        this.setModel(new DefaultTreeModel(root));
        this.expandAll();
        this.setVisible(true);
    }

    private DSTreeNode createIOTree() {

        DSTreeNode root = new DSTreeNode(ROOT_NAME, DSTreeNode.IO_ROOT);
        DSTreeNode inputRoot = createInputNode();
        DSTreeNode outputRoot = createOutputNode();

        root.add(inputRoot);
        root.add(outputRoot);

        return root;
    }

    public void addObserver(Observer o) {
        nodeObservable.addObserver(o);
    }

    private class NodeObservable extends Observable {

        DSTreeNode node;

        public void setNode(DSTreeNode node) {
            this.node = node;
            this.setChanged();
            notifyObservers();
        }

        @Override
        public void notifyObservers(Object arg) {
            super.notifyObservers(node);
        }
    }
}
