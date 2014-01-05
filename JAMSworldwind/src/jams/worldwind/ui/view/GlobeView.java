package jams.worldwind.ui.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
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
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import jams.data.JAMSCalendar;
import jams.workspace.stores.ShapeFileDataStore;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.handler.SelectionHighlightController;
import jams.worldwind.events.Events;
import jams.worldwind.shapefile.JamsShapefileLoader;
import jams.worldwind.data.IntervallCalculation;
import jams.worldwind.data.RandomNumbers;
import jams.worldwind.ui.ColorRamp;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
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
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import reg.DataTransfer;

/**
 *
 * @author Ronny Berndt <ronny.showAttributeTableButtonerndt@uni-jena.de>
 */
public class GlobeView implements PropertyChangeListener, MessageListener {

    private static GlobeView instance;

    public synchronized static GlobeView getInstance() {
        if (instance == null) {
            instance = new GlobeView();
            instance.internalStart();
        }
        return instance;
    }

    //<editor-fold desc="variables definition">
    private static final Logger logger = LoggerFactory.getLogger(GlobeView.class);
    //reference to jams,worldwind.ui.model.Globe
    //private Globe theGlobeModel = Globe.getInstance();

    //The main Globe Window
    private WorldWindowGLCanvas window;
    private Model model;
    //Statusbar
    private StatusBar statusBar;

    private PropertyChangeSupport pcs;

    private JFrame theFrame;
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private LayerListView theLayerView;
    private ScreenSelector theScreenSelector;
    private ShapefileAttributesView sAV;
    private SelectionHighlightController theSelectionHighlightController;
    private HighlightController highlightController;

    //Components of View
    private JPanel topPanel;
    private JSlider opacitySlider;
    private JSlider timeSeriesSlider;
    private JLabel timeSeriesSliderLabel;
    private JComboBox<String> activeLayerComboBox;
    private JComboBox<String> attributesComboBox;
    private JToggleButton selectObjectsToggleButton;
    private JButton showAttributeTableButton;
    private JButton classifyButton;

    private DataTransfer3D data;

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="constructors">
    /**
     *
     * @param d
     */
    //private GlobeView(DataTransfer3D d, boolean reload) {
    //this(reload);
    //    this.data = d;
    //}
    private GlobeView() {
        this.window = new WorldWindowGLCanvas();
        // Create the default model as described in the current worldwind properties.
        this.model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.window.setModel(this.model);

        this.statusBar = new StatusBar();
        this.statusBar.setEventSource(window);
    }

    private void fillAttributeComboBox() {
        String[] attributes = data.getSortedAttributes();
        if (attributes != null) {
            for (String s : attributes) {
                this.attributesComboBox.addItem(s);
            }
            this.attributesComboBox.setSelectedIndex(0);
            this.attributesComboBox.setEnabled(true);
            this.classifyButton.setEnabled(true);
        }
    }

    /**
     *
     * @return
     */
    public WorldWindow getWorldWindow() {
        return this.window;
    }

    /**
     *
     * @return
     */
    public Model getModel() {
        return this.model;
    }

    /**
     *
     * @return
     */
    public View getView() {
        return this.window.getView();
    }

    /**
     *
     * @return
     */
    public StatusBar getStatusBar() {
        return this.statusBar;
    }

    private void internalStart() {
        //if (reload) {
        //   logger.info("Model reload");
        //    reInit();
        //}

        this.pcs = new PropertyChangeSupport(this);
        //this.theGlobeModel = Globe.getInstance();
        getPCS().addPropertyChangeListener(this);

        //this.theGlobeModel.addPropertyChangeListener(theLayerView);
        this.theScreenSelector = new ScreenSelector(getWorldWindow());

        //this.theSelectionHighlightController = new SelectionHighlightController(theGlobeModel.getWorldWindow(), theScreenSelector);
        this.theScreenSelector.addMessageListener(this);
        this.highlightController = new HighlightController(getWorldWindow(), SelectEvent.ROLLOVER);

        this.theFrame = new JFrame("JAMS WORLDWIND VIEWER");
        this.theFrame.setLayout(new BorderLayout());
        this.theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.theFrame.add((Component) getWorldWindow(), BorderLayout.CENTER);
        this.theFrame.add((Component) getStatusBar(), BorderLayout.PAGE_END);
        this.theFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                if (sAV != null) {
                    sAV.show(false);
                }
                if (theLayerView != null) {
                    theLayerView.show(false);
                }
            }

        });

        sAV = new ShapefileAttributesView("ATTRIBUTESTABLE OF ACTIVE LAYER");;
        getPCS().addPropertyChangeListener(this.sAV);

        getWorldWindow().addSelectListener(new SelectListener() {
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
                        sAV.scrollToObject(o);

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

        this.buildToolBar();
        this.buildMenu();
        this.fixMacOSX();
        this.setKeyboardShortcuts();

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

    public void addJAMSExplorerData(DataTransfer3D d, ShapeFileDataStore s) {
        this.data = d;
        addData(s);
        fillAttributeComboBox();
    }

    private void addData(ShapeFileDataStore shp) {
        File file = shp.getShapeFile();
        if (!isShapefileAlreadyOpened(file)) {
            addShapefile(file);
        }
    }

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
                //SimpleFeatureLayer sfl = getModel().getLayers().get(theGlobeModel.getModel().getLayers().size()-1);
                List<?> screenselectorList = theScreenSelector.getSelectedObjects();
                String layerName = (String) activeLayerComboBox.getSelectedItem();
                System.out.println("LAYERNAME: " + layerName);
                System.out.println("SELECTED OBJECTS COUNT: " + screenselectorList.size());
                theScreenSelector.disable();
                selectObjectsToggleButton.setEnabled(false);
                if (!screenselectorList.isEmpty()) {
                    sAV.fillTableWithObjects(screenselectorList);
                    sAV.show(true);
                    theFrame.toFront();
                } else if (layerName != null) {
                    Layer l = getModel().getLayers().getLayerByName(layerName);
                    if (l.getClass().equals(RenderableLayer.class)) {

                        List<Object> list = new ArrayList<>(((RenderableLayer) l).getNumRenderables());
                        for (Renderable r : ((RenderableLayer) l).getRenderables()) {
                            list.add(r);

                        }
                        sAV.fillTableWithObjects(list);
                        sAV.show(true);
                        theFrame.toFront();
                    }
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
                    //theLayerView.setActiveLayerIndex(index);

                    Layer l = getModel().getLayers().getLayerByName(cb.getSelectedItem().toString());
                    new DelayZoom((double[]) l.getValue(Events.BOUNDINGBOXOFSHAPEFILE), 2000).execute();

                    //System.out.println("OPACITY: " + l.getOpacity() * 100);
                    //create test time data
                    if (l.getClass().equals(RenderableLayer.class)) {
                        showAttributeTableButton.setEnabled(true);
                        //Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();

                        //is data from jams-explorer availible
                        DataTransfer3D d = (DataTransfer3D) l.getValue(Events.DATATRANSFER3DDATA_APPEND);
                        if (d == null) {
                            return;
                        }

                        String[] ids = d.getSortedIds();
                        String[] attr = d.getSortedAttributes();
                        JAMSCalendar[] cal = d.getSortedTimeSteps();

                        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>(cal.length);

                        for (int i = 0; i < cal.length; i++) {
                            labelTable.put(new Integer(i), new JLabel(cal[i].toString()));

                        }

                        timeSeriesSlider.setLabelTable(labelTable);
                        timeSeriesSlider.setMinimum(0);
                        timeSeriesSlider.setMaximum(cal.length - 1);

                        timeSeriesSlider.setMajorTickSpacing(1);
                        timeSeriesSlider.setPaintTicks(true);
                        timeSeriesSlider.setSnapToTicks(true);
                        timeSeriesSlider.setPaintLabels(false);
                        timeSeriesSlider.setValue(0);
                        timeSeriesSlider.setEnabled(true);

                    } else {
                        showAttributeTableButton.setEnabled(false);
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

                    getPCS().firePropertyChange(Events.ACTIVE_LAYER_CHANGED, null, index);

                } else {
                    opacitySlider.setEnabled(false);
                    timeSeriesSlider.setEnabled(false);
                    showAttributeTableButton.setEnabled(false);
                }
            }
        });
        this.fillComboBox();

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
                Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
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

                getWorldWindow().redraw();
                /*}

                 }
                 */
                System.out.println("V: " + value);
            }
        });

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(2, 1));
        sliderPanel.setBorder(new TitledBorder("TIME SERIES DATA"));

        timeSeriesSlider = new JSlider();
        //timeSeriesSlider.setBorder(new TitledBorder("TIME SERIES DATA"));
        timeSeriesSlider.setName("TIME_SERIES_SLIDER");
        timeSeriesSlider.setEnabled(false);
        timeSeriesSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Dictionary d = timeSeriesSlider.getLabelTable();
                Integer value = ((JSlider) e.getSource()).getValue();
                timeSeriesSliderLabel.setText(((JLabel) d.get((Object) value)).getText());

                double year = (double) ((JSlider) e.getSource()).getValue();
                //System.out.println("TIME: " + year);
                Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
                //if a renderable is found, opacity must be set for every Renderable

                if (l.getClass().equals(RenderableLayer.class)) {
                    Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();

                    ArrayList al = new ArrayList();
                    double tempmin = Double.MAX_VALUE;
                    double tempmax = Double.MIN_VALUE;
                    /*
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
                     */
                    IntervallCalculation iC = new IntervallCalculation(al);
                    List intervall = iC.getEqualIntervall(tempmin, tempmax, 10);

                    //System.out.println(intervall);
                    //System.out.println("allmin" + iC.getMinimumValue() + "|" + tempmin);
                    //System.out.println("allmax" + iC.getMaximumValue() + "|" + tempmax);
                    ColorRamp cR = new ColorRamp(Color.red, Color.blue, intervall.size());

                    //color objects
                    for (Object obj : list) {
                        /*
                         SurfacePolygons o = (SurfacePolygons) obj;
                         JamsShapeAttributes sattr = (JamsShapeAttributes) o.getAttributes();
                         Number value = objectsTimeData.get(obj).getValue(new Year((int) year));
                         double d = value.doubleValue();
                         int index = iC.getIntervallIndex(intervall, d);
                         //System.out.println("OBJ: " + obj.toString() + "|" + " D: " + d + "|" + " I:" + index);
                         sattr.setInteriorMaterial(new Material(cR.getColor(index)));
                         */
                        /* 
                         AbstractSurfaceShape shape = (AbstractSurfaceShape) obj;
                         ShapeAttributes shapeAttr = shape.getAttributes();
                         shapeAttr.setInteriorOpacity(value);
                         shapeAttr.setOutlineOpacity(value);
                         */
                    }
                }

                getWorldWindow().redraw();
            }
        });

        this.timeSeriesSliderLabel = new JLabel("NO DATA", JLabel.CENTER);
        this.timeSeriesSliderLabel.setEnabled(true);

        sliderPanel.add(timeSeriesSlider);
        sliderPanel.add(timeSeriesSliderLabel);

        this.topPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        this.topPanel.setLayout(gbl);
        this.topPanel.setVisible(true);

        JPanel classifierPanel = new JPanel();
        classifierPanel.setLayout(new GridLayout(2, 1));
        classifierPanel.setBorder(new TitledBorder("CLASSIFY DATA"));

        this.attributesComboBox = new JComboBox<>();
        this.attributesComboBox.setEnabled(false);

        this.classifyButton = new JButton("CLASSIFY");
        this.classifyButton.setEnabled(false);
        
        this.classifyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                classifyButtonActionPerformed(e);
            }
        });

        classifierPanel.add(attributesComboBox);
        classifierPanel.add(classifyButton);

        this.addComponent(topPanel, gbl, selectObjectsToggleButton, 0, 0, 1, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, showAttributeTableButton, 1, 0, 1, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, activeLayerComboBox, 0, 1, 2, 1, 1.0, 1.0);

        //this.addComponent(topPanel, gbl, attributesComboBox, 0, 2, 2, 1, 1.0, 1.0);
        //this.addComponent(topPanel, gbl, classifyButton, 0, 3, 2, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, classifierPanel, 2, 0, 2, 2, 1.0, 1.0);

        this.addComponent(topPanel, gbl, opacitySlider, 4, 0, 2, 2, 1.0, 1.0);
        this.addComponent(topPanel, gbl, sliderPanel, 6, 0, 2, 2, 1.0, 1.0);

        //this.addComponent(topPanel, gbl, timeSeriesSlider, 1, 1, 2, 1, 1.0, 1.0);
        this.theFrame.add(topPanel, BorderLayout.NORTH);

        //comboBox.setEnabled(true);
        //this.theToolBar.add(activeLayerComboBox);
        //this.theFrame.add(this.theToolBar, BorderLayout.PAGE_START);
    }

    //TODE REMOVE
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

    /*
     public void addData(DataTransfer data) {
     double[][] d = data.getData();
     for (double[] d1 : d) {
     for (int j = 0; j < d1.length; j++) {
     System.out.print(d1[j]+ " ");
     }
     System.out.println();
     }
        
     }
     */
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
        LayerList layers = getWorldWindow().getModel().getLayers();
        int index = 0;
        for (int i = 0; i < layers.size(); i++) {
            //if (layers.get(i).getClass() == RenderableLayer.class) {
            //use insertItemAt(...) to prevent fire of ItemListenerEvent
            activeLayerComboBox.insertItemAt(layers.get(i).getName(), index);
            index++;
            // }
        }
        activeLayerComboBox.setPreferredSize(new Dimension(100, 45));
        activeLayerComboBox.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (activeLayerComboBox.getSelectedIndex() > -1) {
                    activeLayerComboBox.setToolTipText(activeLayerComboBox.getSelectedItem().toString());
                }
            }
        });

        /*}

         }*/
    }

    // <editor-fold defaultstate="collapsed" desc="Menu-Action-Listeners"> 
    private void openShapefileActionlistener(ActionEvent e) {
        JFileChooser fc = new JFileChooser("/Users/bigr/Documents/BA-Arbeit/trunk/JAMSworldwind/shapefiles/J2000_Dudh-Kosi/input/local/gis/hrus/hrus.shp");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("ESRI Shapefile", "shp");
        fc.setFileFilter(filter);

        switch (fc.showOpenDialog(this.theFrame)) {
            case JFileChooser.APPROVE_OPTION:
                File file = fc.getSelectedFile();
                if (isShapefileAlreadyOpened(file)) {
                    //Shapefile ist schon vorhanden
                    if (reloadShapefile(file)) {
                        //Shapefile neu laden
                        addShapefile(file);
                    }
                } else {
                    //Shapefile nicht vorhanden -> hinzufügen
                    addShapefile(file);
                }
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
            //this.theLayerView = new LayerListView();
            this.theLayerView = new LayerListView();
            getPCS().addPropertyChangeListener(this.theLayerView);
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
        getWorldWindow().getView().stopAnimations();
        double zoom = computeZoomForExtent(sector);
        Position p = new Position(sector.getCentroid(), zoom);
        getWorldWindow().getView().goTo(p, zoom);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Shapefile loading">
    /**
     *
     * @param f
     */
    public void addShapefile(File f) {
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        //Layer layer = new ShapefileLoader().createLayerFromShapefile(new Shapefile(f));
        Shapefile shp = new Shapefile(f);
        System.out.println("SHAPEFILE RECORDS: " + shp.getNumberOfRecords());
        Layer layer = new JamsShapefileLoader().
                createLayerFromShapefile(shp);

        if (data != null) {
            layer.setValue(Events.DATATRANSFER3DDATA_APPEND, data);
        }

        layer.setValue(Events.BOUNDINGBOXOFSHAPEFILE, shp.getBoundingRectangle());
        layer.setName(layerName);
        getModel().getLayers().add(layer);
        getPCS().firePropertyChange(Events.LAYER_ADDED, null, null);
        getPCS().firePropertyChange(Events.LAYER_CHANGED, null, null);

        //activeLayerComboBox.setSelectedIndex(activeLayerComboBox.getItemCount() - 1);
        //theLayerView.setActiveLayerIndex(activeLayerComboBox.getItemCount() - 1);
        //new DelayZoom(shp.getBoundingRectangle()).execute();
    }

    private boolean isShapefileAlreadyOpened(File f) {
        //Check if Shapefile already loaded
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        this.logger.info("Shapefile: " + layerName);
        Layer l = getWorldWindow().getModel().getLayers().getLayerByName(layerName);
        return l != null;
    }

    private boolean reloadShapefile(File f) {
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        Layer layer = getWorldWindow().getModel().getLayers().getLayerByName(layerName);
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
            getWorldWindow().getModel().getLayers().remove(layer);
        }
        return true;
    }
    //</editor-fold>

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
    
    //<editor-fold defaultstate="collapsed" desc="ActionListener">
    
    private void classifyButtonActionPerformed(ActionEvent e) {
        String[] attributes = data.getSortedAttributes();
        
        IntervallSettingsView intervallView = new IntervallSettingsView(data, attributes);
    }
    
    
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="PropertyChangeEvent">
    public PropertyChangeSupport getPCS() {
        return pcs;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.logger.info("Recieving Event: " + evt.getPropertyName());
        if (evt.getPropertyName().equals(Events.LAYER_CHANGED)) {
            this.fillComboBox();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MessageListener">
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
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Helper Classes">
    
    private class DelayZoom extends SwingWorker<Object, Object> {

        private Sector sector = null;
        private final long millis;

        public DelayZoom(double[] boundingRectangle, long millis) {
            if (boundingRectangle != null) {
                this.sector = new Sector(Angle.fromDegrees(boundingRectangle[0]),
                        Angle.fromDegrees(boundingRectangle[1]),
                        Angle.fromDegrees(boundingRectangle[2]),
                        Angle.fromDegrees(boundingRectangle[3]));
            }
            this.millis = millis;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Thread.sleep(millis);
            return null;
        }

        @Override
        protected void done() {
            if (sector != null) {
                zoomToSector(sector);
            }
        }
    }
    //</editor-fold>
    
}
