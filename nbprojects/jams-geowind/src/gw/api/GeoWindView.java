/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.api;

import gov.nasa.worldwind.layers.Layer;

/** The GeoWind View
 *
 * @author od
 */
public interface GeoWindView {

    /**
     * Add a layer to the view
     * 
     * @param l the new Layer
     */
    public void addLayer(Layer l);

    /**
     * Remove a layer from the view.
     *
     * @param l the layer to remove.
     */
    public void removeLayer(Layer l);
}
