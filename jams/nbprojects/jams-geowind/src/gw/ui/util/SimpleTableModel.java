/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author hbusch
 */
public class SimpleTableModel extends DefaultTableModel {

    @Override
    public Class<?> getColumnClass(int arg0) {

        if (getRowCount() > 0) {
            Object cellValue = getValueAt(0, arg0);
            if (cellValue != null) {
                return cellValue.getClass();
            }
        }
        return Object.class;
    }
}
