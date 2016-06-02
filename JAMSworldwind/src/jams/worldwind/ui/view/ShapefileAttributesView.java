package jams.worldwind.ui.view;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwind.render.SurfacePolylines;
import jams.worldwind.events.Events;
import jams.worldwind.handler.SurfacePolygonClassCellEditor;
import jams.worldwind.handler.SurfacePolylineClassCellEditor;
import jams.worldwind.ui.model.ShapefileAttributesModel;
import jams.worldwind.ui.renderer.SurfacePolygonClassCellRenderer;
import jams.worldwind.ui.renderer.SurfacePolylineClassCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
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
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class ShapefileAttributesView implements PropertyChangeListener, MouseListener {

    private static ShapefileAttributesView instance = null;
    final private JFrame theFrame;
    final private JTable theTable;
    private ShapefileAttributesModel theTableModel;

    //Singleton pattern
    /**
     *
     * @return
     */
    /*
    public synchronized static ShapefileAttributesView getInstance() {
        if (instance == null) {
            instance = new ShapefileAttributesView("ATTRIBUTESTABLE OF ACTIVE LAYER");
        }
        return instance;
    }
    */
    
    public ShapefileAttributesView(String title) {
        //Observer.getInstance().addPropertyChangeListener(this);
        this.theFrame = new JFrame(title);
        this.theTable = new JTable();

        this.theTable.setIntercellSpacing(new Dimension(6, 6));
        this.theTable.setRowHeight(this.theTable.getRowHeight() + 6);

        this.theTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                GlobeView.getInstance().getWorldWindow().redrawNow();
            }
        });
        this.theTable.addMouseListener(this);

        JScrollPane scrollPane = new JScrollPane(this.theTable);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 0));
        mainPanel.add(scrollPane);

        theFrame.getContentPane().add(mainPanel);
        theFrame.setSize(theFrame.getPreferredSize());
    }

    void dispose() {
        this.theFrame.setVisible(false);
        this.theFrame.dispose();
    }
    
    public boolean isVisible() {
        return this.theFrame.isVisible();
    }

    /**
     *
     * @param objects
     */
    public void fillTableWithObjects(List<?> objects) {
        this.theTableModel = new ShapefileAttributesModel(objects);
        
        /**
         * TESTDATA
         * TODO REMOVE
         */
        /*
        RandomNumbers rn = new RandomNumbers(0, 10, this.theTableModel.getRowCount());
        this.theTableModel.addColumn("TESTDATA", new Vector(rn.getDoubleValues()));
        */
        
        this.theTable.setModel(this.theTableModel);

        this.theTable.setAutoCreateRowSorter(true);
        this.theTable.setColumnSelectionAllowed(false);
        this.theTable.setRowSelectionAllowed(true);

        this.theTable.setDefaultEditor(SurfacePolygons.class, new SurfacePolygonClassCellEditor());
        this.theTable.setDefaultRenderer(SurfacePolygons.class, new SurfacePolygonClassCellRenderer());
        this.theTable.setDefaultEditor(SurfacePolylines.class, new SurfacePolylineClassCellEditor());
        this.theTable.setDefaultRenderer(SurfacePolylines.class, new SurfacePolylineClassCellRenderer());
        this.autoResizeColWidth(theTable, theTableModel);

        /*
        final JTableHeader header = this.theTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                int rows = theTableModel.getRowCount();
                System.out.println("ROWSCOUNT: " + rows);
                List values = new ArrayList<>(rows);
                for (int i = 0; i < rows; i++) {
                    values.add((Double) theTable.getValueAt(i, col));
                }
                IntervallCalculation iC = new IntervallCalculation(values);
                List intervall = iC.getEqualIntervall(12);

                System.out.println("INTERVALL: " + intervall);
                System.out.println("INTERVALLSIZE: " + intervall.size());
                ColorRamp cR = new ColorRamp(Color.red, Color.blue, intervall.size());
                System.out.println("COLORRAMP: " + cR.getColorRamp());
                for (int i = 0; i < rows; i++) {
                    SurfacePolygons o = (SurfacePolygons) theTable.getValueAt(i, 0);
                    JamsShapeAttributes sattr = (JamsShapeAttributes) o.getAttributes();
                    Double d = (Double) theTable.getValueAt(i, col);
                    int index = iC.getIntervallIndex(intervall, d);
                    sattr.setInteriorMaterial(new Material(cR.getColor(index)));
                    
                     for (int j = 0; j < intervall.size()-1; j++) {
                     Double d = (Double) theTable.getValueAt(i, col);
                     System.out.println("[" + j + "," + (j+1) + "] (" + d + ")");
                        
                        
                        
                     if (d >= (Double) intervall.get(j) && d < (Double) intervall.get(j + 1)) {
                     //data.put(pd.getDisplayName(), pd.getReadMethod().invoke(sattr));
                     sattr.setInteriorMaterial(new Material(cR.getColor(j)));
                     System.out.println("FOUND: " + d + " INDEX: " + i);
                     break;
                     }

                     }
                     
                }
                Globe.getInstance().getWorldWindow().redrawNow();

            }
        });
*/
        TableCellRenderer rendererFromHeader = this.theTable.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER); // Here you can set the alignment you want.
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

    public void scrollToObject(Object highlighted) {
        if (!(theTable.getParent() instanceof JViewport)) {
            return;
        }
        if (this.theFrame.isVisible()) {
            int rowIndex = 0;

            for (int i = 0; i < theTable.getRowCount(); i++) {
                if (theTable.getValueAt(i, 0).equals(highlighted)) {
                    rowIndex = i;
                    break;
                }
            }
            theTable.scrollRectToVisible(theTable.getCellRect(rowIndex,0, true));
            theTable.setRowSelectionInterval(rowIndex, rowIndex);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = this.theTable.getSelectedRow();
//        System.out.println("ROW: " + row);
        //this.theTable.scrollRectToVisible();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Events.LAYER_REMOVED)) {
            this.theFrame.setVisible(false);
        }
    }
}
