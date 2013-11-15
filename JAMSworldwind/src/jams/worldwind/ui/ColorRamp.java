/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui;

import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class ColorRamp {

    private final Color startColor;
    private final Color endColor;
    private int steps;
    private final ArrayList<Color> colorRamp;

    public ColorRamp(Color start, Color end, int steps) {
        this.startColor = start;
        this.endColor = end;
        this.steps = steps - 1;
        this.colorRamp = new ArrayList<>(this.steps);
        this.calculateRamp();
    }

    private void calculateRamp() {
        int rSteps = (this.startColor.getRed() - this.endColor.getRed()) / this.steps;
        int gSteps = (this.startColor.getGreen() - this.endColor.getGreen()) / this.steps;
        int bSteps = (this.startColor.getBlue() - this.endColor.getBlue()) / this.steps;
        this.colorRamp.add(startColor);
        int red, green, blue;
        for (int i = 1; i < this.steps; i++) {
            red = rSteps<0 ? this.endColor.getRed()+(this.steps-i)*rSteps : this.startColor.getRed()-i*rSteps;
            green = gSteps<0 ? this.endColor.getGreen()+(this.steps-i)*gSteps : this.startColor.getGreen()-i*gSteps;
            blue = bSteps<0 ? this.endColor.getBlue()+(this.steps-i)*bSteps : this.startColor.getBlue()-i*bSteps;
            this.colorRamp.add(new Color(red,green,blue));
        }
        this.colorRamp.add(endColor);
        System.out.println(this.colorRamp);
    }

    public Color getStartColor() {
        return startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    public int getSteps() {
        return steps;
    }

    public ArrayList<Color> getColorRamp() {
        return colorRamp;
    }
}
