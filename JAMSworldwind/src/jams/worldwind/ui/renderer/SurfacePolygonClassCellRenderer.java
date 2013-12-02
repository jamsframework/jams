package jams.worldwind.ui.renderer;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
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
