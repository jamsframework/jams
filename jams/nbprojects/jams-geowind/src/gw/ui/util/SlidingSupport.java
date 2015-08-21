/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import gw.api.Events;
import java.awt.CardLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Orientation;

/** Sliding Panel support.
 * 
 * @author od
 */
public class SlidingSupport {

    private List<JToggleButton> l = new ArrayList<JToggleButton>();
    private JXCollapsiblePane pane = new JXCollapsiblePane(Orientation.VERTICAL);
    private CardLayout cl = new CardLayout();
    private JPanel p = new JPanel(cl);

    public SlidingSupport(boolean animated) {
        pane.add(p);
        pane.setCollapsed(true);
        pane.setAnimated(animated);
    }

    public void remove(String name, JComponent c) {
        p.remove(c);
        JToggleButton t = null;
        for (JToggleButton tb : l) {
            if (tb.getText().equals(name)) {
                t = tb;
            }
        }
        if (t != null) {
            l.remove(t);
        }
    }

    public void add(final String name, final JToggleButton b, final JComponent c) {
        p.add(c, name);
        l.add(b);
        b.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (JToggleButton tb : l) {
                        if (tb == b) {
                            continue;
                        }
                        if (tb.isSelected()) {
                            tb.setSelected(false);
                            break;
                        }
                    }
                    cl.show(p, name);
                    c.firePropertyChange(Events.PANEL_SHOW, 0d, 1d);
                }
                pane.setCollapsed(e.getStateChange() == ItemEvent.DESELECTED);
            }
        });
    }

    public JToggleButton add(String name, JComponent c) {
        JToggleButton b = new JToggleButton(name);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setMargin(new Insets(1, 3, 1, 3));
        add(name, b, c);
        return b;
    }

    public JComponent getPane() {
        return pane;
    }
}
