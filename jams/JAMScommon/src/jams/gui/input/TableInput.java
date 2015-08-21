/*
 * ListInput.java
 * Created on 11. April 2006, 20:46
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.gui.input;

import jams.gui.tools.GUIHelper;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jams.JAMS;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author S. Kralisch
 */
public class TableInput extends JPanel {

    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    static final int BUTTON_SIZE = 20;
    private static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
    private JTable table;
    protected JButton addButton, removeButton, upButton, downButton, editButton;
    protected JScrollPane scrollPane;
    protected TableData tableData = new TableData();
    protected MouseListener editListener;
    
    private String[] header = null;
    private Class[] columnClasses = null;
    private boolean[] editable = null;

    public TableInput(String header[], Class columnClasses[],boolean editable[]) {
        this(header, columnClasses, editable, true);
    }

    public TableInput(String header[], Class columnClasses[],boolean editable[], boolean orderButtons) {
        this(header, columnClasses, editable, orderButtons, true);
    }

    public TableInput(String header[], Class columnClasses[],boolean editable[], boolean orderButtons, boolean editButtons) {
        this.header = header;
        this.columnClasses = columnClasses;
        this.editable = editable;
        // create a panel to hold all other components
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);

        // create a new listbox control
        table = new JTable(new AbstractTableModel() {

            public int getRowCount() {
                return tableData.getRowCount();
            }

            @Override
            public String getColumnName(int col) {
                return TableInput.this.header[col];
            }

            @Override
            public Class getColumnClass(int c) {
                return TableInput.this.columnClasses[c];
            }

            public int getColumnCount() {
                return TableInput.this.header.length;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return tableData.getElementAt(rowIndex)[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int col) {
               return TableInput.this.editable[col];
            }

            public void setValueAt(Object value, int row, int col) {
                tableData.getElementAt(row)[col] = value;
                fireTableCellUpdated(row, col);
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        editListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    editItem();
                }
            }           
        };
        table.addMouseListener(editListener);

        // add the listbox to a scrolling pane
        scrollPane = new JScrollPane();
        scrollPane.getViewport().add(getTable());
        add(scrollPane, BorderLayout.CENTER);

        // create a panel to hold all other components
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        buttonPanel.setLayout(new FlowLayout());
        add(buttonPanel, BorderLayout.EAST);

        // create some function buttons
        addButton = new JButton("+");
        addButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        addButton.setPreferredSize(BUTTON_DIMENSION);
        buttonPanel.add(addButton);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                addItem(); //1,2,3
            }
        });

        removeButton = new JButton("-");
        removeButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        removeButton.setPreferredSize(BUTTON_DIMENSION);
        buttonPanel.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                removeItem();
            }
        });

        if (editButtons) {

            editButton = new JButton("...");
            editButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
            editButton.setPreferredSize(BUTTON_DIMENSION);
            editButton.setToolTipText(JAMS.i18n("Edit"));
            buttonPanel.add(editButton);
            editButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        }

        if (orderButtons) {
            upButton = new JButton();
            upButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
            upButton.setPreferredSize(BUTTON_DIMENSION);
            upButton.setToolTipText(JAMS.i18n("Move_up"));
            upButton.setIcon(UP_ICON);
            upButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            });

            downButton = new JButton();
            downButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
            downButton.setPreferredSize(BUTTON_DIMENSION);
            downButton.setToolTipText(JAMS.i18n("Move_down"));
            downButton.setIcon(DOWN_ICON);
            downButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            });

            buttonPanel.add(upButton);
            buttonPanel.add(downButton);
        }
    }

    private void swapRows(int r1, int r2){
            TableModel model = this.table.getModel();

            for (int i=0;i<model.getColumnCount();i++){
                Object o1 = model.getValueAt(r1, i);
                Object o2 = model.getValueAt(r2, i);

                model.setValueAt(o2, r1, i);
                model.setValueAt(o1, r2, i);
            }
        }

        private void moveUp(){
            int row = table.getSelectedRow();
            if (row != 0){
                swapRows(row, row-1);
            }

        }

        private void moveDown(){
            int row = table.getSelectedRow();
            if (row != table.getRowCount()-1){
                swapRows(row, row+1);
            }

        }

    public void addListDataObserver(Observer obs) {
        tableData.addObserver(obs);
    }

    public void setTableData(ArrayList<Object[]> listData) {
        tableData.setValue(listData);
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public void revalidateScroll() {
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public ArrayList<Object[]> getTableData() {
        return tableData.getValue();
    }

    public Object[] getSelectedString() {
        int selection = getTable().getSelectedRow();
        if (selection >= 0) {
            return tableData.getValue().get(selection);
        } else {
            return null;
        }
    }

    public JTable getTable() {
        return this.table;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getTable().setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        if (editButton != null) {
            editButton.setEnabled(enabled);
        }
        if (upButton != null) {
            upButton.setEnabled(enabled);
        }
        if (downButton != null) {
            downButton.setEnabled(enabled);
        }
    }

    protected void addItem() {
        // Get the text field value
        /*String stringValue = GUIHelper.showInputDlg(TableInput.this, null, JAMS.i18n("New_value"), null);

        // add this item to the list and refresh
        if (stringValue != null && !tableData.getValue().contains(stringValue)) {
            stringValue.addElement(stringValue);
            scrollPane.revalidate();
            scrollPane.repaint();
        }*/
    }

    protected void removeItem() {
        //get the current selection
        int selection = getTable().getSelectedRow();
        if (selection >= 0) {
            // remove this item from the list and refresh
            tableData.removeElementAt(selection);

            scrollPane.revalidate();
            scrollPane.repaint();

            //select the next item
            if (selection >= tableData.getValue().size()) {
                selection = tableData.getValue().size() - 1;
            }
            if (selection == -1)
                getTable().getSelectionModel().clearSelection();
            else
                getTable().setRowSelectionInterval(selection, selection);
        }
    }

    protected void editItem() {
        //get the current selection
        /*int selection = getListbox().getSelectedIndex();
        if (selection >= 0) {
            // edit this item
            Object value = tableData.getElementAt(selection);
            value = GUIHelper.showInputDlg(ListInput.this, null, JAMS.i18n("New_value"), value.toString());
            if (value != null) {
                listData.setElementAt(selection, value);
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        }*/
    }

    protected class TableData extends Observable {

        private ArrayList<Object[]> tableData = new ArrayList<Object[]>();
                
        public int getRowCount(){
            return tableData.size();
        }
        
        public void addElement(Object[] s) {
            tableData.add(s);
            //getTable().setTableData(tableData);

            getTable().setColumnSelectionInterval(
                    getTable().getColumnCount()-1, getTable().getColumnCount()-1);

            this.setChanged();
            this.notifyObservers();
        }

        public void removeElementAt(int selection) {
            tableData.remove(selection);
            //getTable().setTableData(tableData);
            this.setChanged();
            this.notifyObservers();
        }

        public Object[] getElementAt(int selection) {
            return tableData.get(selection);
        }

        public void setElementAt(int selection, Object[] s) {
            tableData.set(selection, s);
        }

        public ArrayList<Object[]> getValue() {
            return tableData;
        }

        public void setValue(ArrayList<Object[]> tableData) {
            this.tableData = tableData;
            //getTable().setListData(tableData);
            this.setChanged();
            ((AbstractTableModel)TableInput.this.getTable().getModel()).fireTableDataChanged();
            this.notifyObservers();
        }
    };
}
