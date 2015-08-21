/*
 * ShapefileControlPanel.java
 *
 * Created on October 22, 2008, 2:32 PM
 */
package gw.ui.layerproperies;

import gov.nasa.worldwind.WorldWindow;
import gw.events.FeatureSelectionListener;
import gw.layers.SimpleFeatureAnnotationLayer;
import gw.layers.SimpleFeatureLayer;
import gw.util.StyleUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import ngmf.io.CSProperties;
import ngmf.io.DataIO;
import ngmf.util.Tables;
import gw.api.Events;
import gw.calc.DataTable;
import gw.ui.ExportPanel;
import gw.ui.util.ColoredTableCellRenderer;
import gw.ui.util.Files;
import gw.ui.util.ProxyTableModel;
import gw.ui.util.ShapefileTableModel;
import gw.util.GeoToolsUtils;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.styling.Style;
import reg.viewer.Viewer;

/**
 *
 * @author  od
 */
public class ShapefileLayerProperties extends javax.swing.JPanel {

    /**
     * @return the tm
     */
    public ProxyTableModel getTableModel() {
        return tm;
    }

    static class MemoryDataModel extends AbstractTableModel {

        double d[][];

        MemoryDataModel(int rows) {
            d = new double[2][rows];
        }

        @Override
        public int getRowCount() {
            return d[0].length;
        }

        @Override
        public int getColumnCount() {
            return d.length;
        }

        @Override
        public String getColumnName(int col) {
            return "C" + col;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return Double.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return (Double) d[column][row];
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            d[col][row] = (Double) aValue;
            fireTableCellUpdated(row, col);
        }
    }
    static Color[] tableColors = new Color[]{
        new Color(170, 211, 252),
        new Color(205, 253, 205),
        new Color(253, 242, 173),
        new Color(255, 223, 220),
        new Color(252, 217, 176),
        new Color(241, 238, 225),};
//
    WorldWindow wwpanel;
    SimpleFeatureLayer layer;
    //Renderable[] labels;
    SimpleFeatureAnnotationLayer labelLayer;
//    DefaultListModel attrModel = new DefaultListModel();
    private final ProxyTableModel tm = new ProxyTableModel(tableColors);
    private ShapefileLayerProperties thisInstance = null;
    private JTable theTable = null;

    /** Creates new form ShapefileControlPanel */
    public ShapefileLayerProperties(SimpleFeatureLayer layer, WorldWindow ww) {
        this.wwpanel = ww;
        this.layer = layer;
        initComponents();
        setupComponents(layer, ww);
        thisInstance = this;
    }

    private void setupComponents(final SimpleFeatureLayer layer, final WorldWindow ww) {

        selectCheck.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                layer.setPickEnabled(e.getStateChange() == ItemEvent.SELECTED);
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    layer.clearSelections();
                }
                update();
            }
        });

        labelCheck.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (labelLayer != null) {
                    labelLayer.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                }
                update();
            }
        });


        borderColorChooser.setColor(layer.getBorderColor());
        borderColorChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    layer.setBorderColor(borderColorChooser.getColor());
                    layer.redraw();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                update();
            }
        });

//        newTableButton.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                MemoryDataModel m = new MemoryDataModel(57);
//                attrModel.addElement("NewTable");
//                getTableModel().addTableModel(m);
//                getTableModel().fireTableStructureChanged();
//                update();
//            }
//        });

//        attriList.setCellRenderer(new ListCellRenderer() {
//
//            JLabel label = new JLabel();
//
//
//            {
//                label.setOpaque(true);
//            }
//
//            @Override
//            public Component getListCellRendererComponent(
//                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                Color bgColor = tableColors[index % tableColors.length];
//                label.setBackground(isSelected ? list.getSelectionBackground() : bgColor);
//                label.setForeground(isSelected ? list.getSelectionForeground() : Color.BLACK);
//                label.setText(" " + value.toString());
//                return label;
//            }
//        });

//        attriList.setModel(attrModel);
//        attriList.setDropTarget(new DropTarget(attriList, new DropTargetAdapter() {
//
//            @Override
//            public void dragEnter(DropTargetDragEvent e) {
//                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
//            }
//
//            @Override
//            public void dragOver(DropTargetDragEvent e) {
//                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
//            }
//
//            @Override
//            @SuppressWarnings("unchecked")
//            public void drop(DropTargetDropEvent evt) {
//                try {
//                    Transferable tr = evt.getTransferable();
//                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
//                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
//                        for (File file : fileList) {
//                            attrModel.addElement(file.getName());
//                        }
//                        addAttrData(fileList);
//                        evt.getDropTargetContext().dropComplete(true);
//                    } else {
//                        evt.rejectDrop();
//                    }
//                } catch (Exception io) {
//                    io.printStackTrace();
//                    evt.rejectDrop();
//                }
//            }
//        }));


        double[] minmax = GeoToolsUtils.getMinMaxHeight(layer.getFeatures(), ww.getModel().getGlobe(), layer.getCRS());
        labelMinMax.setText("[" + Integer.toString((int) minmax[0]) + ", " + Integer.toString((int) minmax[1]) + "]");
        elevationSlider.setValue((int) minmax[1]);
        layer.setElevation((int) minmax[1]);

        surfaceCheck.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean isSel = e.getStateChange() == ItemEvent.SELECTED;
                layer.setFloating(!isSel);
                if (!isSel) {
                    selectCheck.setSelected(false);
                    labelCheck.setSelected(false);

                    elevationText.setText(Double.toString(elevationSlider.getValue()));
                    double[] minmax = GeoToolsUtils.getMinMaxHeight(layer.getFeatures(), ww.getModel().getGlobe(), layer.getCRS());
                    labelMinMax.setText("[" + Integer.toString((int) minmax[0]) + ", " + Integer.toString((int) minmax[1]) + "]");
                }
                selectCheck.setEnabled(isSel);
                labelCheck.setEnabled(isSel);
                elevationText.setEnabled(!isSel);
                actualElevationLabel.setEnabled(!isSel);
                labelMinMax.setEnabled(!isSel);
                elevationSlider.setEnabled(!isSel);
                update();
            }
        });

        selectCheck.setSelected(layer.isPickEnabled());
        opacitySlider.setValue((int) (layer.getOpacity() * 100));
        opacitySlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                int value = slider.getValue();
                layer.setOpacity((double) value / 100);
                update();
            }
        });

        elevationText.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                elevationSlider.setValue(Integer.parseInt(elevationText.getText()));
            }
        });

        elevationSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                int value = slider.getValue();
                layer.setElevation((double) value);
                elevationText.setText(Double.toString(value));
                update();
            }
        });


        sldField.setDropTarget(new DropTarget(sldField, new DropTargetAdapter() {

            @Override
            public void dragEnter(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            public void dragOver(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            public void drop(DropTargetDropEvent evt) {
                try {
                    Transferable tr = evt.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : fileList) {
                            if (file.getName().toLowerCase().endsWith(".sld")) {
                                sldField.setText(file.getName());
                                Style newStyle = StyleUtils.createFromFile(file);
                                layer.setStyle(newStyle);
                                layer.setDrawStyled(true);
                                layer.redraw();
                                update();
                            }
                        }
                        evt.getDropTargetContext().dropComplete(true);
                    } else {
                        evt.rejectDrop();
                    }
                } catch (Exception io) {
                    io.printStackTrace();
                    evt.rejectDrop();
                }
            }
        }));

        buttonExport.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        ExportPanel dialog = new ExportPanel(Viewer.getViewer().getFrame(), true, thisInstance);
                        dialog.setVisible(true);
                        dialog.dispose();
                    }
                });



        // make sure it gets called after the listener is attached, 
        // this is crazy
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("starting setupAttrData for layer " + layer.getName());
                    setupAttrData();

                    // populate ID-combobox (only possible after setupAttrData)
                    String[] columnNames = getTableModel().getColumnNames();
                    javax.swing.DefaultComboBoxModel idColumnSelectorModel = new DefaultComboBoxModel(columnNames);
                    idColumnSelectorModel.setSelectedItem(null);
                    idColumn.setModel(idColumnSelectorModel);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void update() {
        firePropertyChange(Events.GF_UPDATE, null, layer);
    }

    private void addAttrData(List<File> files) {
        for (File file : files) {
            if (file.getName().endsWith("csd")) {
                Reader r = null;
                try {
                    r = new FileReader(file);
                    CSProperties csp = DataIO.properties(r, "data");
                    System.out.println("len  " + layer.getFeatures().length);
                    TableModel m = Tables.fromCSP(csp, layer.getFeatures().length);
                    getTableModel().addTableModel(m);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        r.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        getTableModel().fireTableStructureChanged();
        update();
    }

    private void setupAttrData() throws Exception {
        ShapefileTableModel stm = new ShapefileTableModel(layer.getFeatureSource());
        System.out.println("setupAttrData for layer " + layer.getName());
//        attrModel.addElement(Files.getFileBase(layer.getFile()) + ".dbf");
//        File sldfile = new File(layer.getFile().getParentFile(), Files.getFileBase(layer.getFile()) + ".sld");
        File sldfile = Files.getSibling(new File(layer.getName()), "sld");
        sldField.setText(sldfile.toString());
        renewTable(stm);
    }

    public void renewTable(TableModel tableModel) {
        JTable t = new JTable();
        setup(t, tableModel, layer);
        this.theTable = t;
        DataTable dt = new DataTable(t);
        dt.setPreferredSize(new Dimension(100, 200));
        firePropertyChange("ww_add_attr", layer, dt);

    }

    public void classifyColumn(int column) {
        classifyColumn(theTable, layer, column);
    }

    private void classifyColumn(JTable table, SimpleFeatureLayer ds, int column) {
        getTableModel().setHighlightedColumn(column);
        table.repaint();
        //ds.setPrimaryAttr(column);
        ds.setAttrMinColor(getTableModel().getColorsForColumn(column).getA());
        ds.setAttrMaxColor(getTableModel().getColorsForColumn(column).getB());
        Map<Integer, Double> data = getTableModel().getDataForColumn(column);
        try {
            if (data != null) {
                //System.out.println("Daten gefunden..");
                ds.setColorRampValues(data);
                ds.setDrawStyled(false);
                ds.redraw();
            } else {
                System.out.println("keine Daten gefunden.");
                ds.setDrawStyled(true);
                ds.redraw();
            }
            //Remove all the old labels (if they exist)
            if (labelLayer != null) {
                wwpanel.getModel().getLayers().remove(labelLayer);
            }
            //add new labels
            labelLayer = new SimpleFeatureAnnotationLayer(ds, getTableModel().getStringDataForColumn(column));
            wwpanel.getModel().getLayers().add(labelLayer);
            labelLayer.setEnabled(labelCheck.isSelected());
            firePropertyChange("ww_classify", layer, new Integer(column));

        } catch (Exception e) {
            e.printStackTrace();
        }
        update();
    }

    public int getIdColumn() {
        return this.idColumn.getSelectedIndex();
    }

    public void setIdColumn(int col) {
        this.idColumn.setSelectedIndex(col);
    }

    public SimpleFeatureLayer getLayer() {
        return this.layer;
    }

    private void setup(final JTable table, TableModel model, final SimpleFeatureLayer ds) {
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()) {
                    table.repaint();
                    return;
                }

                ds.clearSelections();
                //find feature object
                SimpleFeature[] features = ds.getFeatures();

                for (int i = 0; i < features.length; i++) {
                    if (table.isRowSelected(i)) {
                        ds.addFeatureSelection(features[i]);
                    }
                }
                update();
            }
        });

        getTableModel().addTableModel(model);
        table.setModel(getTableModel());

        //Table header stuff
        final JTableHeader header = table.getTableHeader();
        final Font boldFont = header.getFont().deriveFont(Font.BOLD);
        final TableCellRenderer headerRenderer = header.getDefaultRenderer();

        header.setDefaultRenderer(new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == getTableModel().getHighlightedColumn()) {
                    comp.setFont(boldFont);
                }
                return comp;
            }
        });

        table.getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                int column = header.columnAtPoint(arg0.getPoint());
                // System.out.println("geklickt auf Spalte " + column);
                classifyColumn(table, ds, column);
            }
        });

        TableCellRenderer r = new ColoredTableCellRenderer();
        table.setDefaultRenderer(Object.class, r);
        table.setDefaultRenderer(Double.class, r);
        //table.setDefaultRenderer(String.class, r);
        table.setDefaultRenderer(Integer.class, r);
        table.setDefaultRenderer(Number.class, r);

        ds.addFeatureSelectionListener(new FeatureSelectionListener() {

            @Override
            public boolean featureSelected(SimpleFeature f) {
                //find index of feature
                if (f == null) {
                    return false;
                }

                SimpleFeature features[] = ds.getFeatures();
                for (int r = 0; r < features.length; r++) {
                    if (features[r].getID().equals(f.getID())) {
                        if (!table.isRowSelected(r)) {
                            table.clearSelection();
                            table.addRowSelectionInterval(r, r);
                            table.scrollRectToVisible(table.getCellRect(r, 0, true));
                            return true;
                        }
                    }
                }
                return true;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        opacitySlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        sldField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        borderColorChooser = new net.java.dev.colorchooser.ColorChooser();
        labelCheck = new javax.swing.JCheckBox();
        surfaceCheck = new javax.swing.JCheckBox();
        actualElevationLabel = new javax.swing.JLabel();
        elevationText = new javax.swing.JTextField();
        labelMinMax = new javax.swing.JLabel();
        elevationSlider = new javax.swing.JSlider();
        selectCheck = new javax.swing.JCheckBox();
        jSeparator4 = new javax.swing.JSeparator();
        idColumn = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        buttonExport = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();

        setLayout(null);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N
        jLabel2.setText(bundle.getString("L_Opacity")); // NOI18N
        add(jLabel2);
        jLabel2.setBounds(450, 0, 120, 14);

        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintTrack(false);
        add(opacitySlider);
        opacitySlider.setBounds(450, 20, 110, 47);

        jLabel1.setText("Styled Layer Descriptor:");
        add(jLabel1);
        jLabel1.setBounds(0, 0, 116, 14);

        sldField.setEditable(false);
        sldField.setText("<NONE>");
        add(sldField);
        sldField.setBounds(0, 20, 130, 20);

        jLabel3.setText(bundle.getString("L_Border_Color")); // NOI18N
        add(jLabel3);
        jLabel3.setBounds(30, 60, 100, 20);

        javax.swing.GroupLayout borderColorChooserLayout = new javax.swing.GroupLayout(borderColorChooser);
        borderColorChooser.setLayout(borderColorChooserLayout);
        borderColorChooserLayout.setHorizontalGroup(
                borderColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 18, Short.MAX_VALUE));
        borderColorChooserLayout.setVerticalGroup(
                borderColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 18, Short.MAX_VALUE));

        add(borderColorChooser);
        borderColorChooser.setBounds(0, 60, 20, 20);

        labelCheck.setText(bundle.getString("L_Show_Labels")); // NOI18N
        add(labelCheck);
        labelCheck.setBounds(150, 20, 85, 23);

        surfaceCheck.setSelected(true);
        surfaceCheck.setText(bundle.getString("L_Bond_to_Surface")); // NOI18N
        add(surfaceCheck);
        surfaceCheck.setBounds(300, 10, 103, 20);

        actualElevationLabel.setText(bundle.getString("L_Elevation")); // NOI18N
        add(actualElevationLabel);
        actualElevationLabel.setBounds(350, 40, 48, 14);

        elevationText.setText("0");
        elevationText.setEnabled(false);
        add(elevationText);
        elevationText.setBounds(350, 60, 80, 20);

        labelMinMax.setText(bundle.getString("L_Min_Max"));
        add(labelMinMax);
        labelMinMax.setBounds(350, 90, 100, 14);

        elevationSlider.setMajorTickSpacing(2000);
        elevationSlider.setMaximum(20000);
        elevationSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        elevationSlider.setPaintTicks(true);
        elevationSlider.setEnabled(false);
        add(elevationSlider);
        elevationSlider.setBounds(300, 30, 33, 80);

        selectCheck.setText(bundle.getString("L_Feature_Selection")); // NOI18N
        add(selectCheck);
        selectCheck.setBounds(150, 0, 109, 20);

        jSeparator4.setBackground(new java.awt.Color(208, 208, 191));
        jSeparator4.setForeground(new java.awt.Color(236, 233, 216));
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setEnabled(false);
        add(jSeparator4);
        jSeparator4.setBounds(440, 10, 10, 110);

        add(idColumn);
        idColumn.setBounds(170, 50, 110, 20);

        jLabel4.setText(bundle.getString("L_ID")); // NOI18N
        add(jLabel4);
        jLabel4.setBounds(150, 50, 20, 20);

        buttonExport.setBackground(null);
        buttonExport.setText(bundle.getString("L_Export")); // NOI18N
        add(buttonExport);
        buttonExport.setBounds(0, 100, 100, 23);

        jSeparator5.setBackground(new java.awt.Color(208, 208, 191));
        jSeparator5.setForeground(new java.awt.Color(236, 233, 216));
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setEnabled(false);
        add(jSeparator5);
        jSeparator5.setBounds(140, 10, 10, 110);

        jSeparator6.setBackground(new java.awt.Color(208, 208, 191));
        jSeparator6.setForeground(new java.awt.Color(236, 233, 216));
        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setEnabled(false);
        add(jSeparator6);
        jSeparator6.setBounds(290, 10, 10, 110);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actualElevationLabel;
    private net.java.dev.colorchooser.ColorChooser borderColorChooser;
    private javax.swing.JButton buttonExport;
    private javax.swing.JSlider elevationSlider;
    private javax.swing.JTextField elevationText;
    private javax.swing.JComboBox idColumn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JCheckBox labelCheck;
    private javax.swing.JLabel labelMinMax;
    private javax.swing.JSlider opacitySlider;
    private javax.swing.JCheckBox selectCheck;
    private javax.swing.JTextField sldField;
    private javax.swing.JCheckBox surfaceCheck;
    // End of variables declaration//GEN-END:variables
}
