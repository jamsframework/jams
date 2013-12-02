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
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import jams.worldwind.handler.SelectionHighlightController;
import jams.worldwind.ui.model.Globe;
import jams.worldwind.events.Events;
import jams.worldwind.events.Observer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
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

        this.buildToolBar();
        this.buildMenu();
        this.fixMacOSX();

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

    //<editor-fold defaultstate="collapsed" desc="build GUI functions">
    private void buildToolBar() {
        this.theToolBar = new JToolBar("TOOLBAR");

        Icon smallIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/16x16/draw-rectangle-2.png"));
//        final Icon largeIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/22x22/draw-rectangle.png"));

        JToggleButton selectObjects = new JToggleButton(smallIcon);
        selectObjects.setToolTipText("SELECT OBJECTS");
        selectObjects.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    theScreenSelector.enable();
                } else {
                    theScreenSelector.disable();
                }
            }
        });
        this.theToolBar.add(selectObjects);
        this.theToolBar.addSeparator();

        smallIcon = new ImageIcon(GlobeView.class.getResource("/jams/worldwind/ressources/16x16/table-link.png"));
        JButton b = new JButton(smallIcon);
        b.setEnabled(false);
        //b.setText("OBJECTTABLE");
        b.putClientProperty("TABLE", "OBJECTTABLE");
        b.setToolTipText("SHOW OBJECT TABLE");

        b.addActionListener(new ActionListener() {
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
        this.theToolBar.add(b);

        //active Layer
        JComboBox<Layer> comboBox = new JComboBox();
        comboBox.setSelectedIndex(-1);
        //comboBox.setEnabled(false);
        comboBox.setMaximumSize(new Dimension(150, 25));
        comboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                int index = ((JComboBox) e.getSource()).getSelectedIndex();
                if (e.getStateChange() == ItemEvent.SELECTED && index >= 0) {
                    JComboBox cb = (JComboBox) e.getSource();
                    theLayerView.setActiveLayerIndex(index);
                    Layer l = theGlobeModel.getModel().getLayers().getLayerByName(cb.getSelectedItem().toString());
                    //if (l instanceof RenderableLayer) {
                        Iterable<Renderable> list = ((RenderableLayer) l).getRenderables();
                        ArrayList alist = new ArrayList();

                        for (Object o : list) {

                            alist.add(o);
                        }
                        sAV.fillTableWithObjects(alist);
                        sAV.show(true);
                        theFrame.toFront();
                    //}

                    Observer.getInstance().getPCS().firePropertyChange(Events.ACTIVE_LAYER_CHANGED, null, index);
                }
            }
        });

        //comboBox.setEnabled(true);
        this.theToolBar.add(comboBox);

        this.theFrame.add(this.theToolBar, BorderLayout.PAGE_START);
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
        stroke=KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
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
        Component[] comp = this.theToolBar.getComponents();
        for (Object o : comp) {
            if (o instanceof JComboBox) {
                JComboBox c = (JComboBox) o;
                c.removeAllItems();
                LayerList layers = this.theGlobeModel.getWorldWindow().getModel().getLayers();
                int index = 0;
                for (int i = 0; i < layers.size(); i++) {
                    if (layers.get(i).getClass() == RenderableLayer.class) {
                        //use insertItemAt(...) to prevent fire of ItemListenerEvent
                        c.insertItemAt(layers.get(i).getName(), index);
                        index++;
                    }
                }
                c.setPreferredSize(new Dimension(200, 25));
            }

        }
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
        if (l == null) {
            return false;
        } else {
            return true;
        }
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

    //<editor-fold defaultstate="collapsed" desc="ui functions">
    /**
     *
     */
    public void show() {
        this.theFrame.setSize(800, 600);
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
                Component[] comp = theToolBar.getComponents();
                for (Object o : comp) {
                    if (o instanceof JButton) {
                        JButton b = (JButton) o;
                        if (b.getClientProperty("TABLE").toString().compareTo("OBJECTTABLE") == 0) {
                            b.setEnabled(true);
                        }
                    }

                }
            }

        } else if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)) {
            Component[] comp = theToolBar.getComponents();
            for (Object o : comp) {
                if (o instanceof JButton) {
                    JButton b = (JButton) o;
                    if (b.getClientProperty("TABLE").toString().compareTo("OBJECTTABLE") == 0) {
                        b.setEnabled(false);
                    }
                }

            }
        }
    }
}
