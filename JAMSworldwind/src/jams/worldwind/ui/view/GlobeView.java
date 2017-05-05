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
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
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
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import gov.nasa.worldwindx.examples.util.ScreenShotAction;
import jams.JAMS;
import jams.JAMSLogging;
import jams.data.JAMSCalendar;
import jams.tools.JAMSTools;
import jams.workspace.stores.ShapeFileDataStore;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.handler.SelectionHighlightController;
import jams.worldwind.events.Events;
import jams.worldwind.data.shapefile.JamsShapefileLoader;
import jams.worldwind.data.RandomNumbers;
import jams.worldwind.data.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.ColorRamp;
import java.awt.BorderLayout;
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
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class GlobeView implements PropertyChangeListener, MessageListener {

    private static GlobeView instance;

    public synchronized static GlobeView getInstance() {
        if (instance == null) {
            instance = new GlobeView();
            instance.internalStart();
            instance.theFrame.addWindowListener(new WindowListener() {
                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    LayerList list = instance.getModel().getLayers();
                    list.remove(list.size() - 1);
                    instance = null;
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {
                }

                @Override
                public void windowOpened(WindowEvent e) {
                }
            });

            instance.theFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.theFrame.setSize(1024, 800);
            instance.theFrame.setLocationRelativeTo(null);

        }
        return instance;
    }

    //<editor-fold desc="variables definition">
    private static final Logger logger = Logger.getLogger(GlobeView.class.getName());
    //reference to jams,worldwind.ui.model.Globe
    //private Globe theGlobeModel = Globe.getInstance();

    //The main Globe Window
    private final WorldWindowGLCanvas window;
    private final Model model;
    //Statusbar
    private final StatusBar statusBar;

    private PropertyChangeSupport pcs;

    private JFrame theFrame;
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private LayerListView theLayerView;
    private IntervallSettingsView intervallView;
    private ScreenSelector theScreenSelector;
    private ShapefileAttributesView shapefileAttributesView;
    private SelectionHighlightController theSelectionHighlightController;
    private HighlightController highlightController;

    //Listener
    SelectListener selectListener;

    //Components of View
    private JPanel topPanel;
    private JSlider internalOpacitySlider, outlineOpacitySlider, exaggSlider;
    private JSlider timeSeriesSlider;
    private JLabel timeSeriesSliderLabel;
    private JComboBox<String> activeLayerComboBox;
    private DisabledItemsComboBox attributesComboBox;
    private JToggleButton selectObjectsToggleButton;
    private JButton showAttributeTableButton;
    private JButton classifyButton;
    private JTextField delayEdit;
    private Timer playTimer;
    private ActionListener playTaskPerformer;

    private DataTransfer3D data;

    //temporary fields -- getting data from other frames
    private List<Double> intervall;
    private ColorRamp colorRamp;

    //saves the calculated intervalls for attrib i
    private List[] intervallCollection;
    //saves the calculated ColorRamp for attrib i
    private ColorRamp[] colorRampCollection;

    private int lastClassifiedIndex = -1;

    private Map<SurfacePolygons, ShapeAttributeView> attributeDlgs = new HashMap();

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

    private void fillAttributesComboBox(int selectedIndex) {

        ItemListener[] listeners = attributesComboBox.getItemListeners();
        for (ItemListener il : listeners) {
            attributesComboBox.removeItemListener(il);
        }

        this.attributesComboBox.removeAllItems();
        String[] attributes = data.getSortedAttributes();
        if (attributes != null) {
            int i = 0;
            for (String s : attributes) {

                if (this.intervallCollection == null || this.colorRampCollection == null
                        || this.intervallCollection[i] == null || this.colorRampCollection[i] == null) {
                    this.attributesComboBox.addItem(s, true);
                } else {
                    this.attributesComboBox.addItem(s);
                }
                i++;
            }
            this.attributesComboBox.setSelectedIndex(selectedIndex);
            //this.attributesComboBox.setEnabled(true);
            //this.classifyButton.setEnabled(true);
        }

        for (ItemListener il : listeners) {
            attributesComboBox.addItemListener(il);
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

        this.theFrame = new JFrame("JAMS WorldWind");
        this.theFrame.setIconImages(JAMSTools.getJAMSIcons());
        this.theFrame.setLayout(new BorderLayout());
        this.theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.theFrame.add((Component) getWorldWindow(), BorderLayout.CENTER);
        this.theFrame.add((Component) getStatusBar(), BorderLayout.PAGE_END);
        this.theFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                if (shapefileAttributesView != null) {
                    shapefileAttributesView.dispose();
                    shapefileAttributesView = null;
                }
                if (theLayerView != null) {
                    theLayerView.dispose();
                    theLayerView = null;
                }
                if (intervallView != null) {
                    intervallView.dispose();
                    intervallView = null;
                }
                if (intervallCollection != null) {
                    intervallCollection = null;
                }
                if (colorRampCollection != null) {
                    colorRampCollection = null;
                }
                removeListener();
            }

        });

        getPCS().addPropertyChangeListener(this.shapefileAttributesView);

        createListener();

        getWorldWindow().addSelectListener(selectListener);

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

    private void createListener() {
        selectListener = new SelectListener() {
            protected Object lastObject;

            @Override
            public void selected(SelectEvent event) {

                if (!event.isLeftClick()) {
                    return;
                }

                Object o = event.getTopObject();

//                if (lastObject != o && o != null) {
                if (o != null) {
//                    lastObject = o;
                    if (o instanceof SurfacePolygons) {
                        SurfacePolygons s = (SurfacePolygons) o;

                        s.setHighlighted(true);

//                        ((SurfacePolygons) lastObject).setHighlighted(false);
                        if (shapefileAttributesView != null && shapefileAttributesView.isVisible()) {
                            shapefileAttributesView.scrollToObject(o);
                        }

                        Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());

                        //only for Shapefile layers
                        if (l.getClass().equals(RenderableLayer.class)) {

                            DataTransfer3D dataTransfer = (DataTransfer3D) l.getValue(Events.DATATRANSFER3DDATA_APPEND);
                            Integer timeIndex = timeSeriesSlider.getValue();
                            JAMSCalendar date = dataTransfer.getSortedTimeSteps()[timeIndex];

                            String[][] values = getEntityData(s, date, dataTransfer);

                            ShapeAttributeView dlg = attributeDlgs.get(s);
                            if (dlg != null && dlg.isFocusable()) {
                                dlg.setDataTransfer(dataTransfer);
                                dlg.setData(values);
                                dlg.setVisible(true);
                                dlg.toFront();
                            } else {
                                dlg = new ShapeAttributeView(theFrame, "Entity " + values[0][1] + ": Information", values, dataTransfer);
                                dlg.setPreferredSize(new Dimension(250, 300));
                                dlg.pack();
                                attributeDlgs.put(s, dlg);
                                dlg.setVisible(true);
                            }
                        }
                    }
                }
            }
        };
    }

    private String[][] getEntityData(SurfacePolygons s, JAMSCalendar date, DataTransfer3D dataTransfer) {

        String column = dataTransfer.getKeyColumn();
        JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
        DBaseRecord dbrec = sattr.getShapeFileRecord().getAttributes();
        String idString = dbrec.getValue(column).toString();
        Set<Map.Entry<String, Object>> c = dbrec.getEntries();

//                            System.out.println(dates[timeIndex].toString());
        String values[][] = new String[dataTransfer.getSortedAttributes().length + c.size() + 2][2];
        values[0][0] = "ID";
        values[0][1] = idString;
        values[1][0] = "time";
        values[1][1] = date.toString();
        //System.out.println("ID : " + idString);
        int i = 2;
        for (String attribute : dataTransfer.getSortedAttributes()) {
            double dataValue = dataTransfer.getValue(idString, attribute, date);
            values[i][0] = attribute;
            values[i][1] = Double.toString(dataValue);
            i++;
            //System.out.println(attribute + " : " + dataValue);
        }

        for (Map.Entry<String, Object> e : c) {
            values[i][0] = e.getKey();
            values[i][1] = String.valueOf(e.getValue());
            i++;
        }

        return values;
    }

    private void updateDlgs() {
        Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
        //only for Shapefile layers
        if (l.getClass().equals(RenderableLayer.class)) {

            DataTransfer3D dataTransfer = (DataTransfer3D) l.getValue(Events.DATATRANSFER3DDATA_APPEND);
            Integer timeIndex = timeSeriesSlider.getValue();
            JAMSCalendar date = dataTransfer.getSortedTimeSteps()[timeIndex];

            for (SurfacePolygons s : attributeDlgs.keySet()) {
                String[][] values = getEntityData(s, date, dataTransfer);
                ShapeAttributeView dlg = attributeDlgs.get(s);
                if (dlg != null && dlg.isFocusable()) {
                    dlg.setData(values);
                }
            }
        }
    }

    private void removeListener() {
        getWorldWindow().removeSelectListener(selectListener);
    }

    public boolean addJAMSExplorerData(DataTransfer3D d) {
        this.data = d;
        intervallCollection = null;
        colorRampCollection = null;
//        this.fillAttributesComboBox(-1);
//        createListener();
        timeSeriesSlider.setEnabled(false);
        timeSeriesSlider.setValue(0);
        attributesComboBox.setEnabled(false);
        classifyButton.setEnabled(false);

        if (intervallView != null) {
            intervallView.dispose();
            intervallView = null;
        }

        try {
            addData(d.getShapeFileDataStore());
        } catch (gov.nasa.worldwind.exception.WWRuntimeException wwrte) {
            JAMSLogging.registerLogger(JAMSLogging.LogOption.Show,
                    Logger.getLogger(GlobeView.class.getName()));
            Logger.getLogger(GlobeView.class.getName()).log(Level.WARNING,
                    "Cannot open Shapefile \"" + d.getShapeFileDataStore().getShapeFile().getAbsolutePath() + "\" due to unknown projection. Please correct!", wwrte);
            JAMSLogging.unregisterLogger(JAMSLogging.LogOption.Show,
                    Logger.getLogger(GlobeView.class.getName()));
            return false;
        }
        return true;

        //fillAttributesComboBox();
//        writeToDisk();
    }

    private void addData(ShapeFileDataStore shp) {
        File file = shp.getShapeFile();
        if (!isShapefileAlreadyOpened(file)) {
            addShapefile(file);
        } else {
            String layerName = file.getName() + " (" + file.getAbsolutePath() + ")";

            if (data != null) {
                Layer l = getModel().getLayers().getLayerByName(layerName);
                l.setValue(Events.DATATRANSFER3DDATA_APPEND, data);
            }
            intervallCollection = new List[this.data.getSortedAttributes().length];
            colorRampCollection = new ColorRamp[this.data.getSortedAttributes().length];
            activeLayerComboBox.setSelectedIndex(0);
            activeLayerComboBox.setSelectedItem(layerName);
        }
//        else {
//            removeShapefile(file);
//            addShapefile(file);
//        }
    }

    private void setKeyboardShortcuts() {
        JComponent c = (JComponent) theFrame.getRootPane();
        InputMap im = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "VK_M");
        am.put("VK_M", new AbstractAction() {
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
        showAttributeTableButton.setToolTipText("Show Attribute Table");

        showAttributeTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //SimpleFeatureLayer sfl = getModel().getLayers().get(theGlobeModel.getModel().getLayers().size()-1);
                List<?> screenselectorList = theScreenSelector.getSelectedObjects();
                String layerName = (String) activeLayerComboBox.getSelectedItem();
                //System.out.println("LAYERNAME: " + layerName);
                //System.out.println("SELECTED OBJECTS COUNT: " + screenselectorList.size());
                theScreenSelector.disable();
                selectObjectsToggleButton.setEnabled(false);
                if (shapefileAttributesView == null) {
                    shapefileAttributesView = new ShapefileAttributesView("Attributes");;
                }
                if (!screenselectorList.isEmpty()) {
                    shapefileAttributesView.fillTableWithObjects(screenselectorList);
                    shapefileAttributesView.show(true);
                    theFrame.toFront();
                } else if (layerName != null) {
                    Layer l = getModel().getLayers().getLayerByName(layerName);
                    if (l.getClass().equals(RenderableLayer.class)) {

                        List<Object> list = new ArrayList<>(((RenderableLayer) l).getNumRenderables());
                        for (Renderable r : ((RenderableLayer) l).getRenderables()) {
                            list.add(r);

                        }
                        shapefileAttributesView.fillTableWithObjects(list);
                        shapefileAttributesView.show(true);
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
        activeLayerComboBox.setBorder(new TitledBorder("Active Layer"));
        //activeLayerComboBox.setEnabled(false);
        //comboBox.setMaximumSize(new Dimension(150, 30));
        activeLayerComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                activeLayerComboBoxItemStateChanged(e);
            }
        });
        this.fillComboBox();

        internalOpacitySlider = new JSlider(0, 100);
        outlineOpacitySlider = new JSlider(0, 100);
//        opacitySlider.setBorder(new TitledBorder("ACTIVE LAYER OPACITY"));
//        opacitySlider.setName("ACTIVE_LAYER_OPACITY_SLIDER");
        internalOpacitySlider.setPaintTicks(false);
        outlineOpacitySlider.setPaintTicks(false);
        //opacitySlider.setMinorTickSpacing(5);
//        opacitySlider.setMajorTickSpacing(25);
//        Hashtable opacLabelTable = new Hashtable(5);
//        opacLabelTable.put(new Integer(0), new JLabel("0%"));
//        opacLabelTable.put(new Integer(25), new JLabel("25%"));
//        opacLabelTable.put(new Integer(50), new JLabel("50%"));
//        opacLabelTable.put(new Integer(75), new JLabel("75%"));
//        opacLabelTable.put(new Integer(100), new JLabel("100%"));
//        opacitySlider.setLabelTable(opacLabelTable);
        internalOpacitySlider.setPaintLabels(false);
        outlineOpacitySlider.setPaintLabels(false);
        internalOpacitySlider.setEnabled(false);
        outlineOpacitySlider.setEnabled(false);
//        slider.setSnapToTicks(true);

        internalOpacitySlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                internalOpacitySliderStateChanged(e);
            }
        });

        outlineOpacitySlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                outlineSliderStateChanged(e);
            }
        });

        exaggSlider = new JSlider(0, 60);
//        exaggSlider.setBorder(new TitledBorder("EXAGGERATION"));
//        exaggSlider.setName("EXAGG_SLIDER");
        exaggSlider.setPaintTicks(false);
//        opacitySlider.setMinorTickSpacing(5);
//        exaggSlider.setMajorTickSpacing(25);
        exaggSlider.setPaintLabels(false);
        exaggSlider.setEnabled(true);
//        exaggSlider.setSnapToTicks(false);
        exaggSlider.setValue((int) getWorldWindow().getSceneController().getVerticalExaggeration() * 10);

        exaggSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                double value = (double) ((JSlider) e.getSource()).getValue();
                getWorldWindow().getSceneController().setVerticalExaggeration(value / 10);

            }
        });

        JPanel sliderPanel1 = new JPanel();
        sliderPanel1.setLayout(new GridLayout(3, 1));
        sliderPanel1.setBorder(new TitledBorder("Opacity & Exaggeration"));
        sliderPanel1.setPreferredSize(new Dimension(100, 50));
//        JPanel opaPanel = new JPanel();
//        opaPanel.setLayout(new GridLayout(1, 2));
        sliderPanel1.add(internalOpacitySlider);
        sliderPanel1.add(outlineOpacitySlider);
//        sliderPanel1.add(opaPanel);
        sliderPanel1.add(exaggSlider);

        JPanel sliderPanel2 = new JPanel();
        sliderPanel2.setLayout(new GridLayout(2, 1));
        sliderPanel2.setBorder(new TitledBorder("Time Steps"));

        timeSeriesSlider = new JSlider();
        //timeSeriesSlider.setBorder(new TitledBorder("TIME SERIES DATA"));
        timeSeriesSlider.setName("TIME_SERIES_SLIDER");
        timeSeriesSlider.setEnabled(false);
        timeSeriesSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                timeSeriesSliderStateChanged(e);
            }
        });

        this.timeSeriesSliderLabel = new JLabel("No Data", JLabel.CENTER);
        this.timeSeriesSliderLabel.setEnabled(true);
        JButton playButton = new JButton(">");
        JButton pauseButton = new JButton("||");
        delayEdit = new JTextField("100");
        delayEdit.setToolTipText(JAMS.i18n("Time_in_milliseconds"));
        delayEdit.setPreferredSize(new Dimension((int) delayEdit.getPreferredSize().getWidth() * 3, (int) delayEdit.getPreferredSize().getHeight()));
        JPanel playPanel = new JPanel();
        playPanel.add(playButton);
        playPanel.add(delayEdit);
        playPanel.add(pauseButton);
        playPanel.add(timeSeriesSliderLabel);

        playTaskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (timeSeriesSlider.getValue() < timeSeriesSlider.getMaximum()) {
                            timeSeriesSlider.setValue(timeSeriesSlider.getValue() + 1);
                        } else {
                            timeSeriesSlider.setValue(timeSeriesSlider.getMinimum());
                        }
                    }
                });
            }
        };

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (playTimer != null) {
                    playTimer.stop();
                }
                int time = 0;
                try {
                    time = Integer.parseInt(delayEdit.getText());
                } catch (NumberFormatException nfe) {
                    return;
                }
                playTimer = new Timer(time, playTaskPerformer);
                playTimer.start();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playTimer.stop();
            }
        });

        sliderPanel2.add(timeSeriesSlider);
        sliderPanel2.add(playPanel);

        this.topPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        this.topPanel.setLayout(gbl);
        this.topPanel.setVisible(true);

        JPanel classifierPanel = new JPanel();
        classifierPanel.setLayout(new GridLayout(2, 1));
        classifierPanel.setBorder(new TitledBorder("Spatial Data"));

        this.attributesComboBox = new DisabledItemsComboBox();
        this.attributesComboBox.setEnabled(false);
        this.attributesComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                attributesComboBoxItemStateChanged(e);
            }
        });

        this.classifyButton = new JButton("Classify");
        this.classifyButton.setEnabled(false);

        this.classifyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                classifyButtonActionPerformed(e);
            }
        });

        classifierPanel.add(attributesComboBox);
        classifierPanel.add(classifyButton);

        this.addComponent(topPanel, gbl, selectObjectsToggleButton, 0, 0, 1, 1, 1.0, 0);
        this.addComponent(topPanel, gbl, showAttributeTableButton, 1, 0, 1, 1, 1.0, 0);
        this.addComponent(topPanel, gbl, activeLayerComboBox, 0, 1, 2, 1, 1.0, 0);

        //this.addComponent(topPanel, gbl, attributesComboBox, 0, 2, 2, 1, 1.0, 1.0);
        //this.addComponent(topPanel, gbl, classifyButton, 0, 3, 2, 1, 1.0, 1.0);
        this.addComponent(topPanel, gbl, classifierPanel, 2, 0, 2, 2, 1.0, 0);

        this.addComponent(topPanel, gbl, sliderPanel1, 4, 0, 2, 2, 1.0, 0);
//        this.addComponent(topPanel, gbl, exaggSlider, 4, 1, 2, 1, 1.0, 0);

        this.addComponent(topPanel, gbl, sliderPanel2, 6, 0, 2, 2, 1.0, 0);

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

        JMenuItem saveScreenshot = new JMenuItem("Save Screenshot...");
        saveScreenshot.addActionListener(new ScreenShotAction(getWorldWindow()));
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        saveScreenshot.setAccelerator(stroke);

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
        fileMenu.add(saveScreenshot);
        fileMenu.addSeparator();
        fileMenu.add(jMenuItemExit);

        //VIEW menu
        JMenu viewMenu = new JMenu("View");
        JMenu controls = new JMenu("View Controls");

        controls.add(new JCheckBoxMenuItem());

        JMenuItem listLayers = new JMenuItem("Show Layers");
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
        JFileChooser fc = new JFileChooser();
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
                    //Shapefile nicht vorhanden -> hinzufÃ¼gen
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
        if (theLayerView == null) {
            this.theLayerView = new LayerListView(theFrame);
            this.theLayerView.setLocationRelativeTo(theFrame);
        }
        this.theLayerView.setVisible(true);
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
        String layerName = f.getName() + " (" + f.getAbsolutePath() + ")";
        //Layer layer = new ShapefileLoader().createLayerFromShapefile(new Shapefile(f));
        Shapefile shp = new Shapefile(f);

        List<Layer> layers = new JamsShapefileLoader().
                createLayersFromShapefile(shp);

        int i = 0;
        for (Layer layer : layers) {
            layer.setEnabled(true);
            if (data != null) {
                layer.setValue(Events.DATATRANSFER3DDATA_APPEND, data);
            }

            layer.setValue(Events.BOUNDINGBOXOFSHAPEFILE, shp.getBoundingRectangle());

            if (i == 0) {
                layer.setName(layerName);
            } else {
                layer.setName(layerName + "_" + i);
            }
            i++;
            getModel().getLayers().add(layer);
        }
        getPCS().firePropertyChange(Events.LAYER_ADDED, null, null);
        getPCS().firePropertyChange(Events.LAYER_CHANGED, null, null);

        activeLayerComboBox.setSelectedIndex(activeLayerComboBox.getItemCount() - 1);
        outlineOpacitySlider.setValue(50);
//        theLayerView.setActiveLayerIndex(activeLayerComboBox.getItemCount() - 1);
//        new DelayZoom(shp.getBoundingRectangle()).execute();
    }

    /**
     *
     * @param f
     */
    public void removeShapefile(File f) {
        String layerName = f.getName() + " (" + f.getAbsolutePath() + ")";
        Layer layer = getWorldWindow().getModel().getLayers().getLayerByName(layerName);

        if (layer != null) {
            logger.info("Layer removed...");
            getModel().getLayers().remove(layer);
        }
        getPCS().firePropertyChange(Events.LAYER_CHANGED, null, null);
    }

    private boolean isShapefileAlreadyOpened(File f) {
        //Check if Shapefile already loaded
        String layerName = f.getName() + " (" + f.getAbsolutePath() + ")";
        this.logger.info("Shapefile: " + layerName);
        Layer l = getWorldWindow().getModel().getLayers().getLayerByName(layerName);
        return l != null;
    }

    private boolean reloadShapefile(File f) {
        String layerName = f.getName() + " (" + f.getAbsolutePath() + ")";
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
        this.theFrame.setVisible(true);
    }

    public void toTop() {
        this.theFrame.toFront();
        this.theFrame.repaint();
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
        if (intervallView == null) {

            intervallView = new IntervallSettingsView(data, attributes);
        } else if (this.attributesComboBox.getSelectedIndex() == lastClassifiedIndex) {
            intervallView.show();
        } else {
            intervallView = new IntervallSettingsView(data, attributes);
        }
        intervallView.show();
        lastClassifiedIndex = this.attributesComboBox.getSelectedIndex();

    }

    public void writeToDisk() {
        try {
            // Serialize data object to a file
            logger.info("Flushing dataset to disk...");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("../../JAMSworldwind/src/jams/worldwind/test/DataTransfer3DTestData.ser"));
            out.writeObject(data);
            out.close();

            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.close();

            // Get the bytes of the serialized object
            //byte[] buf = bos.toByteArray();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     *
     * @param fileName
     */
    public void readFromDisk(String fileName) {
        try {
            logger.info("Reading dataset from disk...");
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            data = (DataTransfer3D) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.severe(e.toString());
        }
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
        logger.info("Receiving Event: " + evt.getPropertyName());
        int selectedIndex = -1;
        if (evt.getPropertyName().equals(Events.INTERVALL_CALCULATED)
                || evt.getPropertyName().equals(Events.INTERVALL_COLORS_SET)) {

            if (this.intervallView != null) {
                selectedIndex = this.intervallView.getSelectedAttributeIndex();
            }
            if (this.intervallCollection == null) {
                this.intervallCollection = new List[this.data.getSortedAttributes().length];
            }
            if (this.colorRampCollection == null) {
                this.colorRampCollection = new ColorRamp[this.data.getSortedAttributes().length];
            }
        }
        switch (evt.getPropertyName()) {
            case Events.LAYER_CHANGED:
                this.fillComboBox();
                break;
            case Events.INTERVALL_CALCULATED:
                if (selectedIndex == -1) {
                    return;
                }
                this.intervall = (List<Double>) evt.getNewValue();
                this.intervallCollection[selectedIndex] = intervall;
                this.attributesComboBox.setSelectedIndex(selectedIndex);
                break;
            case Events.INTERVALL_COLORS_SET:
                if (selectedIndex == -1) {
                    return;
                }
                this.colorRamp = (ColorRamp) evt.getNewValue();
                this.colorRampCollection[selectedIndex] = colorRamp;
                break;
        }
        timeSeriesSlider.setEnabled(false);
        if (selectedIndex != -1) {
            if (this.intervallCollection[selectedIndex] != null
                    && this.colorRampCollection[selectedIndex] != null) {
                timeSeriesSlider.setEnabled(true);
//                timeSeriesSlider.setValue(0);
            }
        }
        fillAttributesComboBox(selectedIndex);
        timeSeriesSliderStateChanged(null);

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

    //<editor-fold defaultstate="collapsed" desc="ItemListener">
    private void attributesComboBoxItemStateChanged(ItemEvent e) {
        if (attributesComboBox.getSelectedIndex() != -1) {
            if (intervallView != null) {
                if (this.intervallCollection[this.attributesComboBox.getSelectedIndex()] == null
                        || this.colorRampCollection[this.attributesComboBox.getSelectedIndex()] == null) {
                    /*                JOptionPane.showMessageDialog(null,
                     "Please calculate intervall and colorramp for attribute (" + this.attributesComboBox.getItemAt(this.attributesComboBox.getSelectedIndex()) + ")",
                     "NO INTERVALL DATA",
                     JOptionPane.OK_OPTION);
                     */
                    this.timeSeriesSlider.setEnabled(false);
                } else {
                    this.timeSeriesSlider.setEnabled(true);
                    timeSeriesSliderStateChanged(null);
                }
            }
        }
    }

    private void activeLayerComboBoxItemStateChanged(ItemEvent e) {
        int index = ((JComboBox) e.getSource()).getSelectedIndex();

        if (e.getStateChange() == ItemEvent.SELECTED && index >= 0) {
            JComboBox cb = (JComboBox) e.getSource();

            Layer l = getModel().getLayers().getLayerByName(cb.getSelectedItem().toString());
            new DelayZoom((double[]) l.getValue(Events.BOUNDINGBOXOFSHAPEFILE), 2000).execute();

            if (l.getClass().equals(RenderableLayer.class)) {
                showAttributeTableButton.setEnabled(true);

                //is data from jams-explorer availible
                DataTransfer3D d = (DataTransfer3D) l.getValue(Events.DATATRANSFER3DDATA_APPEND);
                if (d != null) {
                    logger.info("DATATRANSFER3D found...");
                    fillAttributesComboBox(-1);
                    classifyButton.setEnabled(true);
                    attributesComboBox.setEnabled(true);
                } else {
                    logger.info("DATATRANSFER3D not found...");
                    return;
                }

                String[] ids = d.getSortedIds();
                String[] attr = d.getSortedAttributes();
                JAMSCalendar[] cal = d.getSortedTimeSteps();

                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>(cal.length);

                for (int i = 0; i < cal.length; i++) {
                    labelTable.put(new Integer(i), new JLabel(cal[i].toString()));
                    //System.out.println("TIME:"+cal[i].toString());
                }

                timeSeriesSlider.setLabelTable(labelTable);
                timeSeriesSlider.setMinimum(0);
                timeSeriesSlider.setMaximum(cal.length - 1);

                timeSeriesSlider.setMajorTickSpacing(1);
                timeSeriesSlider.setPaintTicks(true);
                timeSeriesSlider.setSnapToTicks(true);
                timeSeriesSlider.setPaintLabels(false);
                timeSeriesSlider.setEnabled(false);
                Integer value = timeSeriesSlider.getValue();
                timeSeriesSliderLabel.setText(((JLabel) timeSeriesSlider.getLabelTable().get(value)).getText());

            } else {
                showAttributeTableButton.setEnabled(false);
            }
            internalOpacitySlider.setEnabled(true);
            internalOpacitySlider.setValue((int) (l.getOpacity() * 100));
            outlineOpacitySlider.setEnabled(true);
//            outlineOpacitySlider.setValue((int) (l.getOpacity() * 100));
            getPCS().firePropertyChange(Events.ACTIVE_LAYER_CHANGED, null, index);

        } else {
            internalOpacitySlider.setEnabled(false);
            outlineOpacitySlider.setEnabled(false);
            timeSeriesSlider.setEnabled(false);
            showAttributeTableButton.setEnabled(false);
        }
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="ChangeListener">
    private void internalOpacitySliderStateChanged(ChangeEvent e) {
        double value = (double) ((JSlider) e.getSource()).getValue() / 100.0;
        /*Component[] comp = topPanel.getComponents();
         for (Component o : comp) {
         if (o instanceof JComboBox) {
         JComboBox c = (JComboBox) o;*/
        Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
        //if a renderable is found, opacity must be set for every Renderable
        if (l.getClass().equals(RenderableLayer.class)) {
            Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
            for (Object obj : list) {
                AbstractSurfaceShape shape = (AbstractSurfaceShape) obj;
                ShapeAttributes shapeAttr = shape.getAttributes();
                shapeAttr.setInteriorOpacity(value);
//                shapeAttr.setOutlineOpacity(value);
            }
        }
        l.setOpacity(value);

        getWorldWindow().redraw();
    }

    private void outlineSliderStateChanged(ChangeEvent e) {
        double value = (double) ((JSlider) e.getSource()).getValue() / 100.0;
        Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());
        //if a renderable is found, opacity must be set for every Renderable
        if (l.getClass().equals(RenderableLayer.class)) {
            Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
            for (Object obj : list) {
                AbstractSurfaceShape shape = (AbstractSurfaceShape) obj;
                ShapeAttributes shapeAttr = shape.getAttributes();
                shapeAttr.setOutlineOpacity(value);
            }
        }
//        l.setOpacity(value);

        getWorldWindow().redraw();
    }

    private void timeSeriesSliderStateChanged(ChangeEvent e) {
        //System.out.println("TIME: " + year);
        int selectedIndex = this.attributesComboBox.getSelectedIndex();

        if (timeSeriesSlider.isEnabled() && selectedIndex != -1) {
            Layer l = getModel().getLayers().getLayerByName(activeLayerComboBox.getSelectedItem().toString());

            if (l == null) {
                return;
            }
            //only for Shapefile layers
            if (l.getClass().equals(RenderableLayer.class)) {
                Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();

                List<Double> intervall_temp = new ArrayList<>(intervallCollection[selectedIndex]);
                colorRamp = colorRampCollection[selectedIndex];
                DataTransfer3D d = (DataTransfer3D) l.getValue(Events.DATATRANSFER3DDATA_APPEND);
                JAMSCalendar[] dates = d.getSortedTimeSteps();

                Integer value = timeSeriesSlider.getValue();
                timeSeriesSliderLabel.setText(dates[value].toString());

                String column = d.getKeyColumn();
                SurfacePolygons poly = null;
                JamsShapeAttributes sattr = null;
                DBaseRecord record = null;

                //color objects
                for (Object obj : list) {
                    poly = (SurfacePolygons) obj;
                    sattr = (JamsShapeAttributes) poly.getAttributes();
                    record = sattr.getShapeFileRecord().getAttributes();

                    //System.out.println("VALUE    :"+value);
                    //System.out.println("RECORD-ID:" + record.getValue(column).toString());
                    //System.out.println("ATTRIB   :" + attributesComboBox.getSelectedItem().toString());
                    //System.out.println("DATE     :" + dates[value]);
                    double dataValue = d.getValue(record.getValue(column).toString(), attributesComboBox.getSelectedItem().toString(), dates[value]);

                    //System.out.println("DATA     :"+ dataValue);
                    if (dataValue != Double.NEGATIVE_INFINITY) {
                        int index = 0;
                        for (int j = 0; j < intervall_temp.size() - 1; j++) {
                            if (dataValue >= intervall_temp.get(j) && dataValue < intervall_temp.get(j + 1)) {
                                index = j;
                                break;
                            }
                            if (dataValue == intervall_temp.get(intervall_temp.size() - 1)) {
                                index = intervall_temp.size() - 1;
                                break;
                            }
                        }
                        sattr.setInteriorMaterial(new Material(colorRamp.getColor(index)));
                    }
                }
            }
            getWorldWindow().redraw();
            updateDlgs();
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

    private class DisabledItemsComboBox extends JComboBox {

        public DisabledItemsComboBox() {
            super();
            super.setRenderer(new DisabledItemsRenderer());
        }

        private Set disabled_items = new HashSet();

        public void addItem(Object anObject, boolean disabled) {
            try {
                super.addItem(anObject);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (disabled) {
                disabled_items.add(getItemCount() - 1);
            }
        }

        @Override
        public void removeAllItems() {
            super.removeAllItems();
            disabled_items = new HashSet();
        }

        @Override
        public void removeItemAt(final int anIndex) {
            super.removeItemAt(anIndex);
            disabled_items.remove(anIndex);
        }

        @Override
        public void removeItem(final Object anObject) {
            for (int i = 0; i < getItemCount(); i++) {
                if (getItemAt(i) == anObject) {
                    disabled_items.remove(i);
                }
            }
            super.removeItem(anObject);
        }

        @Override
        public void setSelectedIndex(int index) {
            if (!disabled_items.contains(index)) {
                super.setSelectedIndex(index);
            }
        }

        private class DisabledItemsRenderer extends BasicComboBoxRenderer {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                if (disabled_items.contains(index)) {
                    setBackground(list.getBackground());
                    setForeground(UIManager.getColor("Label.disabledForeground"));
                }
                setFont(list.getFont());
                setText((value == null) ? "" : value.toString());
                return this;
            }
        }
    }

//</editor-fold>
}
