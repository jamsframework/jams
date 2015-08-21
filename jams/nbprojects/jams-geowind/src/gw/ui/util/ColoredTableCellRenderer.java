/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import gw.util.ColorBlend;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Ian
 */
public class ColoredTableCellRenderer extends DefaultTableCellRenderer {

    static int HUE = 0;
    static int SATURATION = 1;
    static int BRIGHTNESS = 2;
    float[] hsb = new float[3];

    public ColoredTableCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        comp.setEnabled(table == null || table.isEnabled()); // see question above

        ProxyTableModel tm = (ProxyTableModel) table.getModel();
        Color bg = Color.white;
        if (column == tm.getHighlightedColumn()) {
            bg = tm.getColorForRowAndColumn(row, column);
        } else {
            //bg = tm.getColorsForColumn(column).getB();   // all columns get max-colour?
        }
      
        Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), hsb);

        if (column == table.getSelectedColumn() && table.isRowSelected(row)) {
            this.setBorder(BorderFactory.createLineBorder(Color.RED));
        }

        if (hsb[BRIGHTNESS] > 0.75f) {
            comp.setForeground(Color.BLACK);
        } else {
            comp.setForeground(Color.WHITE);
        }
        if (table.isRowSelected(row) && column != tm.getHighlightedColumn()) {
            bg = ColorBlend.mixColors(bg, Color.BLACK, 0.25f);
        }

        if (table.isRowSelected(row)) {
            comp.setFont(this.getFont().deriveFont(Font.BOLD));
        }
        comp.setBackground(bg);
        return comp;
    }
}
