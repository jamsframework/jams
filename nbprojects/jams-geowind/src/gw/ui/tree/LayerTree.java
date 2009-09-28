/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.tree;

import gov.nasa.worldwind.layers.Layer;
import gw.api.Events;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author Ian Overgard
 */
public class LayerTree extends JTree {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public LayerTree() {
        final LayerTree tree = this;
        this.setCellRenderer(new LayerTreeNodeRenderer());
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int row = tree.getRowForLocation(x, y);
                TreePath path = tree.getPathForRow(row);
                if (path != null) {
                    if (path.getLastPathComponent() instanceof LayerTreeNode) {
                        LayerTreeNode node = (LayerTreeNode) path.getLastPathComponent();
                        Layer cb = node.getLayer();

                        if (e.getX() < tree.getRowBounds(row).x + 20) {
                            boolean isSelected = !(node.isSelected());
                            node.setSelected(isSelected);
                            pcs.firePropertyChange(Events.GF_SELECTION, null, cb);
                            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                            if (row == 0) {
                                tree.revalidate();
                                tree.repaint();
                            }
                        }
                    }
                }
            }
        });
    }

    public Layer getSelectedLayer() {
        if (this.getSelectionModel().isSelectionEmpty() || !(this.getSelectionModel().getSelectionPath().getLastPathComponent() instanceof LayerTreeNode)) {
            return null;
        }
        LayerTreeNode node = ((LayerTreeNode) this.getSelectionModel().getSelectionPath().getLastPathComponent());
        if (node.getUserObject() instanceof Layer) {
            return (Layer) node.getUserObject();
        }
        return null;
    }

    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    static class LayerTreeNodeRenderer implements TreeCellRenderer {

        DefaultTreeCellRenderer defTR = new DefaultTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            if (value instanceof LayerTreeNode) {
                return new CheckNodeComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
            } else if (value instanceof TableNode) {
                return new TableNodeComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
            } else {
                return defTR.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
            }
        }
    }
}
