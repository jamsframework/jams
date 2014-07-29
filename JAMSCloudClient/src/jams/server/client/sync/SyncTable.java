/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.sync;

import jams.JAMS;
import jams.server.client.Controller;
import jams.server.client.Utilities;
import jams.server.entities.Workspace;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author christian
 */
public class SyncTable extends JTable {

    Component tableSyncModeCells[];
    Font defaultFont = new Font("Times New Roman", Font.PLAIN, 10);

    File localWorkspace = null;
    Workspace serverWorkspace = null;
    Controller ctrl = null;

    final int COLUMN_COUNT = 5;
    final int COLUMN_SIZE[] = {25, 365, 75, 90, 50};
    final String COLUMN_NAMES[] = {"", JAMS.i18n("Path"), JAMS.i18n("Extension"), JAMS.i18n("Operation"), JAMS.i18n("SIZE")};
    final Class COLUMN_CLASSES[] = {Boolean.class, String.class, String.class, FileSync.SyncMode.class, Long.class};

    final int SYNCMODE_COLUMN = 3;

    public SyncTable(Controller ctrl, Font defaultFont) {
        super(10, 5);
        this.ctrl = ctrl;

        if (defaultFont != null) {
            this.defaultFont = defaultFont;
        }
    }

    public void setLocalWorkspace(File localWorkspace) {
        this.localWorkspace = localWorkspace;
        initModel();
    }

    public File getLocalWorkspace() {
        return this.localWorkspace;
    }

    public void setServerWorkspace(Workspace serverWorkspace) {
        this.serverWorkspace = serverWorkspace;
        initModel();
    }

    public Workspace getServerWorkspace() {
        return this.serverWorkspace;
    }

    private void initModel() {
        SyncTableModel syncTableModel = new SyncTableModel(ctrl, localWorkspace, serverWorkspace);
        setModel(syncTableModel);

        SyncWorkspaceTableRenderer renderer
                = new SyncWorkspaceTableRenderer(syncTableModel.getSyncList());

        setDefaultRenderer(String.class, renderer);
        setDefaultRenderer(Long.class, renderer);
        getColumnModel().getColumn(SYNCMODE_COLUMN).setCellRenderer(renderer);
        
        getColumnModel().getColumn(SYNCMODE_COLUMN).setCellEditor(new SyncModeEditor());

        setShowHorizontalLines(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < COLUMN_COUNT; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(COLUMN_SIZE[i]);
        }
    }
    
    @Override
    public SyncTableModel getModel() {
        if (super.getModel() instanceof SyncTableModel)
            return (SyncTableModel) super.getModel();
        else 
            return null;
    }

    public class SyncTableModel extends AbstractTableModel {

        ArrayList<FileSync> syncList = null;

        public SyncTableModel(Controller ctrl, File localWsDirectory, Workspace remoteWs) {
            if (ctrl == null
                    || localWsDirectory == null
                    || remoteWs == null) {
                syncList = new ArrayList<>();
            } else {
                syncList = ctrl.getWorkspaceController()
                        .getSynchronizationList(localWsDirectory, remoteWs)
                        .getList(null);
            }
        }

        public List<FileSync> getSyncList() {
            return syncList;
        }

        public FileSync getRoot() {
            if (syncList == null || syncList.isEmpty()) {
                return null;
            }
            return syncList.get(0).getRoot();
        }

        @Override
        public int getRowCount() {
            return syncList.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_CLASSES[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0
                    || (columnIndex == SYNCMODE_COLUMN && tableSyncModeCells[rowIndex] instanceof JComboBox);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FileSync fs = syncList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return fs.isDoSync();
                case 1:
                    if (fs.getLocalFile() != null) {
                        return fs.getLocalFile().getPath();
                    }
                    return null;
                case 2: {
                    String name = syncList.get(rowIndex).getLocalFile().getName();
                    int lastIndex = name.lastIndexOf(".");
                    if (lastIndex == -1) {
                        return "";
                    } else {
                        return name.substring(lastIndex, name.length());
                    }
                }
                case 3:
                    return syncList.get(rowIndex).getSyncMode();
                case 4:
                    if (fs.getServerFile() != null) {
                        return fs.getServerFile().getFile().getFileSize();
                    } else {
                        return 0;
                    }
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                syncList.get(rowIndex).setDoSync((Boolean) aValue, true);
                //generateTableCells();                 
                fireTableDataChanged();
            }
            if (columnIndex == 3) {
                syncList.get(rowIndex).setSyncMode((FileSync.SyncMode) aValue);
                generateTableCells(syncList);
                fireTableDataChanged();
            }
        }
    }

    private class SyncModeEditor extends AbstractCellEditor
            implements TableCellEditor,
            ActionListener {

        JComboBox currentBox = null;

        public SyncModeEditor() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        @Override
        public Object getCellEditorValue() {
            if (currentBox != null) {
                return currentBox.getSelectedItem();
            }
            return null;
        }

        //Implement the one method defined by TableCellEditor.
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            if (tableSyncModeCells[row] instanceof JComboBox) {
                return currentBox = (JComboBox) tableSyncModeCells[row];
            } else {
                return currentBox = null;
            }
        }
    }

    private class SyncWorkspaceTableRenderer extends JLabel implements TableCellRenderer {

        List<FileSync> syncList = null;

        public SyncWorkspaceTableRenderer(List<FileSync> syncList) {
            this.syncList = syncList;

            generateTableCells(syncList);

            setOpaque(true); //MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            //special handling of column 3
            if (column == SYNCMODE_COLUMN) {
                return tableSyncModeCells[row];
            }

            setBackground(Color.WHITE);
            setForeground(Color.BLACK);

            if (value instanceof Long) {
                setText(Utilities.formatSize((Long) value));
                setHorizontalAlignment(JLabel.RIGHT);
            } else if (value instanceof Integer) {
                setText(Utilities.formatSize((Integer) value));
                setHorizontalAlignment(JLabel.RIGHT);
            } else {
                setText(value.toString());
                setHorizontalAlignment(JLabel.LEFT);
            }

            FileSync fs = syncList.get(row);
            if (!fs.isExisting()) {
                setBackground(Color.green);
            } else if (fs.isModified()) {
                setBackground(Color.red);
            }

            if (fs instanceof DirectorySync) {
                setBackground(Color.darkGray);
                setForeground(Color.white);
            }

            setFont(defaultFont);
            return this;
        }
    }

    private void generateTableCells(List<FileSync> syncList) {
        tableSyncModeCells = new Component[syncList.size()];

        for (int i = 0; i < syncList.size(); i++) {
            FileSync fs = syncList.get(i);

            if (fs.getSyncOptions().length == 1) {
                tableSyncModeCells[i] = new JLabel(
                        fs.getSyncOptions()[0].toString(),
                        JLabel.CENTER);

                tableSyncModeCells[i].setBackground(Color.WHITE);
                tableSyncModeCells[i].setForeground(Color.BLACK);

                if (!syncList.get(i).isExisting()) {
                    tableSyncModeCells[i].setBackground(Color.green);
                } else if (syncList.get(i).isModified()) {
                    tableSyncModeCells[i].setBackground(Color.red);
                }

                if (syncList.get(i) instanceof DirectorySync) {
                    tableSyncModeCells[i].setBackground(Color.darkGray);
                    tableSyncModeCells[i].setForeground(Color.white);
                }

                ((JLabel) tableSyncModeCells[i]).setOpaque(true);                
            } else {
                JComboBox box = new JComboBox(fs.getSyncOptions());
                box.putClientProperty("row", new Integer(i));
                box.setSelectedItem(fs.getSyncMode());
                box.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JComboBox src = (JComboBox) e.getSource();
                        int row = (Integer) src.getClientProperty("row");
                        ((SyncTableModel) getModel()).setValueAt(src.getSelectedItem(), row, 3);
                    }
                });
                tableSyncModeCells[i] = box;
                tableSyncModeCells[i].setBackground(Color.WHITE);
                tableSyncModeCells[i].setForeground(Color.BLACK);
            }
            tableSyncModeCells[i].setFont(defaultFont);            
        }
    }

}
