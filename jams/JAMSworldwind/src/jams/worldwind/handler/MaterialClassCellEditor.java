/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.handler;

import gov.nasa.worldwind.render.Material;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class MaterialClassCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    Material currentMaterial;
    Color currentColor;
    JButton button;
    JColorChooser colorChooser;
    JDialog dialog;

    /**
     *
     */
    protected static final String EDIT = "edit";

    /**
     *
     */
    public MaterialClassCellEditor() {
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button,
                                        "Pick a Color",
                                        true,  //modal
                                        colorChooser,
                                        this,  //OK button handler
                                        null); //no CANCEL button handler
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
            button.setBackground(currentColor);
            colorChooser.setColor(currentColor);
            dialog.setVisible(true);

            //Make the renderer reappear.
            fireEditingStopped();

        } else { //User pressed dialog's "OK" button.
            currentColor = colorChooser.getColor();
            currentMaterial = new Material(currentColor);
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override
    public Object getCellEditorValue() {
        return currentMaterial;
    }

    //Implement the one method defined by TableCellEditor.
    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        //currentColor = (Color)value;
        currentMaterial = (Material)value;
        currentColor = currentMaterial.getDiffuse();
        
        return button;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

}
