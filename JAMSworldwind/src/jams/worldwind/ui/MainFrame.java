/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui;

import gov.nasa.worldwind.Configuration;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class MainFrame extends JFrame {

    private JPanel wwPanel;

    public MainFrame() {

        wwPanel = new WWJPanel(new Dimension(800, 600));

        //horizontal split pane
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        horizontalSplitPane.setLeftComponent(null);
        horizontalSplitPane.setRightComponent(wwPanel);
        horizontalSplitPane.setOneTouchExpandable(true);

        // Create a panel for the bottom component of a vertical split-pane.
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("AttributeTable Panel");
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(label, BorderLayout.CENTER);
        
        //vertical split pane
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        verticalSplitPane.setTopComponent(horizontalSplitPane);
        verticalSplitPane.setBottomComponent(bottomPanel);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setResizeWeight(1);
                
        // Add the vertical split-pane to the frame.
        this.getContentPane().add(verticalSplitPane, BorderLayout.CENTER);
        
        //this.getContentPane().add(this.wwPanel, BorderLayout.CENTER);

        // Center the application on the screen.
        Dimension prefSize = this.getPreferredSize();
        Dimension parentSize;
        java.awt.Point parentLocation = new java.awt.Point(0, 0);
        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
        this.setLocation(x, y);
        this.setResizable(true);
        
        //Fix Mac OS X UI Bug
        getFrame().addComponentListener(new ComponentListener()
        {
            @Override
            public void componentResized(ComponentEvent ce) 
            {
            }

            @Override
            public void componentMoved(ComponentEvent ce) 
            {
            }

            @Override
            public void componentHidden(ComponentEvent ce) 
            {
            }

            @Override
            public void componentShown(ComponentEvent ce) 
            {
                JFrame frame=getFrame();
                frame.setSize(frame.getSize().width+1,frame.getSize().height+1);
            }
        });
    }

    public JFrame getFrame() {
        return this;
    }
    
    public static void main(String[] args) {

    }
}