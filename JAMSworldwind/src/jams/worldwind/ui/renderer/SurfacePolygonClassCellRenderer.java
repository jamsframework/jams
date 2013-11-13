/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.worldwind.ui.renderer;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author bigr
 */
public class SurfacePolygonClassCellRenderer implements TableCellRenderer{

    private final JButton theButton;
    private final String buttonText = "...";
    
    public SurfacePolygonClassCellRenderer() {
        this.theButton = new JButton(this.buttonText);
    }
        
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this.theButton;
    }
    
}
