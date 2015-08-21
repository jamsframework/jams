package jams.worldwind.handler;

import gov.nasa.worldwind.layers.Layer;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class LayerListItemTransferable implements Transferable {

    /**
     *
     */
    public static final DataFlavor LAYER_LIST_ITEM_DATA_FLAVOR = new DataFlavor(Layer.class, "gov/nasa/worldwind/layers/Layer");
    private final Layer item;
    
    /**
     *
     * @param l
     */
    public LayerListItemTransferable(Layer l) {
        this.item = l;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{LAYER_LIST_ITEM_DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(LAYER_LIST_ITEM_DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return this.item;
    }
    
}
