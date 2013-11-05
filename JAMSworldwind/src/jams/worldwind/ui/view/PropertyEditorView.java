package jams.worldwind.ui.view;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolygons;
import jams.worldwind.handler.MaterialClassCellEditor;
import jams.worldwind.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.model.Globe;
import jams.worldwind.ui.model.PropertyEditorModel;
import jams.worldwind.ui.renderer.MaterialClassCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class PropertyEditorView {

    private static PropertyEditorView instance = null;
    private JFrame theFrame;
    private JTable theTable;
    private PropertyEditorModel theTableModel;

    //Singleton pattern

    /**
     *
     * @return 
     */
        public synchronized static PropertyEditorView getInstance() {
        if (instance == null) {
            instance = new PropertyEditorView("LAST SELECTED OBJECTS");
        }
        return instance;
    }

    private PropertyEditorView(String title) {
        this.theFrame = new JFrame(title);
        this.theTable = new JTable();
        this.theTable.setIntercellSpacing(new Dimension(6, 6));
        this.theTable.setRowHeight(this.theTable.getRowHeight() + 6);

        JScrollPane scrollPane = new JScrollPane(this.theTable);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 0));
        mainPanel.add(scrollPane);

        theFrame.getContentPane().add(mainPanel);
        theFrame.setSize(theFrame.getPreferredSize());
    }

    /**
     *
     * @param objects
     */
    public void fillTableWithObjects(List<?> objects) {
        HashMap<String, Object> data = new HashMap<>();
        for (Object cls : objects) {
            BeanInfo beanInfo;
            try {
                SurfacePolygons s = (SurfacePolygons) cls;
                JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
                beanInfo = Introspector.getBeanInfo(sattr.getClass().getSuperclass());
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                        data.put(pd.getDisplayName(), pd.getReadMethod().invoke(sattr));
                    }
                }

            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(PropertyEditorView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.setTableModel(objects, data);
    }

    /**
     *
     * @param cls
     */
    public void setClassProperties(List<?> cls) {

    }

    private void setTableModel(List<?> cls, HashMap<String, Object> data) {
        this.theTableModel = new PropertyEditorModel(cls, data);
        this.theTable.setModel(this.theTableModel);
        this.autoResizeColWidth(theTable, theTableModel);

        this.theTable.setDefaultEditor(Material.class, new MaterialClassCellEditor());
        this.theTable.setDefaultRenderer(Material.class, new MaterialClassCellRenderer(true));

        this.theTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                Globe.getInstance().getWorldWindow().redrawNow();

            }
        });

    }

    /*  Code from http://ieatbinary.com/2008/08/13/auto-resize-jtable-column-width/
     */

    /**
     *
     * @param table
     * @param model
     * @return
     */
    
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

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.theFrame.setTitle(title);
    }

    /**
     *
     * @param b
     */
    public void show(boolean b) {
        this.theFrame.pack();
        this.theFrame.setVisible(b);
    }
}
