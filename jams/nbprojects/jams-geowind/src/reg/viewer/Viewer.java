package reg.viewer;

import gw.GeoWind;
import java.awt.Dimension;
import gw.ui.FancyPanel;
import gw.ui.BottomPanel;
import gw.ui.IdColumnDialog;
import gw.ui.layerproperies.ShapefileLayerProperties;
import gw.ui.util.OvalBorder;
import jams.tools.StringTools;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.jdesktop.swingx.StackLayout;
import reg.DataTransfer;
import reg.tools.Tools;

/**
 *
 * @author hbusch
 *
 * main class to view geowind with regionalized data
 * this is a singleton class
 *
 */
public class Viewer {

    /**
     * the singleton instance
     */
    private static Viewer theInstance;
    /**
     * the main panel
     */
    private FancyPanel mainPanel;
    /**
     * the overall frame
     */
    private final JFrame frame = new JFrame();
    private boolean initialized = false;
    /**
     * zwischenspeicher
     */
    private DataTransfer dataTransfer = null;

    /**
     * creates the one instance and starts the viewer
     * @return the viewer
     */
    public static Viewer getViewer() {
        if (theInstance == null) {
            theInstance = new Viewer();
            try {
                theInstance.internalStart();
            } catch (Exception e) {
                System.out.println("Exception at running viewer: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return theInstance;
    }

    public static void destroy() {
        if (theInstance != null) {
            theInstance.initialized = false;
            theInstance.frame.dispose();
            // make sure a new instance is created next time!
            theInstance = null;
        }
    }

    /**
     * add columns to working shapeFile
     * @param theDataTransfer
     * @throws java.lang.Exception
     */
    public void addData(DataTransfer theDataTransfer)
            throws Exception {

        // wake up
        if (!frame.isVisible()) {
            frame.setVisible(true);
        }

        ShapefileLayerProperties layerProperties = null;
        // get working shape file from data transfer
        String layerName = theDataTransfer.getParentName();
        if (!StringTools.isEmptyString(layerName)) {
            layerProperties = this.mainPanel.getShapeLayer(layerName);
            if (layerProperties == null) {
                System.out.println("kein Layer " + layerName + " im Modell gefunden.");
            } else {
                System.out.println("Layer " + layerName + " im Modell gefunden.");
                this.mainPanel.setWorkLayerName(layerName);
            }
        }

        // not found -> create it
        if (layerProperties == null) {
            URI shapeURI = theDataTransfer.getParentURI();
            if (shapeURI != null) {
                File shapeFile = new File(shapeURI);
                this.mainPanel.addWorkShape(shapeFile);
                layerName = this.mainPanel.getWorkLayerName();
                System.out.println("Layer " + layerName + " im Modell angelegt.");
            }
        }
        this.dataTransfer = theDataTransfer;

        // add data after all (has to do with ShapefileLayerProperties.setupAttrData()
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {

                    ShapefileLayerProperties layerProperties = mainPanel.getWorkShapeFileProperties();
                    if (layerProperties == null) {
                        System.out.println("no working shape file found.");
                    } else {
                        String[] columnNames = getColumnNames(layerProperties);
                        int idColumn = -1;
                        String targetKeyName = dataTransfer.getTargetKeyName();
                        System.out.println("Viewer.addData. targetKeyName=" + targetKeyName);
                        if (targetKeyName != null) {
                            if (columnNames.length > 0) {
                                System.out.println("Viewer.addData. search for column " + targetKeyName);
                                for (int i = 0; i < columnNames.length; i++) {
                                    if (columnNames[i].equals(targetKeyName)) {
                                        idColumn = i;
                                        // System.out.println("Viewer.addData. found idColumn=" + idColumn);
                                        layerProperties.setIdColumn(idColumn);
                                        break;
                                    }
                                }
                            }
                        } else {
                            idColumn = layerProperties.getIdColumn();
                        }
                        System.out.println("Viewer.addData. idColumn=" + idColumn);
                        if (idColumn <= 0) {
                            if (columnNames.length > 0) {
                                IdColumnDialog dialog = new IdColumnDialog(getFrame(), true, columnNames);
                                dialog.setVisible(true);
                                if (dialog.getReturnStatus() == IdColumnDialog.RET_OK) {
                                    idColumn = dialog.getIdColumn();
                                }
                                dialog.dispose();
                                System.out.println("Viewer.addData. idColumn=" + idColumn);
                                layerProperties.setIdColumn(idColumn);
                            }
                        }
                        if (idColumn > 0) {
                            mainPanel.addWorkShapeFileColumn(dataTransfer);
                            System.out.println("fly to work layer.");
                            mainPanel.flyToWorkLayer();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }

    private String[] getColumnNames(ShapefileLayerProperties layerProperties) {

        String[] columnNames = null;
        boolean done = false;
        int max = 1;
        int act = 1;
        while (!done) {
            columnNames = layerProperties.getTableModel().getColumnNames();
            if (columnNames == null || columnNames.length == 0) {
                act++;
                if (act > max) {
                    System.out.println("giving up.");
                    done = true;
                } else {
                    System.out.println("waiting...");
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (Exception e) {
                    }
                }
            } else {
                done = true; // success
            }
        }
        return columnNames;
    }

    /**
     * get semaphore
     * @return initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * starts the viewer
     * @throws java.lang.Exception
     */
    private synchronized void internalStart()
            throws Exception {
        if (versionCheck()) {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
                    GeoWind.class.getResource("/gw/resources/Earth-16x16.png")));
            frame.setTitle(FancyPanel.TITLE);
//            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            frame.addWindowListener(new WindowListener() {

                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    // call destroy of window is closed
                    destroy();
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


            mainPanel = new FancyPanel(frame, getUnusedLayers(), getApplicationLayers());
            BottomPanel bp = new BottomPanel();

            JPanel pane = new JPanel();
            pane.setMinimumSize(new Dimension(300, 300));
            mainPanel.setBorder(new OvalBorder(2, 2));
            pane.setLayout(new StackLayout());

            pane.add(bp, StackLayout.TOP);
            pane.add(mainPanel, StackLayout.TOP);

            frame.getContentPane().add(pane);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setSize((int) (screenSize.width * .5), (int) (screenSize.height * .7));

            // Get the size of the screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

            // Determine the new location of the window
            int x = (dim.width - frame.getSize().width) / 2;
            int y = (dim.height - frame.getSize().height) / 2;

            // Move the window
            frame.setLocation(x, y);
            frame.setVisible(false);
            initialized = true;
        }
    }

    /**
     * check java version
     * @return result
     */
    private boolean versionCheck() {
        try {
            Class.forName("java.io.Console");
            return true;
        } catch (ClassNotFoundException E) {
            System.err.println("Error: Java 1.6+ required.");
            return false;
        }
    }

    /**
     * get the working shape file from property file
     * @return file or null
     */
    private File getWorkingShapeFile() {
        ResourceBundle resources = java.util.ResourceBundle.getBundle(Constants.RESOURCE_FILE);

        String shapeFileName = resources.getString(Constants.PROP_KEY_WORK_SHAPE);
        System.out.println("working shapeFile:" + shapeFileName);
        if (shapeFileName != null) {
            File shapeFile = new File(shapeFileName);
            if (shapeFile == null || !shapeFile.exists()) {
                System.out.println("shapeFile nicht gefunden.");
            } else {
                return shapeFile;
            }
        }
        return null;
    }

    /**
     * get all unused layers from property file
     * @return vector of layer names
     **/
    private Vector<String> getUnusedLayers() {
        return Tools.getPropertyGroup(Constants.RESOURCE_FILE, Constants.PROP_UNUSED_LAYERS);
    }

    private Vector<File> getApplicationLayers() {
        System.out.println("add some application layers coming from properties..");
        Vector<File> applicationLayers = new Vector<File>();
        Vector<String> applicationLayerNames = Tools.getPropertyGroup(Constants.RESOURCE_FILE, Constants.PROP_KEY_APPLICATION_LAYERS);
        for (String applicationLayerName : applicationLayerNames) {
            File layerFile = new File(applicationLayerName);
            if (layerFile == null || !layerFile.exists()) {
                System.out.println("layerFile " + applicationLayerName + " nicht gefunden.");
            } else {
                System.out.println("add layerFile " + applicationLayerName);
                applicationLayers.add(layerFile);
            }
        }
        return applicationLayers;
    }

    /**
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }
}
