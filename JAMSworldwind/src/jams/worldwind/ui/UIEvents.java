/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public interface UIEvents {
    
    /**
     *
     */
    public static final String LAYER_CHANGE = "LayerChange";
    
    /**
     *
     * @param pcl
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     *
     * @param pcl
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);
    
}
