/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gw.ui.tree;


import gov.nasa.worldwind.layers.Layer;
import javax.swing.tree.DefaultMutableTreeNode;

public class LayerTreeNode extends DefaultMutableTreeNode {

    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
    protected int selectionMode;

    public LayerTreeNode() {
        this(null);
    }

    public LayerTreeNode(Object userObject) {
        this(userObject, true);
    }

    public LayerTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
        setSelectionMode(SINGLE_SELECTION);
    }

    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public Layer getLayer() {
        if(userObject instanceof Layer)
            return (Layer)userObject;
        return null;
    }

    public void setSelected(boolean isSelected) {
        if(getLayer() != null)
            getLayer().setEnabled(isSelected);
    }

    public boolean isSelected() {
        if(getLayer() == null)
            return false;
        return getLayer().isEnabled();
    }

    // If you want to change "isSelected" by CellEditor,
  /*
    public void setUserObject(Object obj) { if (obj instanceof Boolean) {
     * setSelected(((Boolean)obj).booleanValue()); } else {
     * super.setUserObject(obj); } }
     */
}