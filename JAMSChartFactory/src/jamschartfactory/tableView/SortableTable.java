/*
 * SortableTable.java
 *
 * Created on 21. November 2005, 12:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jamschartfactory.tableView;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author c0krpe
 */
public class SortableTable extends JPanel{
    String[] headerStr;
    String[] dataTypes;
    boolean[] editable;
    JTable theTable;
    public int tableHeight;
    
    /** Creates a new instance of SortableTable */
    public SortableTable(String[] headers, String[] dataTypes, boolean[] editable, Object[][] data) {
        this.headerStr = headers;
        this.dataTypes = dataTypes;
        this.editable = editable;
        
        //layout
        setLayout(new BorderLayout());
        
        int[] columnWidth = new int[headerStr.length];
        for(int i = 0; i < headerStr.length; i++){
            columnWidth[i] = 80;
        }
        
        SortableTableModel tabModel = new SortableTableModel(this.dataTypes, this.editable);
        tabModel.setDataVector(data, this.headerStr);
        
        theTable = new JTable(tabModel);
        theTable.setCellSelectionEnabled(true);
        theTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        theTable.setShowGrid(false);
        theTable.setShowVerticalLines(true);
        theTable.setShowHorizontalLines(true);
        theTable.setAutoResizeMode(theTable.AUTO_RESIZE_OFF);
        SortButtonRenderer renderer = new SortButtonRenderer();
        TableColumnModel model = theTable.getColumnModel();
        int n = headerStr.length;
        for (int i=0;i<n;i++) {
            model.getColumn(i).setHeaderRenderer(renderer);
            model.getColumn(i).setMinWidth(columnWidth[i]);
        }
        
        JTableHeader header = theTable.getTableHeader();
        header.addMouseListener(new HeaderListener(header,renderer));
        JScrollPane pane = new JScrollPane(theTable);
        add(pane, BorderLayout.CENTER);
        CalcPanel calcPanel = new CalcPanel(theTable);
        add(calcPanel, BorderLayout.SOUTH);
        
        int paneHeight = pane.getSize().height;
        int rowHeight = theTable.getRowHeight();
        int rows = theTable.getRowCount();
        int headerHeight = renderer.height;
        int scrollBarHeight = pane.getHorizontalScrollBar().getHeight();
        this.tableHeight = (scrollBarHeight + headerHeight + (rows * rowHeight) + (rows - 1) * theTable.getRowMargin());
        
    }
    
    /*public void setValueAt(Object obj, int row, int col) {
        java.text.DecimalFormat dobf = new java.text.DecimalFormat("#.####");
        
        if(dataTypes[col].equals("s"))
            theTable.setValueAt(obj, row, col);
        else if(dataTypes[col].equals("i")){
            try{
                theTable.setValueAt(new Integer(obj.toString()), row, col);
                ((javax.swing.table.DefaultTableModel)theTable.getModel()).fireTableDataChanged();
            }catch(java.lang.NumberFormatException e){
                System.err.println("An integer is required here!");
            }
        } else if(dataTypes[col].equals("d")){
            String objs = dobf.format(obj);
            theTable.setValueAt(new Double(objs), row, col);
            ((javax.swing.table.DefaultTableModel)theTable.getModel()).fireTableDataChanged();
        } else
            System.err.println("Undefined type for column " + col);
    }*/
}
class HeaderListener extends MouseAdapter {
    JTableHeader   header;
    SortButtonRenderer renderer;
    
    HeaderListener(JTableHeader header,SortButtonRenderer renderer) {
        this.header   = header;
        this.renderer = renderer;
        //header.getTable().getModel()
    }
    
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == e.BUTTON3){
            int col = header.columnAtPoint(e.getPoint());
            int sortCol = header.getTable().convertColumnIndexToModel(col);
            renderer.setPressedColumn(col);
            renderer.setSelectedColumn(col);
            header.repaint();
            
            if (header.getTable().isEditing()) {
                header.getTable().getCellEditor().stopCellEditing();
            }
            
            boolean isAscent;
            if (SortButtonRenderer.DOWN == renderer.getState(col)) {
                isAscent = true;
            } else {
                isAscent = false;
            }
            
            ((SortableTableModel)header.getTable().getModel()).sortByColumn(sortCol, isAscent, ((SortableTableModel)header.getTable().getModel()).getDataTypes(sortCol));
        }
        else if(e.getButton() == e.BUTTON1 && e.getClickCount() == 2){
            int col = header.columnAtPoint(e.getPoint());
            int rows = header.getTable().getRowCount() - 1;
            header.getTable().getVisibleRect();
            header.getTable().changeSelection(rows, col, false, false);
            header.getTable().changeSelection(0, col, false, true);
            
            //header.getTable().setSColumnSelectionInterval(col, col);
            //header.getTable().setRowSelectionInterval(0, rows-1);
            
        }
    }
    
}
