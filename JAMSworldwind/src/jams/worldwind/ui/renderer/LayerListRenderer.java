package jams.worldwind.ui.renderer;

import gov.nasa.worldwind.layers.Layer;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class LayerListRenderer extends JCheckBox
        implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean hasFocus) {
        setEnabled(list.isEnabled());
        setSelected(((Layer) value).isEnabled());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setText(((Layer)value).getName());
        setToolTipText(((Layer)value).getName());
        return this;
    }
}

