package jams.worldwind.handler;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolylines;
import jams.worldwind.ui.model.ShapefilePropertiesModel;
import jams.worldwind.ui.renderer.MaterialClassCellRenderer;
import jams.worldwind.ui.view.GlobeView;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class SurfacePolylineClassCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private final String buttonText = "...";
    private final JButton theButton;
    private JOptionPane propertiesDialog;

    private SurfacePolylines polylines;

    public SurfacePolylineClassCellEditor() {
        this.theButton = new JButton(this.buttonText);
        this.theButton.addActionListener(this);
        this.theButton.setBorderPainted(true);
    }

    @Override
    public Object getCellEditorValue() {
        return this.polylines;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //System.out.println(value);
        if (value instanceof SurfacePolylines) {
            this.polylines = (SurfacePolylines) value;

        }
        return this.theButton;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFrame propertiesFrame = new JFrame("OBJECT PROPERTIES");
        JTable propertiesTable = new JTable();

        propertiesTable.setIntercellSpacing(new Dimension(6, 6));
        propertiesTable.setRowHeight(propertiesTable.getRowHeight() + 6);

        JScrollPane scrollPane = new JScrollPane(propertiesTable);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout());
        mainPanel.add(scrollPane);

        propertiesFrame.getContentPane().add(mainPanel);
        propertiesFrame.setSize(propertiesFrame.getPreferredSize());

        propertiesTable.setModel(new ShapefilePropertiesModel(this.polylines));
        propertiesTable.setDefaultEditor(Material.class, new MaterialClassCellEditor());
        propertiesTable.setDefaultRenderer(Material.class, new MaterialClassCellRenderer(true));
        
        propertiesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                GlobeView.getInstance().getWorldWindow().redrawNow();
            }
        });
        
        TableCellRenderer rendererFromHeader = propertiesTable.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER); // Here you can set the alignment you want.
        this.autoResizeColWidth(propertiesTable, (DefaultTableModel) propertiesTable.getModel());

        propertiesFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        propertiesFrame.setVisible(true);

        fireEditingStopped();
    }

    public JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //table.setModel(model);

        int margin = 5;

        for (int i = 0; i < table.getColumnCount(); i++) {
            int vColIndex = i;
            DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn col = colModel.getColumn(vColIndex);
            int width = 0;

            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();

            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }

            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

            width = comp.getPreferredSize().width;

            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                        r, vColIndex);
                Object o = table.getValueAt(r, vColIndex);
                if (!(o instanceof Material)) {
                    width = Math.max(width, comp.getPreferredSize().width);
                }
            }

            // Add margin
            width += 2 * margin;

            // Set the width
            col.setPreferredWidth(width);
        }

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
                SwingConstants.LEFT);

        // table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

}
