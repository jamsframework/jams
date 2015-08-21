/*
 * Layerpanel.java
 *
 * Created on October 20, 2008, 10:21 AM
 */
package gw.ui;

import java.awt.event.MouseEvent;
import gw.ui.layerproperies.ProfileLayerProperties;
import gw.ui.layerproperies.WMSLayerProperties;
import gw.ui.layerproperies.ShapefileLayerProperties;
import gw.ui.layerproperies.DefaultLayerProperties;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gw.api.Events;
import gw.layers.GridCoverageLayer;
import gw.layers.SimpleFeatureLayer;
import gw.ui.layerproperies.AttributeTableProperties;
import gw.ui.layerproperies.GridLayerProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import gw.ui.layerproperies.ScaleLayerProperties;
import gw.ui.tree.AttributeTable;
import gw.ui.tree.LayerTree;
import gw.ui.tree.LayerTreeNode;
import gw.ui.tree.TableNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author  od
 */
public class LayerControl extends javax.swing.JPanel {

    ResourceBundle bundle = ResourceBundle.getBundle("gw/resources/language");
    CardLayout layerProps = new CardLayout();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Layer"));
    DefaultTreeModel model = new DefaultTreeModel(root);
    //CheckBoxList cbl = new CheckBoxList();
    LayerTree tree = new LayerTree();
    HashMap<String, JPanel> panels = new HashMap<String, JPanel>();
//    
    Action removeLayerAction = new AbstractAction() {

        {
            putValue(NAME, java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Remove"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Layer layer = tree.getSelectedLayer();//(Layer) cbl.getSelectedValue();
            if (layer != null) {
                LayerControl.this.firePropertyChange("gf_remove_layer", layer, null);
            }
        }
    };
    Action flytoLayerAction = new AbstractAction() {

        {
            putValue(NAME, java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Fly_to_it"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Layer layer = tree.getSelectedLayer();
            if (layer != null) {
                LayerControl.this.firePropertyChange("ww_flyto", null, layer);
            }
        }
    };

    public LayerControl(PropertyChangeListener l) {
        initComponents();
        setupComponents(l);
        addPropertyChangeListener(l);
        tree.addListener(l);
    }

    void removeLayer(Layer l, PropertyChangeListener updater) {

        //Remove tree node
        for (int i = 0; i < root.getChildCount(); i++) {
            if (((LayerTreeNode) root.getChildAt(i)).getLayer() == l) {
                root.remove(i);
                break;
            }
        }

        //Remove panel and property change listeners
        JPanel p = panels.get(l.getName());
        p.removePropertyChangeListener(updater);
        jPanel2.remove(p);

        //Reload the tree model
        model.reload();
    }

    private void setupComponents(final PropertyChangeListener l) {

        final JPopupMenu popup = new JPopupMenu();
        final LayerControl lc = this;
        popup.add(removeLayerAction);
        popup.add(flytoLayerAction);

        tree.setDropTarget(new DropTarget(tree, new DropTargetAdapter() {

            @Override
            public void dragEnter(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            public void dragOver(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent evt) {
                try {
                    Transferable tr = evt.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : fileList) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForLocation((int) evt.getLocation().getX(),
                                    (int) evt.getLocation().getY()).getLastPathComponent();
                            if (file.getName().endsWith(".csd") && node.getUserObject() instanceof SimpleFeatureLayer) {
                                lc.addAttributeTable((LayerTreeNode) node, file);
                            }
                        }
                        //addAttrData(fileList);
                        evt.getDropTargetContext().dropComplete(true);
                    } else {
                        evt.rejectDrop();
                    }
                } catch (Exception io) {
                    io.printStackTrace();
                    evt.rejectDrop();
                }
            }
        }));

        tree.setRowHeight(18);
//        tree.setRootVisible(false);
        tree.setModel(model);
        tree.addPropertyChangeListener(l);
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                LayerTree tree = (LayerTree) e.getSource();
                TitledBorder b = (TitledBorder) jPanel2.getBorder();
                if (tree.getSelectionPath() != null) {

                    Object ob = tree.getSelectionPath().getLastPathComponent();
                    if (ob instanceof LayerTreeNode) {
                        LayerTreeNode o = (LayerTreeNode) ob;
                        Layer l = o.getLayer();
                        LayerControl.this.firePropertyChange(Events.GF_SELECTION, null, l);
                    }
                    String t = tree.getSelectionPath().getLastPathComponent().toString();
                    layerProps.show(jPanel2, t);
                    b.setTitle(" " + t + " - " + bundle.getString("L_LAYER_PROPS"));
                } else {
                    layerProps.show(jPanel2, bundle.getString("L_EMPTY"));
                    b.setTitle(bundle.getString("L_LAYER_PROPS"));
                }
                jPanel2.repaint();
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            private void doPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    tree.setSelectionRow(tree.getRowForLocation(e.getX(), e.getY()));
                    Layer layer = (Layer) tree.getSelectedLayer();
                    removeLayerAction.setEnabled(layer != null);
                    if (layer != null) {
                        removeLayerAction.putValue(Action.NAME, java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Remove") + " '" + layer.getName() + "'");
                    } else {
                        removeLayerAction.putValue(Action.NAME, java.util.ResourceBundle.getBundle("gw/resources/language").getString("L_Remove"));
                    }

                    flytoLayerAction.setEnabled((layer instanceof SimpleFeatureLayer) || (layer instanceof GridCoverageLayer));
                    popup.show(tree, e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                doPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                doPopup(e);
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path.getLastPathComponent() instanceof TableNode) {
                        TableNode node = (TableNode) path.getLastPathComponent();
                        LayerTreeNode l = (LayerTreeNode) node.getParent();

//                        layerProps.show(jPanel2, node.toString());
                        LayerControl.this.firePropertyChange("ww_doubleclick", null, l.getLayer());
//                        l.propertyChange(new PropertyChangeEvent(tree, "ww_doubleclick", node, node));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                doPopup(e);
            }
        });

        jPanel2.setLayout(layerProps);
        jPanel2.add(new JPanel(), "empty");
        jPanel1.add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    public void addAttributeTable(LayerTreeNode node, File attrFile) {
        node.add(new TableNode(new AttributeTable(attrFile)));
        ShapefileLayerProperties layerProp = (ShapefileLayerProperties) this.panels.get(node.getUserObject().toString());
        model.reload(node);
        int index = node.getChildCount() - 1;
        JPanel panel = new AttributeTableProperties(attrFile, (SimpleFeatureLayer) node.getLayer(), layerProp.getTableModel(), index);
        jPanel2.add(panel, attrFile.getName());
        panels.put(attrFile.getName(), panel);
    }

    public JPanel addLayer(Layer layer, PropertyChangeListener updater, WorldWindowGLJPanel ww) {
        LayerTreeNode node = new LayerTreeNode(layer);
        root.insert(node, 0);
        model.reload();
        JPanel p = null;
        if (layer instanceof TerrainProfileLayer) {
            // profile layer
            p = new ProfileLayerProperties((TerrainProfileLayer) layer, (OrbitView) ww.getView());
        } else if (layer instanceof SimpleFeatureLayer) {
            // shape file layer
            p = new ShapefileLayerProperties((SimpleFeatureLayer) layer, ww);
            File attrFile = new File(((SimpleFeatureLayer) layer).getName().replaceAll(".shp", ".dbf"));
            node.add(new TableNode(new AttributeTable(attrFile)));
            int index = node.getChildCount() - 1;
            ShapefileLayerProperties layerProp = (ShapefileLayerProperties) p;
            JPanel panel = new AttributeTableProperties(attrFile, (SimpleFeatureLayer) node.getLayer(), layerProp.getTableModel(), index);
            jPanel2.add(panel, attrFile.getName());
            panels.put(attrFile.getName(), panel);
        } else if (layer instanceof GridCoverageLayer) {
            p = new GridLayerProperties((GridCoverageLayer) layer);
        } else if (layer instanceof WMSTiledImageLayer) {
            p = new WMSLayerProperties((WMSTiledImageLayer) layer);
        } else if (layer instanceof ScalebarLayer) {
            p = new ScaleLayerProperties((ScalebarLayer) layer);
        } else {
            // internel layer
            p = new DefaultLayerProperties(layer);
        }
        p.addPropertyChangeListener(updater);
        jPanel2.add(p, layer.getName());
        panels.put(layer.getName(), p);
        return p;
    }

    /**
     * get the properties of a certain layer
     * @param layerName
     * @return panel of layer
     */
    public JPanel getLayerPanel(String layerName) {
        return panels.get(layerName);
    }

    public Set<String> getPanels() {
        return panels.keySet();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " " + bundle.getString("L_LayerList") + " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " " + bundle.getString("L_LAYER_PROPS") + " ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BOTTOM));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
