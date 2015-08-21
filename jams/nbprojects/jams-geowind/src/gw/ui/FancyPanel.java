/*
 * P.java
 *
 * Created on November 13, 2008, 1:06 PM
 */
package gw.ui;

import gov.nasa.worldwind.AnaglyphSceneController;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.StatusBar;
import gw.layers.GridCoverageLayer;
import gw.layers.SimpleFeatureLayer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
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
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import gw.api.Addon;
import gw.api.Events;
import gw.api.GeoWindView;
import gw.calc.DataTable;
import gw.layers.LayerFactory;
import gw.ui.layerproperies.AttributeTableProperties;
import gw.ui.layerproperies.ShapefileLayerProperties;
import gw.ui.util.Files.LayerFilter;
import gw.ui.util.ProxyTableModel;
import gw.ui.util.SimpleTableModel;
import gw.ui.util.SlidingSupport;
import gw.util.WorldWindUtils;
import java.awt.Component;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Map.Entry;
import javax.swing.JFileChooser;
import org.jdesktop.swingx.JXCollapsiblePane;
import reg.DataTransfer;
import reg.viewer.Viewer;
import worldwind.kml.KMLLayer;
import worldwind.kml.KMLParser;
import worldwind.kml.model.KMLFile;

/** The basic panel.
 *
 * @author  od
 */
public class FancyPanel extends JPanel implements GeoWindView {

    /**
     * the language definitions
     */
    private static java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("gw/resources/language"); // NOI18N

    public static final String TITLE = bundle.getString("L_PROJECTNAME");

    public static final boolean enableNext = true;

    /**
     * the working layer to add columns to it during runtime
     */
    private String workLayerName = null;

    /**
     * unused standard layers
     */
    private Vector<String> unusedStandardLayers = new Vector<String>();

    /**
     * additional application layers
     */
    private Vector<File> applicationLayers = new Vector<File>();

    /**
     * get properties of working shape file
     * @return ShapefileLayerProperties
     */
    public ShapefileLayerProperties getWorkShapeFileProperties() {
        return getShapeLayer(this.workLayerName);
    }

    public String getWorkLayerName() {
        return workLayerName;
    }

    public void flyToWorkLayer() {
        SimpleFeatureLayer workLayer = (SimpleFeatureLayer) getWorkLayer();
        if (workLayer != null) {
            System.out.println("fly to layer " + workLayer.getName());
            WorldWindUtils.flyTo(workLayer.getSector(), ww);
        } else {
            System.out.println("but worklayer was null.. ");
        }
    }

    private Layer getWorkLayer() {
        if (workLayerName == null) {
            return null;
        } else {
            LayerList layers = ww.getModel().getLayers();
            for (Layer l : layers) {
                if (l.getName().equals(workLayerName)) {
                    return l;
                }
            }
            return null;
        }
    }

    public void setWorkLayerName(String workLayerName) {
        this.workLayerName = workLayerName;
    }

    public Vector<File> getApplicationLayers() {
        return applicationLayers;
    }

    public void setApplicationLayers(Vector<File> applicationLayers) {
        this.applicationLayers = applicationLayers;
    }

    public Vector<String> getUnusedStandardLayers() {
        return unusedStandardLayers;
    }

    public void setUnusedStandardLayers(Vector<String> unusedStandardLayers) {
        this.unusedStandardLayers = unusedStandardLayers;
    }

    public void addWorkShapeFileColumn(DataTransfer dataTransfer) {
        ShapefileLayerProperties workShapeFileProperties = getWorkShapeFileProperties();
        if (workShapeFileProperties != null) {

            // get data and ids from dataTransfer
            double[][] dtValues = dataTransfer.getData();
            String[] dtColumnNames = dataTransfer.getNames();
            double[] dtIds = dataTransfer.getIds();

            // init the new values structure
            int max = dtValues.length;

            // id-column in shapeFile?
            ProxyTableModel ptm = workShapeFileProperties.getTableModel();
            int idColumn = workShapeFileProperties.getIdColumn();
            System.out.println("addWorkShapeFileColumn. id-column found:" + idColumn);
            if (idColumn < 0) {
            }
            String idColumnName = ptm.getColumnName(idColumn);
            System.out.println("id-column:" + idColumn + ", " + idColumnName);

            // ids -> fill values according to them:
            Map<Integer, Double> idData = null;
            if (dtIds != null && dtIds.length > 0 && idColumn > 0) {
                idData = workShapeFileProperties.getTableModel().getDataForColumn(idColumn);
            }

            for (int i = 0; i < dtColumnNames.length; i++) {
                String dtColumnName = dtColumnNames[i];
                double[] dtColumnValues = dtValues[i];
                Vector<Double> wrappedValues = new Vector<Double>(max);
                if (idData != null) {
                    wrappedValues = getResortedValues(idData, dtIds, dtColumnValues);
                } else {
                    System.out.println("Couldn't correctly match the values, using default ordering!");
                    // no Ids -> take the values as it is (but wrapped)
                    for (double value : dtColumnValues) {
                        wrappedValues.add(new Double(value));
                    }
                }

                // add the column
                SimpleTableModel tableModel = new SimpleTableModel();
                tableModel.addColumn(dtColumnName, wrappedValues);
                System.out.println("column added with class:" + tableModel.getColumnClass(0).getName());

                ptm.addTableModel(tableModel);
                int col = ptm.getColumnCount() - 1;
                workShapeFileProperties.classifyColumn(col);

                //workShapeFileProperties.renewTable(ptm);

                System.out.println("addWorkShapeFileColumn. column added: " + tableModel.getColumnName(0));
            }

        }
    }

    /**
     * get values in sequence of IDs
     *
     * @param idValues - collection of ids in wished sequence
     * @param inputIds
     * @param inputValues - fitting to inputIds
     * @return vector with sorted values
     */
    private Vector<Double> getResortedValues(Map<Integer, Double> idData, double[] inputIds, double[] inputValues) {

        double[] values = new double[idData.size()];
        Map<Double, Double> idValueMap = new HashMap<Double, Double>();
        
        for (int i = 0; i < inputIds.length; i++) {
            idValueMap.put(inputIds[i], inputValues[i]);
        }
       
        for (Integer idx : idData.keySet()) {
            
            Double id = idData.get(idx);
            Double value = idValueMap.get(id);
            values[idx] = value;
            
        }
        
        Vector<Double> result = new Vector<Double>();
        for (double d : values) {
            result.add(d);
        }
        return result;
    }
    
//    private Vector<Double> getResortedValues(Map<Integer, Double> idData, double[] inputIds, double[] inputValues) {
//
//        // create reversed map of idData
//        Map<Double, Integer> idPosition = new HashMap<Double, Integer>();
//        Iterator<Entry<Integer, Double>> iter = idData.entrySet().iterator();
//        while (iter.hasNext()) {
//            Entry<Integer, Double> e = iter.next();
//            idPosition.put(e.getValue(), e.getKey());
//        }
//        
//
//        double[] sortedValues = new double[inputIds.length];
//        int success = 0;
//        int failure = 0;
//
//        for (int i = 0; i < inputIds.length; i++) {
//
//            if (!idPosition.containsKey(inputIds[i])) {
//                failure++;
//                continue;
//            }
//
//            success++;
//            int index = idPosition.get(inputIds[i]);
//            sortedValues[index] = inputValues[i];
//        }
//
//        Vector<Double> result = new Vector<Double>();
//        for (double d : sortedValues) {
//            result.add(d);
//        }
//        return result;
//    }    

    private Component getTop(JPanel c) {
        int ncomponents = c.getComponentCount();
        for (int i = 0; i < ncomponents; i++) {
            Component comp = c.getComponent(i);
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }
//    
    PropertyChangeListener updater = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pn = evt.getPropertyName();
            if (pn.equals(Events.GF_UPDATE) || pn.equals(Events.GF_SELECTION)) {
                ww.redrawNow();
                Layer layer = (Layer) evt.getNewValue();
                if (layer != null && layer instanceof SimpleFeatureLayer) {
                    al.show(data, layer.getName());
                    DataTable dt = (DataTable) getTop(data);
                    console.setCommandHandler(dt);
                } else {
                    console.setCommandHandler(null);
                    al.show(data, "none");
                }
            } else if (pn.equals("gf_remove_layer")) {
                Layer layer = (Layer) evt.getOldValue();
                removeLayer(layer);
            } else if (pn.equals("ww_doubleclick")) {
//                Layer layer = (Layer) evt.getNewValue();
                setAttributeTableVisible(true);
            } else if (pn.equals("ww_flyto")) {
                Layer layer = (Layer) evt.getNewValue();
                if (layer instanceof SimpleFeatureLayer) {
                    SimpleFeatureLayer sfl = (SimpleFeatureLayer) layer;
                    System.out.println("fly to layer " + sfl.getName());
                    WorldWindUtils.flyTo(sfl.getSector(), ww);
                }
                if (layer instanceof GridCoverageLayer) {
                    GridCoverageLayer sfl = (GridCoverageLayer) layer;
                    WorldWindUtils.flyTo(sfl.getSector(), ww);
                }
            } else if (pn.equals("ww_add_attr")) {
                SimpleFeatureLayer layer = (SimpleFeatureLayer) evt.getOldValue();
                DataTable attrFile = (DataTable) evt.getNewValue();
                try {
                    addData(attrFile, layer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (pn.equals("ww_classify")) {
                SimpleFeatureLayer layer = (SimpleFeatureLayer) evt.getOldValue();
                String layerName = layer.getName();
                String dataPanelName = layerName.substring(0, layerName.length()-4) + ".dbf";
                int col = ((Integer) evt.getNewValue()).intValue();
                AttributeTableProperties table = (AttributeTableProperties) lc.getLayerPanel(dataPanelName);
                if (table != null) {
                    table.setMinMaxValue(col);
                }
            }
        }
    };
//

    Action exitAction = new AbstractAction() {

        {
            putValue(NAME, "Exit");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Viewer.destroy();
            // no system exit here
        }
    };
//

    WorldWindowGLJPanel ww;

    SlidingSupport ss = new SlidingSupport(true);

    LayerControl lc;

    ViewControl vc;
//    OutputControl con = new OutputControl();

    ConsoleControl console = new ConsoleControl();
//    

    CardLayout al = new CardLayout();

    JXCollapsiblePane data1 = new JXCollapsiblePane(JXCollapsiblePane.Orientation.VERTICAL);

    JPanel data = new JPanel(al);
//    

    static Color blueish = new Color(174, 209, 255);
//

    Addon activeAddon;
//    JFrame full = new JFrame();

    JPanel center = new JPanel(new BorderLayout());

    JToggleButton upButton = new JToggleButton();

//    public void dispose() {
//        full.dispose();
//    }
    /** Creates new form P */
    /**
     * constructor with some initial data
     * @param theFrame
     * @param unusedStandardLayers - unused standard layers coming with geowind
     * @param applicationLayers - list of own application layers
     */
    public FancyPanel(JFrame theFrame, Vector<String> unusedStandardLayers, Vector<File> applicationLayers) {

        if (unusedStandardLayers != null) {
            setUnusedStandardLayers(unusedStandardLayers);
        }
        if (applicationLayers != null) {
            setApplicationLayers(applicationLayers);
        }

        initComponents();
//        setupWindowControls(theFrame);
        createMenu();
        setupComponents();

        setDropTarget(new DropTarget(this, new DropTargetAdapter() {

            @Override
            public void dragEnter(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            public void dragOver(DropTargetDragEvent e) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            @SuppressWarnings ("unchecked")
            public void drop(DropTargetDropEvent evt) {
                try {
                    Transferable tr = evt.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        add(fileList);
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
    }

    private void add(List<File> fileList) throws Exception {
        try {
            for (File file : fileList) {
                if (matchFileExt(file, "shp") > -1) {
                    SimpleFeatureLayer sf = LayerFactory.fromShapefile(file, ww);
                    sf.setAttrMinColor(new Color(0.2f, 0.4f, 0.1f, 0.75f));
                    sf.setAttrMaxColor(new Color(0, 0.5f, 1, 0.75f));
                    WorldWindUtils.flyTo(sf.getSector(), ww);
                    addLayer(sf);
                } else if (matchFileExt(file, "tif", "tiff") > -1) {
                    GridCoverageLayer sf = LayerFactory.fromGeotiff(file);
                    WorldWindUtils.flyTo(sf.getSector(), ww);
                    addLayer(sf);
                } else if (matchFileExt(file, "kmz", "kml") > -1) {
                    KMLFile kml = KMLParser.parseURL(file.toURI().toURL());
                    KMLLayer kmlLayer = new KMLLayer(kml);
                    kmlLayer.setName(file.getName());
                    WorldWindUtils.flyTo(kml.getSector(), ww);
                    addLayer(kmlLayer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            (new ExceptionFrame(e)).setVisible(true);
        }
    }

    /** Matches a file extension
     * 
     * @param file
     * @param ext
     * @return
     */
    private static int matchFileExt(File file, String... ext) {
        String fn = file.getName().toLowerCase();
        for (int i = 0; i < ext.length; i++) {
            if (fn.endsWith(ext[i])) {
                return i;
            }
        }
        return -1;
    }

    private JMenuItem addon(final Addon ao, ButtonGroup bg) {
        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(ao.getName(), false);
        bg.add(mi);
        ItemListener il = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    showAddon(ao);
                }
            }
        };
        mi.addItemListener(il);
        return mi;
    }

    private void showAddon(Addon newAddon) {
    }

    private JMenu createAddonMenu() {
        JMenu m = new JMenu("Add-ons");
        ButtonGroup bg = new ButtonGroup();
        JCheckBoxMenuItem none = new JCheckBoxMenuItem("Not Shown", true);
        bg.add(none);
        none.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    showAddon(null);
                }
            }
        });

        m.add(none);
        m.addSeparator();
        return m;
    }
    File curr;

    private void createMenu() {
        JMenuItem m;
        final JPopupMenu popup = new JPopupMenu("System");
        m = popup.add(this.bundle.getString("L_OPEN"));
        m.setEnabled(false);
        m = popup.add(this.bundle.getString("L_SAVE"));
        m.setEnabled(false);
        popup.addSeparator();
        m = popup.add(this.bundle.getString("L_ADDLAYER"));
        m.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new LayerFilter());
                fc.setDialogTitle(bundle.getString("L_ADDLAYER"));
                fc.setMultiSelectionEnabled(true);
                if (curr != null) {
                    fc.setCurrentDirectory(curr);
                }

                int returnVal = fc.showOpenDialog((Component) ww);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    List<File> f = Arrays.asList(fc.getSelectedFiles());
                    try {
                        add(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                curr = fc.getCurrentDirectory();
            }
        });
        popup.addSeparator();
        popup.add(createAddonMenu());
        m = popup.add(this.bundle.getString("L_OPTIONS"));
        m.setEnabled(false);
        popup.addSeparator();
        popup.add(exitAction);

    }

    private void setupWindowControls(final JFrame f) {



        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        full.setUndecorated(true);
//        full.setBounds(0, 0, screenSize.width, screenSize.height);
//        full.getContentPane().add(center);
        center.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this.bundle.getString("L_fullscreen"));
        center.getActionMap().put("fullscreen",
                new AbstractAction("fullscreen") {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
//                        full.setVisible(false);
                        center.remove(ww);
                        wwDesk.add(ww, BorderLayout.CENTER);
                        f.toFront();
                        f.setVisible(true);
                    }
                });



        

        resizeLabel.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        resizeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        resizeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                });

        resizeLabel.addMouseMotionListener(new MouseMotionListener() {

            Point o;

            @Override
            public void mouseDragged(final MouseEvent e) {
                final Point p = e.getPoint();
                f.setSize(f.getSize().width + (p.x - o.x), f.getSize().height + (p.y - o.y));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                o = e.getPoint();
            }
        });
    }

    public void setAttributeTableVisible(boolean visible) {
        upButton.setSelected(visible);
    }

    private void setupComponents() {

        data1.add(data);
        data1.setCollapsed(true);
        data1.setAnimated(true);

        Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnaglyphSceneController.class.getName());

        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        ww = new WorldWindowGLJPanel();
        ww.setModel(m);

        StatusBar status = new StatusBar();
        status.setEventSource(ww);

        ((AnaglyphSceneController) ww.getSceneController()).setDisplayMode(
                AnaglyphSceneController.DISPLAY_MODE_MONO);

        vc = new ViewControl(ww);
        vc.addPropertyChangeListener(updater);

        lc = new LayerControl(updater);

        upButton.setIcon(new ImageIcon(getClass().getResource("/gw/resources/up.gif"))); // NOI18N
        upButton.setPreferredSize(new Dimension(12, 12));
        upButton.setToolTipText(this.bundle.getString("L_CLICK_OPEN_ATTR"));
        upButton.setFocusable(false);
        upButton.setFocusPainted(false);
        upButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                data1.setCollapsed(e.getStateChange() == ItemEvent.DESELECTED);
            }
        });


        tools.add(ss.add(this.bundle.getString("L_LAYER"), lc));
        tools.add(ss.add(this.bundle.getString("L_VIEW"), vc));
//        tools.add(ss.add("Edit", new EditControl()));
//        tools.add(ss.add("WMS", new WMSControl(this)));
//        tools.add(ss.add("Output", con));
//        tools.add(ss.add("Console", console));



        JToolBar sou = new JToolBar();
        LayoutManager fl = new BorderLayout();
        sou.setLayout(fl);
        sou.setFloatable(false);
        sou.add(upButton, BorderLayout.WEST);
        sou.add(status, BorderLayout.CENTER);
        sou.add(resizeLabel, BorderLayout.EAST);

        JPanel tools1 = new JPanel(new BorderLayout());
        tools1.add(sou, BorderLayout.SOUTH);
        tools1.add(data1, BorderLayout.CENTER);

        wwDesk.add(ss.getPane(), BorderLayout.NORTH);
        wwDesk.add(ww, BorderLayout.CENTER);
        wwDesk.add(tools1, BorderLayout.SOUTH);

        // Add TerrainProfileLayer
        TerrainProfileLayer tpl = new TerrainProfileLayer();
        tpl.setEnabled(false);
        tpl.setEventSource(ww);
        tpl.setStartLatLon(LatLon.fromDegrees(0, -10));
        tpl.setEndLatLon(LatLon.fromDegrees(0, 65));

        // USGS topomaps
        USGSTopographicMaps topo = new USGSTopographicMaps();
        topo.setEnabled(false);

        LayerList layers = m.getLayers();
        // add the default layer
        layers.add(tpl);
        layers.add(topo);

        // remove unused layers
        Vector<String> unusedLayers = getUnusedStandardLayers();
        Iterator<Layer> iter = layers.iterator();
        while (iter != null && iter.hasNext()) {
            Layer actLayer = iter.next();
            String layerName = actLayer.getName();
            if (unusedLayers.contains(layerName)) {
                layers.remove(actLayer);
                System.out.println("Layer " + layerName + " removed due to property settings.");
            }
        }

        // add the application layers
        for (File applicationLayerFile : getApplicationLayers()) {
            try {
                String fileName = applicationLayerFile.getName();
                if (fileName.endsWith("shp") || fileName.endsWith("SHP")) {
                    SimpleFeatureLayer sfl = LayerFactory.fromShapefile(applicationLayerFile, ww);
                    layers.add(sfl);
                    System.out.println("application-layer added (shape):" + sfl.getName());
                }
                if (fileName.endsWith("tif") || fileName.endsWith("tiff") || fileName.endsWith("TIF") || fileName.endsWith("TIFF")) {
                    GridCoverageLayer sfl = LayerFactory.fromGeotiff(applicationLayerFile);
                    layers.add(sfl);
                    System.out.println("application-layer added (tif):" + sfl.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        layers.add(gl);
        for (Layer l : layers) {
            System.out.println("layer:" + l.getName() + " (" + l.getClass() + ")");
            lc.addLayer(l, updater, ww);
        }

        JLabel lbl = new JLabel(" [NO DATA] ", JLabel.CENTER);
        lbl.setEnabled(false);
        data.add(lbl, "none");
        al.show(data, "none");
    }

    public void addData(DataTable dt, SimpleFeatureLayer layer) throws Exception {
//        dt.setOut(con.getOut());
        data.add(dt, layer.getName());
        al.show(data, layer.getName());
    }

    /** Add a new (non-ww-internal) layer
     * 
     * @param l tha layer to add.
     */
    @Override
    public void addLayer(Layer l) {
        ww.getModel().getLayers().add(l);      // add it to WW LM
        lc.addLayer(l, updater, ww);           // add it to the UI.
    }

    @Override
    public void removeLayer(Layer l) {
        ww.getModel().getLayers().remove(l);    // add it to WW LM
        lc.removeLayer(l, updater);             // add it to the UI.
    }

    public ShapefileLayerProperties getShapeLayer(String theName) {

        JPanel thePanel = lc.getLayerPanel(theName);
        if (thePanel != null && thePanel instanceof ShapefileLayerProperties) {
            return (ShapefileLayerProperties) thePanel;
        }
        return null;
    }

    /**
     * add the layer with work shape file
     * @param workShapeFile
     */
    public void addWorkShape(File workShapeFile)
            throws Exception {
        if (workShapeFile != null) {
            SimpleFeatureLayer layer = LayerFactory.fromShapefile(workShapeFile, ww);
            addLayer(layer);
            String layerName = layer.getName();
            this.workLayerName = layerName;
            System.out.println("work-layer added:" + layerName);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings ("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resizeLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        tools = new javax.swing.JToolBar();
        wwDesk = new javax.swing.JPanel();

        resizeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gw/resources/corner.png"))); // NOI18N

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jPanel2.setPreferredSize(new java.awt.Dimension(673, 24));

        tools.setBorder(null);
        tools.setFloatable(false);
        tools.setRollover(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(tools, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(450, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tools, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_START);

        wwDesk.setLayout(new java.awt.BorderLayout());
        add(wwDesk, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel resizeLabel;
    private javax.swing.JToolBar tools;
    private javax.swing.JPanel wwDesk;
    // End of variables declaration//GEN-END:variables
}
