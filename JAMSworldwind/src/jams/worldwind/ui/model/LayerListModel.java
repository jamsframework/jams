/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.model;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import javax.swing.DefaultListModel;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class LayerListModel extends DefaultListModel<Layer> {

    private GlobeModel globeModel;

    public LayerListModel(GlobeModel gm) {
        this.globeModel = gm;
        fillList();
    }

    private void fillList() {
        LayerList layers = this.globeModel.getWorldWindow().getModel().getLayers();
        //System.out.println(layers);
        for(int i=0;i<layers.size();i++) {
            this.add(i,layers.get(i));
        }
        /* LayerListItem
        for (Layer l : layers) {
            
            LayerListItem lli = new LayerListItem(l.getName(), l.isEnabled());
            this.
            //this.add(lli);
            this.addElement(lli);
        }
        */
    }

    public void update() {
        this.clear();
        fillList();
    }

    public void updateWorldWind() {
        globeModel.getModel().getLayers().clear();
        LayerList newLayers = new LayerList();
        for(int i=0;i<this.size();i++) {
            newLayers.add(this.get(i));
        }
        globeModel.getModel().setLayers(newLayers);
        globeModel.getWorldWindow().redraw();
    }
}
