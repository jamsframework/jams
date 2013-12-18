package jams.worldwind.ui.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.MessageListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import jams.worldwind.handler.SelectionHighlightController;
import jams.worldwind.ui.model.Globe;
import jams.worldwind.events.Events;
import jams.worldwind.events.Observer;
import jams.worldwind.shapefile.JamsShapeAttributes;
import jams.worldwind.test.IntervallCalculation;
import jams.worldwind.test.RandomNumbers;
import jams.worldwind.ui.ColorRamp;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Year;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reg.DataTransfer;

/**
 *
 * @author Ronny Berndt <ronny.showAttributeTableButtonerndt@uni-jena.de>
 */
public class GlobeView implements PropertyChangeListener, MessageListener {

    //<editor-fold desc="variables definition">
    private static final Logger logger = LoggerFactory.getLogger(GlobeView.class);
    //reference to jams,worldwind.ui.model.Globe
    private final Globe theGlobeModel = Globe.getInstance();
    private final JFrame theFrame;
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private final LayerListView theLayerView;
    private ScreenSelector theScreenSelector;
    private ShapefileAttributesView sAV;
    private SelectionHighlightController theSelectionHighlightController;
    private HighlightController highlightController;

    //Components of View
    private JPanel topPanel;
    private JSlider opacitySlider;
    private JSlider timeSeriesSlider;
    private JComboBox activeLayerComboBox;
    private JToggleButton selectObjectsToggleButton;
    private JButton showAttributeTableButton;

    private HashMap<Object, TimeSeries> objectsTimeData;

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="constructors">
    /**
     *
     */
    public GlobeView() {
        //this.theGlobeModel = Globe.getInstance();
        Observer.getInstance().addPropertyChangeListener(this);
        this.theLayerView = new LayerListView();
        //this.theGlobeModel.addPropertyChangeListener(theLayerView);
        theScreenSelector = new ScreenSelector(theGlobeModel.getWorldWindow());
        //theSelectionHighlightController = new SelectionHighlightController(theGlobeModel.getWorldWindow(), theScreenSelector);
        this.theScreenSelector.addMessageListener(this);
        //this.highlightController = new HighlightController(this.theGlobeModel.getWorldWindow(), SelectEvent.ROLLOVER);

        this.theFrame = new JFrame("JAMS WORLDWIND VIEWER");
        this.theFrame.setLayout(new BorderLayout());
        this.theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.theFrame.add((Component) theGlobeModel.getWorldWindow(), BorderLayout.CENTER);
        this.theFrame.add((Component) theGlobeModel.getStatusBar(), BorderLayout.PAGE_END);

        sAV = ShapefileAttributesView.getInstance();

        this.theGlobeModel.getWorldWindow().addSelectListener(new SelectListener() {
            protected Object lastObject;

            @Override
            public void selected(SelectEvent event) {
                Object o = event.getTopObject();
                if (lastObject != o && o != null) {
                    lastObject = o;
                    if (o instanceof SurfacePolygons) {
                        SurfacePolygons s = (SurfacePolygons) o;

                        s.setHighlighted(true);
                        ((SurfacePolygons) lastObject).setHighlighted(false);
                        sAV.getInstance().scrollToObject(o);

                        /*         
                         //System.out.println(s.getEntries().toString());
                         JamsShapeAttributes bs = (JamsShapeAttributes) s.getAttributes();
                         ShapefileRecord record = bs.getShapeAttributes();
                         Set<Map.Entry<String, Object>> c = record.getAttributes().getEntries();
                         for (Map.Entry<String, Object> e : c) {
                         System.out.println(e.getKey() +":" +e.getValue());
                         }*/
                    }
                }
            }
        });

        objectsTimeData = new HashMap<>();

        this.buildToolBar();
        this.buildMenu();
        this.fixMacOSX();
        this.setKeyboardShortcuts();

        File file = new File("../../JAMSworldwind/shapefiles/vg2500_geo84/vg2500_bld.shp");
        //File file = new File("../../../jamsmodelsdata/JAMS-Gehlberg/input/local/gis/hrus/hrus.shp");
        this.theGlobeModel.addShapefile(file);

        //zoom to region after loading
      /*
         double[] br = new Shapefile(file).getBoundingRectangle();
         Sector s = new Sector(Angle.fromDegrees(br[0]),
         Angle.fromDegrees(br[1]),
         Angle.fromDegrees(br[2]),
         Angle.fromDegrees(br[3]));
         this.zoomToSector(s);
         */
    }
    //</editor-fold>

    private void setKeyboardShortcuts() {
        JComponent c = (JComponent) theFrame.getRootPane();
        InputMap im = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "F1");
        am.put("F1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (topPanel.isVisible()) {
                    topPanel.setVisible(false);
                } else {
                    topPanel.setVisible(true);
                }
                theFrame.revalidate();
            }
        });

    }

    //<editor-fold defaultstate="collapsed" desc="build GUI functions">
    private void buildToolBar() {

        //this.theToolBar = new JToolBar("TOOLBAR");
        Icon smallIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/16x16/draw-rectangle-2.png"));
//        final Icon largeIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/22x22/draw-rectangle.png"));

        selectObjectsToggleButton = new JToggleButton(smallIcon);
        selectObjectsToggleButton.setToolTipText("SELECT OBJECTS");
        selectObjectsToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    theScreenSelector.enable();
                } else {
                    theScreenSelector.disable();
                }
            }
        });
        //this.theToolBar.add(selectObjectsToggleButton);
        //this.theToolBar.addSeparator();

        smallIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/16x16/table-link.png"));
        showAttributeTableButton = new JButton(smallIcon);
        showAttributeTableButton.setEnabled(false);
        //b.setText("OBJECTTABLE");
        //b.putClientProperty("TABLE", "OBJECTTABLE");
        showAttributeTableButton.setToolTipText("SHOW OBJECT TABLE");

        showAttributeTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //SimpleFeatureLayer sfl = Globe.getInstance().getModel().getLayers().get(theGlobeModel.getModel().getLayers().size()-1);
                List<?> list = theScreenSelector.getSelectedObjects();
                System.out.println("SELECTED OBJECTS COUNT: " + list.size());
                if (!list.isEmpty()) {
                    sAV.fillTableWithObjects(list);
                    sAV.show(true);
                    theFrame.toFront();
                }
            }

        });
        //this.theToolBar.add(showAttributeTableButton);

        //active Layer
        activeLayerComboBox = new JComboBox();
        activeLayerComboBox.setSelectedIndex(-1);
        activeLayerComboBox.setName("ACTIVE_LAYER_COMBOBOX");
        activeLayerComboBox.setBorder(new TitledBorder("ACTIVE LAYER"));
        //comboBox.setEnabled(false);
        //comboBox.setMaximumSize(new Dimension(150, 30));
        activeLayerComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                int index = ((JComboBox) e.getSource()).getSelectedIndex();
                if (e.getStateChange() == ItemEvent.SELECTED && index >= 0) {
                    JComboBox cb = (JComboBox) e.getSource();
                    theLayerView.setActiveLayerIndex(index);
                    showAttributeTableButton.setEnabled(true);
                    Layer l = theGlobeModel.getModel().getLayers().getLayerByName(cb.getSelectedItem().toString());
                    //System.out.println("OPACITY: " + l.getOpacity() * 100);
                    //create test time data
                    if (l.getClass().equals(RenderableLayer.class)) {
                        Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
                        if (objectsTimeData.isEmpty()) {
                            for (Object o : list) {
                                objectsTimeData.put(o, createTimeData());
                            }
                        }
                        System.out.println("END INIT");
                        Object first = list.iterator().next();
                        Collection periods = objectsTimeData.get(first).getTimePeriods();
                        LinkedList llist = new LinkedList(periods);

                        //System.out.println("PERIOD: " + objectsTimeData.get(first).getTimePeriods());

                        int fyear = ((Year) llist.getFirst()).getYear();
                        timeSeriesSlider.setMinimum(fyear);
                        int lyear = ((Year) llist.getLast()).getYear();
                        timeSeriesSlider.setMaximum(lyear);
                        
                        timeSeriesSlider.setMinorTickSpacing(1);
                        //System.out.println("Major: " + (lyear - fyear / llist.size() / 5));
                        timeSeriesSlider.setMajorTickSpacing(5);
                        timeSeriesSlider.setPaintTicks(true);
                        timeSeriesSlider.setSnapToTicks(true);
                        timeSeriesSlider.setPaintLabels(true);
                        timeSeriesSlider.setEnabled(true);

                    }
                    //if (l instanceof RenderableLayer) {
                    /*
                     Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
                     ArrayList alist = new ArrayList();

                     for (Object o : list) {
                        
                     alist.add(o);
                     }
                     sAV.fillTableWithObjects(alist);
                     sAV.show(true);
                     theFrame.toFront();
                     //}
                     */

                    opacitySlider.setEnabled(true);
                    timeSeriesSlider.setEnabled(true);
                    opacitySlider.setValue((int) (l.getOpacity() * 100));

                    Observer.getInstance().getPCS().firePropertyChange(Events.ACTIVE_LAYER_CHANGED, null, index);

                } else {
                    opacitySlider.setEnabled(false);
                    timeSeriesSlider.setEnabled(false);
                    showAttributeTableButton.setEnabled(false);
                }
            }
        });

        opacitySlider = new JSlider(0, 100);
        opacitySlider.setBorder(new TitledBorder("ACTIVE LAYER OPACITY"));
        opacitySlider.setName("ACTIVE_LAYER_OPACITY_SLIDER");
        opacitySlider.setPaintTicks(true);
        //opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setMajorTickSpacing(25);
        Hashtable labelTable = new Hashtable(5);
        labelTable.put(new Integer(0), new JLabel("0%"));
        labelTable.put(new Integer(25), new JLabel("25%"));
        labelTable.put(new Integer(50), new JLabel("50%"));
        labelTable.put(new Integer(75), new JLabel("75%"));
        labelTable.put(new Integer(100), new JLabel("100%"));
        opacitySlider.setLabelTable(labelTable);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setEnabled(false);
        //slider.setSnapToTicks(true);

        opacitySlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) ((JSlider) e.getSource()).getValue() / 100.0;
                /*Component[] comp = topPanel.getComponents();
                 for (Component o : comp) {
                 if (o instanceof JComboBox) {
                 JComboBox c = (JComboBox) o;*/
                Layer l = theGlobeModel.getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
                System.out.println(l.getValues());
                System.out.println(l.getName());
                //if a renderable is found, opacity must be set for every Renderable
                if (l.getClass().equals(RenderableLayer.class)) {
                    Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
                    for (Object obj : list) {
                        AbstractSurfaceShape shape = (AbstractSurfaceShape) obj;
                        ShapeAttributes shapeAttr = shape.getAttributes();
                        shapeAttr.setInteriorOpacity(value);
                        shapeAttr.setOutlineOpacity(value);
                    }
                } else {
                    l.setOpacity(value);
                }

                Globe.getInstance().getWorldWindow().redraw();
                /*}

                 }
                 */
                System.out.println("V: " + value);
            }
        });

        timeSeriesSlider = new JSlider();
        timeSeriesSlider.setBorder(new TitledBorder("TIME SERIES DATA"));
        timeSeriesSlider.setName("TIME_SERIES_SLIDER");
        timeSeriesSlider.setEnabled(false);
        timeSeriesSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double year = (double) ((JSlider) e.getSource()).getValue();
                //System.out.println("TIME: " + year);
                Layer l = theGlobeModel.getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
                //if a renderable is found, opacity must be set for every Renderable

                if (l.getClass().equals(RenderableLayer.class)) {
                    Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();

                    ArrayList al = new ArrayList();
                    double tempmin = Double.MAX_VALUE;
                    double tempmax = Double.MIN_VALUE;

                    for (TimeSeries t : objectsTimeData.values()) {
                        for (int j = 0; j < t.getItemCount(); j++) {
                            TimeSeriesDataItem di = t.getDataItem(j);
                            al.add(di.getValue().doubleValue());
                            //System.out.print("Y: " + di.getPeriod().toString() + " " + di.getValue().doubleValue() + " ");
                        }
                        //System.out.println();

                        if (tempmin > t.getMinY()) {
                            tempmin = t.getMinY();
                        }
                        if (tempmax < t.getMaxY()) {
                            tempmax = t.getMaxY();
                        }
                    }

                    IntervallCalculation iC = new IntervallCalculation(al);
                    List intervall = iC.getEqualIntervall(tempmin, tempmax, 10);

                    //System.out.println(intervall);
                    //System.out.println("allmin" + iC.getMinimumValue() + "|" + tempmin);
                    //System.out.println("allmax" + iC.getMaximumValue() + "|" + tempmax);
                    ColorRamp cR = new ColorRamp(Color.red, Color.blue, intervall.size());

                    //color objects
                    for (Object obj : list) {
                        SurfacePolygons o = (SurfacePolygons) obj;
                        JamsShapeAttributes sattr = (JamsShapeAttributes) o.getAttributes();
                        Number value = objectsTimeData.get(obj).getValue(new Year((int) year));
                        double d = value.doubleValue();
                        int index = iC.getIntervallIndex(intervall, d);
                        //System.out.println("OBJ: " + obj.toString() + "|" + " D: " + d + "|" + " I:" + index);
                        sattr.setInteriorMaterial(new Material(cR.getColor(index)));

                        /* 
                         AbstractSurfaceShape shape = (AbstractSurfaceShape) obj;
                         ShapeAttributes shapeAttr = shape.getAttributes();
                         shapeAttr.setInteriorOpacity(value);
                         shapeAttr.setOutlineOpacity(value);
                         */
                    }
                }

                Globe.getInstance().getWorldWindow().redraw();
            }
        });

        this.topPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        this.topPanel.setLayout(gbl);
        this.topPanel.setVisible(true);

        this.addComponent(topPanel, gbl, selectObjectsToggleButton, 0, 0, 1, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, showAttributeTableButton, 2, 0, 1, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, activeLayerComboBox, 3, 0, 1, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, opacitySlider, 0, 1, 1, 2, 1.0, 1.0);
        this.addComponent(topPanel, gbl, timeSeriesSlider, 1, 1, 2, 1, 1.0, 1.0);
        this.theFrame.add(topPanel, BorderLayout.NORTH);

        //comboBox.setEnabled(true);
        //this.theToolBar.add(activeLayerComboBox);
        //this.theFrame.add(this.theToolBar, BorderLayout.PAGE_START);
    }

    private TimeSeries createTimeData() {
        int maxNumbers = 11;
        TimeSeries timeData = new TimeSeries("TEST PRECIPITATION");
        for (int i = 0; i < maxNumbers; i++) {
            RandomNumbers rn = new RandomNumbers(0, 100, maxNumbers);
            Double value = (Double) rn.getDoubleValues().get(i);
            timeData.add(new Year(2000 + i), value);
            //System.out.print("Y: " + (2000 + i) + " " + value + " ");
        }
        //System.out.println();

        return timeData;
    }
    
    public void addData(DataTransfer data) {
        double[][] d = data.getData();
        for (double[] d1 : d) {
            for (int j = 0; j < d1.length; j++) {
                System.out.print(d1[j]+ " ");
            }
            System.out.println();
        }
        
    }
    
    public void addTimeData() {
        
    }

    private void addComponent(Container container,
            GridBagLayout gbl,
            Component c,
            int x, int y,
            int width, int height,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        container.add(c);
    }

    private void buildMenu() {
        this.theMenuBar = new JMenuBar();

        //FILE menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem openShapefile = new JMenuItem("Open Shapefile...");
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        openShapefile.setAccelerator(stroke);
        openShapefile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openShapefileActionlistener(e);
            }
        });

        JMenuItem jMenuItemExit = new JMenuItem("Exit");
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        jMenuItemExit.setAccelerator(stroke);
        jMenuItemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitActionListener(e);
            }
        });

        fileMenu.add(openShapefile);
        fileMenu.addSeparator();
        fileMenu.add(jMenuItemExit);

        //VIEW menu
        JMenu viewMenu = new JMenu("View");
        JMenu controls = new JMenu("View Controls");

        controls.add(new JCheckBoxMenuItem());

        JMenuItem listLayers = new JCheckBoxMenuItem("Show Layers");
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        listLayers.setAccelerator(stroke);
        listLayers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLayersActionListener(e);
            }
        });
        viewMenu.add(listLayers);

        this.theMenuBar.add(fileMenu);
        this.theMenuBar.add(viewMenu);

        this.theFrame.setJMenuBar(theMenuBar);

    }
    //</editor-fold>

    private void fillComboBox() {
        /*Component[] comp = this.topPanel.getComponents();
         for (Object o : comp) {
         if (o instanceof JComboBox) {
         JComboBox c = (JComboBox) o;*/
        activeLayerComboBox.removeAllItems();
        LayerList layers = this.theGlobeModel.getWorldWindow().getModel().getLayers();
        int index = 0;
        for (int i = 0; i < layers.size(); i++) {
            //if (layers.get(i).getClass() == RenderableLayer.class) {
            //use insertItemAt(...) to prevent fire of ItemListenerEvent
            activeLayerComboBox.insertItemAt(layers.get(i).getName(), index);
            index++;
            // }
        }
        activeLayerComboBox.setPreferredSize(new Dimension(100, 45));
        /*}

         }*/
    }

    // <editor-fold defaultstate="collapsed" desc="Menu-Action-Listeners"> 
    private void openShapefileActionlistener(ActionEvent e) {
        JFileChooser fc = new JFileChooser("/Users/bigr/Documents/BA-Arbeit/trunk/JAMSworldwind/shapefiles/JAMS-Kosi/hrus.shp");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("ESRI Shapefile", "shp");
        fc.setFileFilter(filter);

        switch (fc.showOpenDialog(this.theFrame)) {
            case JFileChooser.APPROVE_OPTION:
                File file = fc.getSelectedFile();
                if (isShapefileAlreadyOpened(file)) {
                    //Shapefile ist schon vorhanden
                    if (reloadShapefile(file)) {
                        //Shapefile neu laden
                        this.theGlobeModel.addShapefile(file);
                    }
                } else {
                    //Shapefile nicht vorhanden -> hinzufügen
                    this.theGlobeModel.addShapefile(file);
                }
                //zoom to region after loading
                double[] br = new Shapefile(file).getBoundingRectangle();
                Sector s = new Sector(Angle.fromDegrees(br[0]),
                        Angle.fromDegrees(br[1]),
                        Angle.fromDegrees(br[2]),
                        Angle.fromDegrees(br[3]));
                this.zoomToSector(s);
                break;
            default:
                break;
        }
    }

    private void exitActionListener(ActionEvent e) {
        for (Frame frame : JFrame.getFrames()) {
            if (frame.isActive()) {
                WindowEvent windowClosing = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
                frame.dispatchEvent(windowClosing);
            }
        }
    }

    private void showLayersActionListener(ActionEvent e) {
        if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
            this.theLayerView.show(true);
            this.theFrame.toFront();
        } else {
            this.theLayerView.show(false);
            this.theFrame.toFront();
        }
    }
    // </editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="zoom to region">
    /**
     *
     * @param sector
     * @return
     */
    public double computeZoomForExtent(Sector sector) {
        Angle delta = sector.getDeltaLat();
        if (sector.getDeltaLon().compareTo(delta) > 0) {
            delta = sector.getDeltaLon();
        }
        double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
        double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
        double ret = arcLength / (Math.tan(fieldOfView / 2.0));
        return ret;
    }

    /**
     *
     * @param sector
     */
    public void zoomToSector(Sector sector) {
        double zoom = computeZoomForExtent(sector);
        Position p = new Position(sector.getCentroid(), zoom);
        this.theGlobeModel.getWorldWindow().getView().stopAnimations();
        this.theGlobeModel.getWorldWindow().getView().goTo(p, zoom);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Shapefile loading">
    private boolean isShapefileAlreadyOpened(File f) {
        //Check if Shapefile already loaded
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        this.logger.info("Shapefile: " + layerName);
        Layer l = this.theGlobeModel.getWorldWindow().getModel().getLayers().getLayerByName(layerName);
        return l != null;
    }

    private boolean reloadShapefile(File f) {
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        Layer layer = this.theGlobeModel.getWorldWindow().getModel().getLayers().getLayerByName(layerName);
        int result = JOptionPane.showConfirmDialog(null,
                "Shapefile already loaded, reload?",
                "Shapefile exists",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.NO_OPTION) {
            //Shapefile nicht erneut laden
            return false;
        } else if (result == JOptionPane.YES_OPTION) {
            //remove old Shapefile layer
            this.theGlobeModel.getWorldWindow().getModel().getLayers().remove(layer);
        }
        return true;
    }
    //</editor-fold>

    /*public void addData(DataTransfer data) {

     System.out.println(data);
     }
     */
    //<editor-fold defaultstate="collapsed" desc="ui functions">
    /**
     *
     */
    public void show() {
        this.theFrame.setSize(1024, 800);
        this.theFrame.setLocationRelativeTo(null);
        this.theFrame.setVisible(true);
    }

    private void fixMacOSX() {
        //Fix Mac OS X UI Bug Java Version 7 Update 40
        if (Configuration.isMacOS()) {
            this.logger.info("Mac OS X detected - fixing view bug");
            this.theFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent ce) {
                    theFrame.setSize(theFrame.getSize().width + 1, theFrame.getSize().height + 1);
                }
            });
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PropertyChangeEvent">
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.logger.info("Recieving Event: " + evt.getPropertyName());
        if (evt.getPropertyName().equals(Events.LAYER_CHANGED)) {
            this.fillComboBox();
        }
    }
    //</editor-fold>

    @Override
    public void onMessage(Message msg) {
        if (msg.getName().equals(ScreenSelector.SELECTION_ENDED)) {
            if (!theScreenSelector.getSelectedObjects().isEmpty()) {
                showAttributeTableButton.setEnabled(true);
                /*Component[] comp = topPanel.getComponents();
                 for (Object o : comp) {
                 if (o instanceof JButton) {
                 JButton showAttributeTableButton = (JButton) o;
                 if (showAttributeTableButton.getClientProperty("TABLE").toString().compareTo("OBJECTTABLE") == 0) {
                 showAttributeTableButton.setEnabled(true);
                 }
                 }

                 }*/
            }

        } else if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)) {
            showAttributeTableButton.setEnabled(false);
            /*Component[] comp = topPanel.getComponents();
             for (Object o : comp) {
             if (o instanceof JButton) {
             JButton showAttributeTableButton = (JButton) o;
             if (showAttributeTableButton.getClientProperty("TABLE").toString().compareTo("OBJECTTABLE") == 0) {
             showAttributeTableButton.setEnabled(false);
             }
             }

             }*/
        }
    }
}
