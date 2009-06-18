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
package jams.gui;

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
public class JAMSSplash {

    private Image img;

    private int width, height;

    private JDialog dlg;

    /**
     * Creates a new splash dialog
     */
    public JAMSSplash() {
        this(0, 0);
    }

    /**
     * Creates a new splash dialog width given width/height
     * @param width dialog width
     * @param height dialog height
     */
    public JAMSSplash(int width, int height) {
        this.width = 0;
        this.height = 0;
    }

    public void show(JFrame frame, int timeInMillis) {
        frame.setVisible(true);
    }

    /**
     *
     * @param frame Frame to show after splash disappears
     * @param timeInMillis Time to show the splash
     */
    public void show_(JFrame frame, int timeInMillis) {

        if (timeInMillis == 0) {
            frame.setVisible(true);
            return;
        }

        dlg = new JDialog() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(img, 0, 0, this);
            }
        };

        dlg.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.setTitle("JAMS");
        dlg.setUndecorated(true);
        dlg.setFocusable(false);
        dlg.setAlwaysOnTop(true);

        URL imgURL = ClassLoader.getSystemResource("resources/images/JAMSsplash.png");
        if (img == null && imgURL != null) {
            img = new ImageIcon(imgURL).getImage();//.getScaledInstance(x, y, Image.SCALE_SMOOTH);
        }
        if (width == 0) {
            width = img.getWidth(null);
            height = img.getHeight(null);
        } else {
            img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        dlg.setSize(width, height);

        Dimension d2 = new java.awt.Dimension(width, height);
        dlg.setPreferredSize(d2);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        dlg.setLocation(d.width / 2 - width / 2, d.height / 2 - height / 2);

        dlg.setVisible(true);
        new Timer().schedule(new FrameStarter(dlg, frame), timeInMillis);
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
