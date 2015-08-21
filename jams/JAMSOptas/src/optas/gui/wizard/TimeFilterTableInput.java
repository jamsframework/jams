/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMS;
import jams.gui.input.TableInput;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.Object;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import optas.data.TimeFilter;
import optas.data.TimeFilterCollection;
import optas.data.TimeSerie;

/**
 *
 * @author christian
 */
class TimeFilterTableInput extends TableInput {
        TimeSerie timeserie;
        TimeFilterDialog tfd = null;
        ArrayList<TimeFilterTableInputListener> listeners = new ArrayList<TimeFilterTableInputListener>();
        ArrayList<Integer> selectedRows = new ArrayList<Integer>();
        
        static abstract public class TimeFilterTableInputListener{
            int event;
            
            abstract public void tableChanged(TimeFilterTableInput tfti);
            abstract public void itemChanged(TimeFilterTableInput tfti);
        }
                
        public class AndOrRenderer extends JButton
            implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public AndOrRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object andor,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Boolean newValue = (Boolean)andor;
            this.setMargin(new Insets(1, 1, 1, 1));
            if (newValue)
                setText("AND");
            else
                setText("OR");
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            return this;
        }
    }
        public TimeFilterTableInput(TimeSerie timeserie) {
            super(new String[]{JAMS.i18n("enabled"),JAMS.i18n("inverted"), JAMS.i18n("log_operation"), JAMS.i18n("description")}, new Class[]{Boolean.class,Boolean.class,Boolean.class, String.class}, new boolean[]{true, true, true, false}, true);

            this.timeserie = timeserie;
            
            getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            TableColumn col;
            int width = 75;
            col = getTable().getColumnModel().getColumn(0);
            col.setPreferredWidth(width);
            col = getTable().getColumnModel().getColumn(1);
            col.setPreferredWidth(width);
            col = getTable().getColumnModel().getColumn(2);
            col.setPreferredWidth(width);
            col.setCellRenderer(new AndOrRenderer(true));
            col = getTable().getColumnModel().getColumn(3);
            col.setPreferredWidth(4*width);
            
            getTable().setRowHeight(25);
            
            ((AbstractTableModel) this.getTable().getModel()).addTableModelListener(new TableModelListener() {

                @Override
                public void tableChanged(TableModelEvent e) {
                    for (Object row[] : tableData.getValue()) {
                        if (row[0] instanceof Boolean) {
                            ((TimeFilter) row[3]).setEnabled((Boolean) row[0]);
                        }
                        if (row[1] instanceof Boolean) {
                            ((TimeFilter) row[3]).setInverted((Boolean) row[1]);
                        }
                        if (row[2] instanceof Boolean) {
                            ((TimeFilter) row[3]).setAdditive((Boolean) row[2]);
                        }
                    }
                    for (TimeFilterTableInputListener tftiListener : listeners){
                        tftiListener.itemChanged(TimeFilterTableInput.this); //1
                    }
                }
            });

            setPreferredSize(new Dimension(375, 200));
            
            if (timeserie==null){
                this.addButton.setEnabled(false);
                this.editButton.setEnabled(false);
            }
        }

        public void addChangeListener(TimeFilterTableInputListener tftiListener){
            this.listeners.add(tftiListener);
        }
        public void removeChangeListener(TimeFilterTableInputListener tftiListener){
            this.listeners.remove(tftiListener);
        }
              
        public void clear(){
            this.tableData.setValue(new ArrayList<Object[]>());
        }
                                                
        @Override
        protected void editItem() {
            //get the current selection
            int selection = getTable().getSelectedRow();
            if (selection == -1)
                return;
            Object selectedData = tableData.getElementAt(selection)[3];

            if (selectedData instanceof TimeFilter){                
                tfd = new TimeFilterDialog(timeserie);
                tfd.init(timeserie, (TimeFilter)selectedData);
                tfd.setModal(true);
                tfd.setVisible(true);
                
                if (tfd.getApproval()){
                    TimeFilter filter = tfd.getFilter();
                    tableData.setElementAt(selection, new Object[]{new Boolean(filter.isEnabled()), new Boolean(filter.isInverted()), new Boolean(filter.isAdditive()), filter});
                    for (TimeFilterTableInputListener tftiListener : listeners) {
                        tftiListener.tableChanged(this);
                    }
                }
            }
        }

        @Override
        protected void removeItem(){
            super.removeItem();
            for (TimeFilterTableInputListener tftiListener : listeners) {
                tftiListener.tableChanged(this);
            }
        }
        
        // add this item to the list and refresh
        private void addItem(TimeFilter filter) {
        if (filter != null) {
            this.tableData.addElement(new Object[]{new Boolean(filter.isEnabled()), new Boolean(filter.isInverted()), new Boolean(filter.isAdditive()), filter});
            this.setTableData(tableData.getValue());
            scrollPane.revalidate();
            scrollPane.repaint();
        }
        for (TimeFilterTableInputListener tftiListener : listeners) {
            tftiListener.tableChanged(this);
        }
    }
                
        @Override
        protected void addItem() {            
            tfd = new TimeFilterDialog(timeserie);//1,2,3
            tfd.setVisible(true);
            if (tfd.getApproval()){                
                TimeFilter filter = tfd.getFilter();                
                addItem(filter);
            }
        }
        
        public TimeFilterCollection getTimeFilters(){
            TimeFilterCollection timeFilters = new TimeFilterCollection();            
            for (Object[] o : getTableData()) {
                timeFilters.add((TimeFilter) o[3]);
            }
            return timeFilters;
        }
        
        public void setSelectedItem(TimeFilter selection){
            if (selection==null){
                getTable().getSelectionModel().clearSelection();
                return;
            }
            int i=0;
            for (Object[] o : getTableData()) {
                if (selection.equals((TimeFilter) o[3])){
                    getTable().getSelectionModel().addSelectionInterval(i, i);                    
                }
                i++;
            }
        }
        
        public void setTimeFilters(TimeFilterCollection timeFilters){
            this.clear();
            
            for (TimeFilter filter : timeFilters.get()){
                this.tableData.addElement(new Object[]{new Boolean(filter.isEnabled()), new Boolean(filter.isInverted()), new Boolean(filter.isAdditive()), filter});                
            }
            this.setTableData(tableData.getValue());    
            scrollPane.revalidate();
            scrollPane.repaint();
                                
            for (TimeFilterTableInputListener tftiListener : listeners) {
                tftiListener.tableChanged(this);
            }
        }
        
        public void setTimeSeries(TimeSerie timeserie){
            this.timeserie = timeserie;
        }        
    }
