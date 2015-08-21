package jams.worldwind.handler;

import gov.nasa.worldwind.layers.Layer;
import jams.worldwind.ui.model.LayerListModel;
import jams.worldwind.ui.view.LayerListView;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class LayerListItemTransferHandler extends TransferHandler {

    private static final Logger logger = LoggerFactory.getLogger(LayerListView.class);
    LayerListModel model;

    /**
     *
     * @param m
     */
    public LayerListItemTransferHandler(LayerListModel m) {
        this.model = m;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return (support.getComponent() instanceof JList) && support.isDataFlavorSupported(LayerListItemTransferable.LAYER_LIST_ITEM_DATA_FLAVOR);
    }

    @Override
    public boolean importData(TransferSupport support) {
        boolean accept = false;
        if (canImport(support)) {
            try {
                Transferable t = support.getTransferable();
                Object value = t.getTransferData(LayerListItemTransferable.LAYER_LIST_ITEM_DATA_FLAVOR);
                if (value instanceof Layer) {
                    JList list = (JList) support.getComponent();
                    int indexOld = model.indexOf(value);
                    int indexNew = list.getDropLocation().getIndex();
                    boolean increment = (indexNew >= indexOld);
                    Layer oldValue = model.elementAt(indexOld);
                    if (increment) {
                        model.insertElementAt(oldValue, indexNew);
                        model.removeElementAt(indexOld);
                    } else {
                        model.removeElementAt(indexOld);
                        model.insertElementAt(oldValue, indexNew);
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                logger.info(ex.getMessage());
            }
        }
        return accept;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        Transferable t = null;
        if (c instanceof JList) {
            JList list = (JList) c;
            Object value = list.getSelectedValue();
            if (value instanceof Layer) {
                Layer li = (Layer) value;
                t = new LayerListItemTransferable(li);
            }
        }
        return t;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        //Update WorldWind window
        logger.info("Update WorldWind Layers");
        model.updateWorldWind();
    }
}
