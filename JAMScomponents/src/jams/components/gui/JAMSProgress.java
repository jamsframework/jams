/*
 * JAMSProgress.java
 * Created on 1. Dezember 2005, 19:46
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.gui;

import jams.data.*;
import jams.model.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS simple progress bar",
        author="Sven Kralisch",
        date="1. December 2005",
        description="This visual component creates a small progress bar showing how many iterations " +
        "of the enclosing context already have passed.")
public class JAMSProgress extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Title string for progress bar"
            )
            public Attribute.String title;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Progress bar height"
            )
            public Attribute.Integer height;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Progress bar width"
            )
            public Attribute.Integer width;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Progress bar always on top?"
            )
            public Attribute.Boolean ontop;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Progress bar always on top?"
            )
            public Attribute.Integer counter;
    
    private JProgressBar jamsProgressBar;
    private JFrame frame;
    private JButton cancelButton;
    private Runnable updatePBar;
    //private int counter;
    
    public void init() {
        counter.setValue(0);
        
        /*try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {}*/
        
        frame = new JFrame();
        jamsProgressBar = new JProgressBar();
        
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(ontop.getValue());
        frame.setTitle(title.getValue());
        frame.setName("JAMSProgress");
        frame.setResizable(false);
        frame.setUndecorated(true);
        jamsProgressBar.setPreferredSize(new java.awt.Dimension(width.getValue(), height.getValue()));
        jamsProgressBar.setString(title.getValue());
        jamsProgressBar.setStringPainted(true);
        jamsProgressBar.setIndeterminate(false);
        frame.getContentPane().add(jamsProgressBar, BorderLayout.CENTER);
        
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        
        frame.getContentPane().add(cancelButton, java.awt.BorderLayout.EAST);
        frame.setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        frame.pack();
        
        frame.setLocation((int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()-frame.getWidth())/2, (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()-frame.getHeight())/2);
        jamsProgressBar.setMinimum(0);
        jamsProgressBar.setMaximum((int)this.getContext().getNumberOfIterations());
        jamsProgressBar.setValue(counter.getValue());
        
        updatePBar = new Runnable() {
            public void run() {
                int c = counter.getValue();                
                jamsProgressBar.setValue(++c);
                counter.setValue(c);
                //jamsProgressBar.setString(title.getValue() + " @ " + Math.round(jamsProgressBar.getPercentComplete()*100) + "%");
            }
        };
        
        frame.setVisible(true);
    }
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //super.fireNotification("User abort");
        getModel().getRuntime().sendHalt();
    }
    
    public void run() {
        SwingUtilities.invokeLater(updatePBar);
    }
    
    public void cleanup() {
        frame.dispose();
        System.out.println("aufrufe:" + this.counter.getValue());
    }
}
