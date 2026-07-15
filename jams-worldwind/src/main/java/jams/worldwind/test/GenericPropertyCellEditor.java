/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.test;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class GenericPropertyCellEditor implements TableCellEditor {

    private static final Logger logger = LoggerFactory.getLogger(GenericPropertyCellEditor.class);
    
    private JTextField textField = new JTextField();
    private PropertyEditor pe;
    private Object oValue;
    private List lListeners = new ArrayList();
    
    /**
     *
     * @param pe
     */
    public GenericPropertyCellEditor(PropertyEditor pe) {
        this.pe = pe;
        if (pe.supportsCustomEditor()) {
            pe.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    oValue = evt.getNewValue();
                    notifyListeners(true);
                }
            });
        } else {
            textField = new JTextField();
            textField.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    stopCellEditing();
                }
            });
        }
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        oValue = value;
        pe.setValue(value);
        
        if(pe.supportsCustomEditor()) {
            return pe.getCustomEditor();
        }
        
        textField.setText(pe.getAsText());
        return textField;
    }

    @Override
    public Object getCellEditorValue() {
        return oValue;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        boolean bSuccess = true;
        if(pe.supportsCustomEditor()) {
            oValue=pe.getValue();
        } else {
            try {
                pe.setAsText((textField.getText()));
                oValue=pe.getValue();
            } catch (Exception e) {
                bSuccess = false;
                logger.error("Bad value: " + textField.getText() + " Exception: " + e.toString());
                textField.requestFocus();
            }
        }
        notifyListeners(true);
        return bSuccess;
    }

    @Override
    public void cancelCellEditing() {
        notifyListeners(false);
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        lListeners.add(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        lListeners.remove(l);
    }
    
    private void notifyListeners(boolean bStop) {
        for(int i=0;i<lListeners.size();i++) {
            if(bStop) {
                ((CellEditorListener)lListeners.get(i)).editingStopped(null);
            } else
                ((CellEditorListener)lListeners.get(i)).editingCanceled(null);
        }
    }
    
}
