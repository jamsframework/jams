/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.ui.tree;

import gw.ui.util.CheckBoxList.LayerIcons;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 *
 * @author Ian Overgard
 */
public class TableNodeComponent extends JPanel {

    protected TreeLabel label;
    static LayerIcons icons = new LayerIcons();
    static Color selCol = new Color(216, 232, 255);

    public TableNodeComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {

        setLayout(null);
        add(label = new TreeLabel());
        label.setForeground(UIManager.getColor("Tree.textForeground"));

        setEnabled(tree.isEnabled());

        label.setFont(tree.getFont());
        label.setText(((TableNode) value).toString());
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
        label.setIcon(icons.get(((TableNode) value).getUserObject()));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d_label = label.getPreferredSize();
        return new Dimension(d_label.width, d_label.height);
    }

    @Override
    public void doLayout() {
        Dimension d_label = label.getPreferredSize();
        int y_label = 0;
        label.setLocation(0, y_label);
        label.setBounds(0, y_label, d_label.width, d_label.height);
    }

    @Override
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource) {
            color = null;
        }
        super.setBackground(color);
    }

    public class TreeLabel extends JLabel {

        boolean isSelected;
        boolean hasFocus;

        public TreeLabel() {
        }

        @Override
        public void setBackground(Color color) {
            if (color instanceof ColorUIResource) {
                color = null;
            }
            super.setBackground(color);
        }

        @Override
        public void paint(Graphics g) {
            String str;
            if ((str = getText()) != null) {
                if (0 < str.length()) {
                    if (isSelected) {
//                        g.setColor(UIManager.getColor("Tree.selectionBackground"));
                        g.setColor(selCol);
                    } else {
                        g.setColor(UIManager.getColor("Tree.textBackground"));
                    }
                    Dimension d = getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();
                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
                    }
                    g.fillRect(imageOffset, 0, d.width - 1 - imageOffset, d.height);
//                    if (hasFocus) {
//                        g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
//                        g.drawRect(imageOffset, 0, d.width - 1 - imageOffset, d.height - 1);
//                    }
                }
            }
            super.paint(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();
            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3, retDimension.height);
            }
            return retDimension;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}
