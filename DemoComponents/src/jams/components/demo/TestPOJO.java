/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.demo;

import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TestPOJO {
    GregorianCalendar cal;
    private double x;

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     * @throws InterruptedException
     */
    public void setX(double x) throws InterruptedException {
        Thread.sleep(Math.round(x));
        this.x = x;
    }
}
