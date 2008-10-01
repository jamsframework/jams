/*
 * JAMSSplash.java
 * Created on 5. April 2006, 08:52
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.jams.gui;

import java.awt.*;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author  S. Kralisch
 */
public class JAMSSplash extends JDialog {
    
    private Image img;
    
    public JAMSSplash() {
        this(0,0);
    }
    
    public JAMSSplash(int x, int y) {
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("JAMS");
        this.setUndecorated(true);
        this.setFocusable(false);
        this.setAlwaysOnTop(true);
        
        URL imgURL = ClassLoader.getSystemResource("resources/images/JAMSsplash.png");
        if (img == null && imgURL != null) {
            img = new ImageIcon(imgURL).getImage();//.getScaledInstance(x, y, Image.SCALE_SMOOTH);
        }
        if (x == 0) {
            x = img.getWidth(null);
            y = img.getHeight(null);
        } else {
            img = img.getScaledInstance(x, y, Image.SCALE_SMOOTH);
        }
        setSize(x, y);
        
        Dimension d2 = new java.awt.Dimension(x, y);
        this.setPreferredSize(d2);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d.width / 2 - x / 2, d.height / 2 - y / 2);
        
        
/*
        //wait 'till image has been loaded
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(img, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
 */
        
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(img, 0, 0, this);
    }
    
    public void show(JFrame frame, int timeInMillis) {
        setVisible(true);
        new Timer().schedule(new FrameStarter(this, frame), timeInMillis);
    }
    
    class FrameStarter extends TimerTask {
        
        private JDialog splash;
        private JFrame frame;
        
        public FrameStarter(JDialog splash, JFrame frame) {
            this.splash = splash;
            this.frame = frame;
        }
        public void run() {
            // kill splash
            splash.setVisible(false);
            splash.dispose();
            
            //start main window
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
        }
        
    }
}