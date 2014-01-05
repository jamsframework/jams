package jams.worldwind.ui.model;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import javax.swing.DefaultListModel;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class LayerListModel extends DefaultListModel<Layer> {

    //sets the active Layer
    private int activeIndex;

    /**
     *
     */
    public LayerListModel() {
        this.activeIndex = -1;
         fillList();
    }

    private void fillList() {
        LayerList layers = Globe.getInstance().getWorldWindow().getModel().getLayers();
        for (int i = 0; i < layers.size(); i++) {
            this.add(i, layers.get(i));
        }
    }

    /**
     *
     */
    public void update() {
        this.clear();
        fillList();
    }

    /**
     *
     */
    public void updateWorldWind() {
        Globe.getInstance().getModel().getLayers().clear();
        LayerList newLayers = new LayerList();
        for (int i = 0; i < this.size(); i++) {
            
            newLayers.add(this.get(i));
            System.out.println(newLayers.get(i).getName());
        }
        Globe.getInstance().getModel().setLayers(newLayers);
        Globe.getInstance().getWorldWindow().redraw();
    }

    public Layer getActiveLayer() {
        return super.get(activeIndex);
    }

    public void setActiveLayer(int index) {
        if (index >= 0 && index < Globe.getInstance().getModel().getLayers().size()) {
            this.activeIndex = index;
        }
    }
}
