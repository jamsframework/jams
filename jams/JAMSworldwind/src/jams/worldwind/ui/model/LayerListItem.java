/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.model;

import gov.nasa.worldwind.layers.Layer;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class LayerListItem {

    private String label;
    private boolean isSelected = false;

    /**
     *
     * @param label
     */
    public LayerListItem(String label) {
        this.label = label;
    }
    
    /**
     *
     * @param label
     * @param isSelected
     */
    public LayerListItem(String label, boolean isSelected) {
        this.label = label;
        this.isSelected = isSelected;
    }

    /**
     *
     * @return
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     *
     * @param isSelected
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return label;
    }
}
