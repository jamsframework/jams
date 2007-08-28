/*
 * SortableTableModel.java
 *
 * Created on 21. November 2005, 12:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jamschartfactory.tableView;

import javax.swing.table.*;

/**
 *
 * @author c0krpe
 */
public class SortableTableModel extends DefaultTableModel {
    
    int[] indexes;
    TableSorter sorter;
    String [] dataTypes;
    boolean [] editable;
    public SortableTableModel(String[] dataTypes, boolean[] editable) {
        this.dataTypes = dataTypes;
        this.editable = editable;
    }
    
    public String getDataTypes(int col){
        return this.dataTypes[col];
    }
    
    public Object getValueAt(int row, int col) {
        int rowIndex = row;
        if (indexes != null) {
            rowIndex = indexes[row];
        }
        return super.getValueAt(rowIndex, col);
    }
    
    public boolean isCellEditable(int row, int col){
        return editable[col];
    }
    
    public void setValueAt(Object obj, int row, int col) {
        //translate coordinates
        int rowIndex = row;
        if(indexes != null){
            rowIndex = indexes[row];
        }
        if(dataTypes[col].equals("s"))
            super.setValueAt(obj, rowIndex, col);
        else if(dataTypes[col].equals("i")){
            try{
                super.setValueAt(new Integer(obj.toString()), rowIndex, col);
                this.fireTableDataChanged();
            }catch(java.lang.NumberFormatException e){
                System.err.println("An integer is required here!");
            }
        } else if(dataTypes[col].equals("d")){
            super.setValueAt(obj, rowIndex, col);//new Double(objs), rowIndex, col);
            this.fireTableDataChanged();
        } else
            System.err.println("Undefined type for column " + col);
    }
    
    
    public void sortByColumn(int column, boolean isAscent, String type) {
        if (sorter == null) {
            sorter = new TableSorter(this);
        }
        sorter.sort(column, isAscent, type);
        //fireTableDataChanged();
    }
    
    public Class getColumnClass(int col, String type) {
        if(type.equals("s"))
            return String.class;
        else if(type.equals("i"))
            return Integer.class;
        else if(type.equals("d"))
            return Double.class;
        else
            return null;
    }
    
    public int[] getIndexes() {
        int n = getRowCount();
        if (indexes != null) {
            if (indexes.length == n) {
                return indexes;
            }
        }
        indexes = new int[n];
        for (int i=0; i<n; i++) {
            indexes[i] = i;
        }
        return indexes;
    }
    
}
