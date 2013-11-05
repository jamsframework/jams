package jams.worldwind.ui.model;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.StatusBar;
import jams.worldwind.shapefile.JamsShapefileLoader;
import jams.worldwind.ui.UIEvents;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class Globe implements UIEvents {

    private static Globe instance;
    //observe this class
    private PropertyChangeSupport changeSupport;
    private static final Logger logger = LoggerFactory.getLogger(Globe.class);
    //The main Globe Window
    private WorldWindow window;
    private Model model;
    //Statusbar
    private StatusBar statusBar;

    //Singleton pattern

    /**
     *
     * @return
     */
        public synchronized static Globe getInstance() {
        if(instance == null) {
            instance = new Globe();
        }
        return instance;
    }

    private Globe() {
        changeSupport = new PropertyChangeSupport(this);

        this.window = new WorldWindowGLCanvas();

        // Create the default model as described in the current worldwind properties.
        this.model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.window.setModel(this.model);

        this.statusBar = new StatusBar();
        this.statusBar.setEventSource(window);
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

    /**
     *
     * @param f
     */
    public void addShapefile(File f) {
        String layerName = f.getName() + "|" + f.getAbsolutePath();
        //Layer layer = new ShapefileLoader().createLayerFromShapefile(new Shapefile(f));
        Layer layer = new JamsShapefileLoader().
                createLayerFromShapefile(new Shapefile(f));
        layer.setName(layerName);
        model.getLayers().add(layer);
        changeSupport.firePropertyChange(UIEvents.LAYER_CHANGE, null, null);
    }

    /**
     *
     * @param pcl
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    /**
     *
     * @param pcl
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.removePropertyChangeListener(pcl);
    }
}