package jams.worldwind.ui.model;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import javax.swing.DefaultListModel;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class LayerListModel extends DefaultListModel<Layer> {

    private Globe globeModel;

    public LayerListModel() {
        this.globeModel = Globe.getInstance();
        fillList();
    }

    private void fillList() {
        LayerList layers = this.globeModel.getWorldWindow().getModel().getLayers();
        for(int i=0;i<layers.size();i++) {
            this.add(i,layers.get(i));
        }
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
