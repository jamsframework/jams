package jams.worldwind.ui.model;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
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

    public void addShapefile(File f) {
        String layerName = f.getName()+"|"+f.getAbsolutePath();
        //Layer layer = new ShapefileLoader().createLayerFromShapefile(new Shapefile(f));
        Layer layer = new JamsShapefileLoader().createLayerFromShapefile(new Shapefile(f));
        layer.setName(layerName);
        model.getLayers().add(layer);
        
        /*List<Layer> layers = new ShapefileLoader().createLayersFromShapefile(new Shapefile(f));
        //insert Layer
        for (Layer l : layers) {
            l.setName(layerName);
            model.getLayers().add(l);
        }*/
        double[] r1 = new Shapefile(f).getBoundingRectangle();
        ((BasicOrbitView) wwd.getView()).addPanToAnimator(
                                // The elevation component of 'targetPos' here is not the surface elevation,
                                // so we ignore it when specifying the view center position.
                                new Position(LatLon.fromDegrees(r1[0] + (r1[1] - r1[0]) / 2, r1[2] + (r1[3] - r1[2]) / 2), 0),
                                Angle.ZERO, Angle.ZERO, 400000, 1000, false);
        changeSupport.firePropertyChange(UIEvents.LAYER_CHANGE, null, null);
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
