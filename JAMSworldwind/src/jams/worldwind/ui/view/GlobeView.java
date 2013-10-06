package jams.worldwind.ui.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.LayerPanel;
import jams.worldwind.ui.model.GlobeModel;
import jams.worldwind.ui.UIEvents;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class GlobeView implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(GlobeView.class);
    //reference to GlobeModel
    private GlobeModel globeModel;
    private JFrame viewingFrame;
    private JMenuBar menuBar;
    private LayerListView layerView;
    
    public GlobeView(GlobeModel gm) {
        viewingFrame = new JFrame("JAMS WorldWind Viewer");
        this.globeModel = gm;
        globeModel.addPropertyChangeListener(this);
        
        viewingFrame.setLayout(new BorderLayout());
        viewingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        viewingFrame.add((Component) globeModel.getWorldWindow(), BorderLayout.CENTER);
        viewingFrame.add((Component) globeModel.getStatusBar(), BorderLayout.PAGE_END);

        layerView = new LayerListView(this.globeModel);

        buildMenu();
        fixMacOSX();

    }

    private void buildMenu() {
        menuBar = new JMenuBar();

        //FILE menu
        JMenu fileMenu = new JMenu("File");
        JCheckBox jcb = new JCheckBox("Test");
        JMenuItem openShapefile = new JMenuItem("Open Shapefile...");
        openShapefile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser("/Users/bigr/Documents/BA-Arbeit/trunk/JAMSworldwind/shapefiles/JAMS-Kosi/hrus.shp");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("ESRI Shapefile", "shp");
                fc.setFileFilter(filter);

                switch (fc.showOpenDialog(viewingFrame)) {
                    case JFileChooser.APPROVE_OPTION:
                        File file = fc.getSelectedFile();
                        if (isShapefileAlreadyOpened(file)) {
                            //Shapefile ist schon vorhanden
                            if (reloadShapefile(file)) {
                                //Shapefile neu laden
                                globeModel.addShapefile(file);
                            }
                        } else {
                            //Shapefile nicht vorhanden -> hinzufügen
                            globeModel.addShapefile(file);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        JMenuItem jMenuItemExit = new JMenuItem("Exit");
        jMenuItemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Frame frame : JFrame.getFrames()) {
                    if (frame.isActive()) {
                        WindowEvent windowClosing = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
                        frame.dispatchEvent(windowClosing);
                    }
                }
            }
        });

        fileMenu.add(openShapefile);
        fileMenu.addSeparator();
        fileMenu.add(jMenuItemExit);

        //VIEW menu
        JMenu viewMenu = new JMenu("View");

        JMenuItem listLayers = new JCheckBoxMenuItem("List Layers");
         listLayers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                    layerView.show(true);
                    viewingFrame.toFront();
                    //layerPanel.update(globeModel.getWorldWindow());
                    //viewingFrame.getContentPane().add(layerPanel, BorderLayout.WEST);
                    //new LayerListView(globeModel);
                } else {
                    layerView.show(false);
                    viewingFrame.toFront();
                }
                //viewingFrame.revalidate();
                //viewingFrame.repaint();
            }
        });
        viewMenu.add(listLayers);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        viewingFrame.setJMenuBar(menuBar);
    }

    private boolean isShapefileAlreadyOpened(File f) {
        //Check if Shapefile already loaded
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        logger.info("Shapefile: " + layerName);
        Layer l = this.globeModel.getWorldWindow().getModel().getLayers().getLayerByName(layerName);
        if (l == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean reloadShapefile(File f) {
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        Layer layer = this.globeModel.getWorldWindow().getModel().getLayers().getLayerByName(layerName);
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
            this.globeModel.getWorldWindow().getModel().getLayers().remove(layer);
        }
        return true;
    }

    private void fixMacOSX() {
        //Fix Mac OS X UI Bug Java Version 7 Update 40
        if (Configuration.isMacOS()) {
            logger.info("Mac OS X detected - fixing view bug");
            viewingFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent ce) {
                    viewingFrame.setSize(viewingFrame.getSize().width + 1, viewingFrame.getSize().height + 1);
                }
            });
        }
    }

    public void show() {

        //viewingFrame.pack();
        viewingFrame.setSize(800, 600);
        viewingFrame.setLocationRelativeTo(null);
        viewingFrame.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.info("Recieving Event: " + evt.getPropertyName());
        if (evt.getPropertyName().equals(UIEvents.LAYER_CHANGE)) {
            layerView.updateLayerListView();
        }
    }
}
