package jams.worldwind.test;

import gov.nasa.worldwind.render.Material;
import jams.worldwind.handler.MaterialClassCellEditor;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class PropertyEditorCellEditor extends AbstractCellEditor implements TableCellEditor {

    private TableCellEditor editor;

    @Override
    public Object getCellEditorValue() {
        return editor.getCellEditorValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //System.out.println("ROW: " + row + " COLUMN: " + column + " TYPE: " + value.getClass().getName());
        if (value instanceof Material) {
            editor = new MaterialClassCellEditor();
        } else if (value instanceof Double) {
            editor = new DefaultCellEditor(new JFormattedTextField(new DecimalFormat("#.##")));
        } else if (value instanceof Boolean) {
            editor = new DefaultCellEditor(new JCheckBox());
        }
        else {
            editor = new DefaultCellEditor(new JTextField());
        }
        
        return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
