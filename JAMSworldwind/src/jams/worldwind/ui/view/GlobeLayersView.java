/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.LayerPanel;
import java.awt.BorderLayout;
import javax.swing.JFrame;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class GlobeLayersView {
    
    private WorldWindow wwd;
    private JFrame frame;
    
    /**
     *
     * @param w
     */
    public GlobeLayersView(WorldWindow w) {
        this.wwd = w;
        
        frame = new JFrame("Availible Layers");
        
        
        frame.setLayout(new BorderLayout());
        frame.add(new LayerPanel(wwd));
        
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
}
