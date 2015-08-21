/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gw.layers.SimpleFeatureLayer;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import gw.api.Events;
import gw.ui.tree.AttributeTable;

public class CheckBoxList extends JList {
    
    // TODO add layer reorder and layer filtering

    static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    static Border noBorder = new EmptyBorder(0, 0, 0, 0);
    static int offs = new JCheckBox().getPreferredSize().width;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//
    public static class LayerIcons {

        String base = "/gw/resources/";
        final Icon layerIcon = new ImageIcon(getClass().getResource(base + "globe-layer.png"));
        final Icon shapeIcon = new ImageIcon(getClass().getResource(base + "shape-layer.png"));
        final Icon wmsIcon = new ImageIcon(getClass().getResource(base + "wms-layer.png"));
        final Icon controlIcon = new ImageIcon(getClass().getResource(base + "control-layer.png"));
        final Icon tableIcon = new ImageIcon(getClass().getResource(base + "table.png"));

        public Icon get(Object key) {
            if (key instanceof WMSTiledImageLayer) {
                return wmsIcon;
            } else if (key instanceof SimpleFeatureLayer) {
                return shapeIcon;
            } else if (key instanceof AttributeTable) {
                return tableIcon;
            } else if (key instanceof CompassLayer || key instanceof TerrainProfileLayer ||
                    key instanceof WorldMapLayer || key instanceof ScalebarLayer) {
                return controlIcon;
            } else {
                return layerIcon;
            }
        }
    };
    static LayerIcons icons = new LayerIcons();

    public CheckBoxList() {

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new ListCellRenderer() {

            JCheckBox r = new JCheckBox("");
            JPanel p = new JPanel(new BorderLayout(0, 0));
            JLabel lbl = new JLabel();
            Color selCol = new Color(216, 232, 255);

            {
                r.setFocusPainted(false);
                r.setBorderPainted(true);
                p.setBorder(noBorder);
                p.add(r, BorderLayout.WEST);
                p.add(lbl, BorderLayout.CENTER);

                lbl.setOpaque(true);
                p.setOpaque(true);
            }

            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Layer l = (Layer) value;
                r.setBackground(isSelected ? selCol : getBackground());
//                r.setBackground(isSelected ? getSelectionBackground() : getBackground());
//                r.setForeground(isSelected ? getSelectionForeground() : getForeground());
                r.setEnabled(isEnabled());
                r.setFont(getFont());
                r.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
                r.setSelected(l.isEnabled());

                lbl.setBackground(isSelected ? selCol : getBackground());
//                lbl.setBackground(isSelected ? getSelectionBackground() : getBackground());
//                lbl.setForeground(isSelected ? getSelectionForeground() : getForeground());
                lbl.setText(l.getName());
                lbl.setIcon(icons.get(l));

                return p;
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    Layer cb = (Layer) getModel().getElementAt(index);
                    if (e.getX() > getCellBounds(index, index).x + offs) {
                        if (e.getClickCount() == 2) {
                            pcs.firePropertyChange("ww_doubleclick", null, cb);
                        } else {
                            pcs.firePropertyChange(Events.GF_SELECTION, null, cb);
                        }
                        return;
                    }
                    cb.setEnabled(!cb.isEnabled());
                    pcs.firePropertyChange(Events.GF_SELECTION, null, cb);
                    repaint();
                }
            }
        });

    //           setCellRenderer(new ListCellRenderer() {
//            public Component getListCellRendererComponent(
//                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                JCheckBox checkbox = (JCheckBox) value;
//                checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
//                checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
//                checkbox.setEnabled(isEnabled());
//                checkbox.setFont(getFont());
//                checkbox.setFocusPainted(false);
//                checkbox.setBorderPainted(true);
//                checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
//                return checkbox;
//            }
//        });
//       
//        addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                int index = locationToIndex(e.getPoint());
//                if (index != -1) {
//                    if (e.getX() > getCellBounds(index, index).x + offs) {
//                        return;
//                    }
//                    JCheckBox cb = (JCheckBox) getModel().getElementAt(index);
//                    cb.setSelected(!cb.isSelected());
//                    repaint();
//                }
//            }
//        });
    }

    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
