/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.handler;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.MessageListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class SelectionHighlightController extends HighlightController implements MessageListener {

    /**
     *
     */
    protected ScreenSelector screenSelector;

    /**
     *
     */
    protected List<Highlightable> lastBoxHighlightObjects = new ArrayList<>();

    /**
     *
     * @param wwd
     * @param screenSelector
     */
    public SelectionHighlightController(WorldWindow wwd, ScreenSelector screenSelector) {
        super(wwd, SelectEvent.ROLLOVER);

        this.screenSelector = screenSelector;
        this.screenSelector.addMessageListener(this);
    }

    /**
     *
     */
    @Override
    public void dispose() {
        super.dispose();

        this.screenSelector.removeMessageListener(this);
    }

    @Override
    public void onMessage(Message msg) {
        try {
            // Update the list of highlighted objects whenever the ScreenSelector's selection changes. We capture
            // both the selection started and selection changed events to ensure that we clear the list of selected
            // objects when the selection begins or re-starts, as well as update the list when it changes.
            if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)
                    || msg.getName().equals(ScreenSelector.SELECTION_CHANGED)) {
                this.highlightSelectedObjects(this.screenSelector.getSelectedObjects());
            }
        } catch (Exception e) {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    /**
     *
     * @param o
     */
    @Override
    protected void highlight(Object o) {
        // Determine if the highlighted object under the cursor has changed, but should remain highlighted because
        // its in the selection box. In this case we assign the highlighted object under the cursor to null and
        // return, and thereby avoid changing the highlight state of objects still highlighted by the selection box.
        if (this.lastHighlightObject != o && this.lastBoxHighlightObjects.contains(this.lastHighlightObject)) {
            this.lastHighlightObject = null;
            return;
        }

        super.highlight(o);
    }

    /**
     *
     * @param list
     */
    protected void highlightSelectedObjects(List<?> list) {
        if (this.lastBoxHighlightObjects.equals(list)) {
            return; // same thing selected
        }
        // Turn off highlight for the last set of selected objects, if any. Since one of these objects may still be
        // highlighted due to a cursor rollover, we detect that object and avoid changing its highlight state.
        for (Highlightable h : this.lastBoxHighlightObjects) {
            if (h != this.lastHighlightObject) {
                h.setHighlighted(false);
            }
        }
        this.lastBoxHighlightObjects.clear();

        if (list != null) {
            // Turn on highlight if object selected.
            for (Object o : list) {
                if (o instanceof Highlightable) {
                    //((Highlightable) o).setHighlighted(true);
                    this.lastBoxHighlightObjects.add((Highlightable) o);
                }
            }
        }

        // We've potentially changed the highlight state of one or more objects. Request that the world window
        // redraw itself in order to refresh these object's display. This is necessary because changes in the
        // objects in the pick rectangle do not necessarily correspond to mouse movements. For example, the pick
        // rectangle may be cleared when the user releases the mouse button at the end of a drag. In this case,
        // there's no mouse movement to cause an automatic redraw.
        this.wwd.redraw();
    }

}
