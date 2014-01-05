package jams.worldwind.ui.model;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.StatusBar;
import jams.worldwind.shapefile.JamsShapefileLoader;
import jams.worldwind.events.Events;
import jams.worldwind.events.Observer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class Globe {

    private static Globe instance;
    //observe this class
    //private PropertyChangeSupport changeSupport;
    private static final Logger logger = LoggerFactory.getLogger(Globe.class);
    //The main Globe Window
    private WorldWindowGLCanvas window;
    private Model model;
    //Statusbar
    private StatusBar statusBar;
    //Singleton pattern
    

    /**
     *
     * @return
     */
    /*public synchronized static Globe getInstance() {
        if (instance == null) {
            instance = new Globe();

        }
        return instance;
    }
    */
    public void reInit() {
        instance = new Globe();
    }
    
    private Globe() {
        //changeSupport = new PropertyChangeSupport(this);

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
}
