/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.model;

import gov.nasa.worldwind.layers.Layer;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class LayerListItem {

    private String label;
    private boolean isSelected = false;

    public LayerListItem(String label) {
        this.label = label;
    }
    
    public LayerListItem(String label, boolean isSelected) {
        this.label = label;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return label;
    }
}
