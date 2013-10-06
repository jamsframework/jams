package jams.worldwind.ui.model;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;
import jams.worldwind.ui.UIEvents;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class GlobeModel implements UIEvents {

    //observe this class
    private PropertyChangeSupport changeSupport;
    private static final Logger logger = LoggerFactory.getLogger(GlobeModel.class);
    //The main Globe Window
    private WorldWindow wwd;
    private Model model;
    //Statusbar
    private StatusBar statusBar;

    public GlobeModel() {
        changeSupport = new PropertyChangeSupport(this);

        this.wwd = new WorldWindowGLCanvas();
        
        // Create the default model as described in the current worldwind properties.
        this.model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(this.model);

        this.statusBar = new StatusBar();
        this.statusBar.setEventSource(wwd);
    }

    public WorldWindow getWorldWindow() {
        return wwd;
    }
    
    public Model getModel() {
        return this.model;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void addShapefile(File f, String layerName) {
        List<Layer> layers = new ShapefileLoader().createLayersFromSource(f);

        for (Layer l : layers) {
            l.setName(layerName);
            insertBeforePlacenames(l);
        }
        changeSupport.firePropertyChange(UIEvents.LAYER_CHANGE, null, null);

    }

    public void insertBeforePlacenames(Layer layer) {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.removePropertyChangeListener(pcl);
    }
}
