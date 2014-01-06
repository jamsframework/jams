/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.worldwind.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class ColorRampPanel extends JPanel {

    private final ColorRamp colorRamp;
    
    public ColorRampPanel(ColorRamp r) {
        this.colorRamp = r;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int startx, xsteps;
        xsteps = this.getWidth() / colorRamp.getSteps();
        startx = 0;
        for(int i = 0; i< colorRamp.getSteps();i++) {
            g.setColor(this.colorRamp.getColorRamp().get(i));
            g.fillRect(startx, 0, xsteps, this.getHeight());
            g.setColor(Color.black);
            g.drawLine(startx, 0, startx, this.getHeight());
            //System.out.println("X: " + startx + " XSTEPS: " + xsteps);
            startx += xsteps;
        }
    }
}
