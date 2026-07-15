package jams.worldwind.test;

import gov.nasa.worldwind.render.Material;
import jams.worldwind.ui.renderer.MaterialClassCellRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class PropertyEditorCellRenderer extends JLabel implements TableCellRenderer {

    private Color colorSelected = new Color(200, 255, 200);
    private Color colorFocus = new Color(255, 200, 200);
    private Color colorNormal = new Color(200, 200, 255);
    //TableCellRenderer renderer;

    /**
     *
     */
    public PropertyEditorCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
/*
        // die normalen Farben
        setForeground(Color.BLACK);
        if (hasFocus) {
            setBackground(colorFocus);
        } else if (isSelected) {
            setBackground(colorSelected);
        } else {
            setBackground(colorNormal);
        }
*/
        setText(null);
        setIcon(null);

        if (value instanceof Short) {
            setText(((Short) value).toString());
        }
        if (value instanceof Integer) {
            setText(((Integer) value).toString());
        }
        if (value instanceof Long) {
            setText(((Long) value).toString());
        }
        if (value instanceof Float) {
            setText(((Float) value).toString());
        }
        if (value instanceof Double) {
            setText(((Double) value).toString());
        } else if (value instanceof Icon) {
            setIcon((Icon) value);
        } else if (value instanceof Color) {
            Color color = (Color) value;
            setForeground(color);
            setText(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        } else if (value instanceof Boolean) {
            if (((Boolean) value).booleanValue()) {
                setText("true");
            } else {
                setText("false");
            }
        } else if (value instanceof Material) {
            return new MaterialClassCellRenderer(true).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value == null) {
            setText("not defined");
        } else {
            setText(value.toString());
        }

        return this;
        /*
         if (value instanceof Material) {
         renderer = new MaterialClassCellRenderer(true);
         } else {
         renderer = new DefaultTableCellRenderer();
         }
         return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         */
    }
}
