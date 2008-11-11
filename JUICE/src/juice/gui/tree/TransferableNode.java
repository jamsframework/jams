package juice.gui.tree;

import java.awt.datatransfer.*;
import java.util.*;

public class TransferableNode implements Transferable {
	public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");
	public static final DataFlavor EXPANDED_STATE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Vector");
	private JAMSNode node;
	private Vector expandedStates;
	private DataFlavor[] flavors = { NODE_FLAVOR, EXPANDED_STATE_FLAVOR };
	public TransferableNode(JAMSNode nd, Vector es) {
		node = nd;
		expandedStates = es;
	}
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor == NODE_FLAVOR) {
			return node;
		}
		else if (flavor == EXPANDED_STATE_FLAVOR) {
			return expandedStates;
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}
}
