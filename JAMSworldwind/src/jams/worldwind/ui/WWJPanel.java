/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.StatusBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class WWJPanel extends JPanel {

    private WorldWindow wwd;
    private StatusBar statusBar;

    public WWJPanel(Dimension size) {
        super(new BorderLayout());

        this.wwd = new WorldWindowGLCanvas();
        ((Component) this.wwd).setPreferredSize(size);
        
        // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
        //this.setMinimumSize(new Dimension(0, 0));

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);
        
        // Add the WorldWindow to this JPanel.
        this.add((Component) this.wwd, BorderLayout.CENTER);

        //create StatusBar
        this.statusBar = new StatusBar();
        this.add(statusBar, BorderLayout.PAGE_END);
        this.statusBar.setEventSource(wwd);
    }

    public WorldWindow getWwd() {
        return wwd;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }
}
