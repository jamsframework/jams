package jams.worldwind.events;

import jams.worldwind.ui.model.Globe;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class Observer implements PropertyChangeListener {

    private static Observer instance;
    private final PropertyChangeSupport pcs;

    /**
     *
     * @return
     */
    public synchronized static Observer getInstance() {
        if (instance == null) {
            instance = new Observer();
        }
        return instance;
    }

    private Observer() {
        this.pcs = new PropertyChangeSupport(this);
        //register our Observer for notifications from WorldWind
        Globe.getInstance().getWorldWindow().addPropertyChangeListener(this);
    }
    
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
        System.out.println("GETTING MESSAGE FROM WW: " + evt.getPropertyName() + " OLD VALUE: " + evt.getOldValue() + " NEW VALUE: " + evt.getNewValue());
    }
}
