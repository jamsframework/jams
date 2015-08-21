/*
 * MathematicalCalculations.java
 *
 * Created on 24. November 2005, 12:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

/**
 *
 * @author c0krpe
 */
public class MathCalc {

    static final double PI_180 = Math.PI / 180;
    static final double _180_PI = 180 / Math.PI;
    
    /** Creates a new instance of MathematicalCalculations */
    private MathCalc() {
    }
    
    /**
     * converts angles in degree to radians
     * @param deg the angle in degree
     * @return the angle in radians
     */
    public static double rad(double deg) {
        return  PI_180 * deg;
    }
    
    /**
     * converts angles in radians to degree
     * @param rad the angle in radians
     * @return the angle in degree
     */
    public static double deg(double rad) {
        return _180_PI * rad;
    }
}
