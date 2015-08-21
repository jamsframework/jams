/*
 * MathematicalCalculations.java
 *
 * Created on 24. November 2005, 12:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.unijena.j2k.mathematicalCalculations;

import jams.JAMS;

/**
 *
 * @author c0krpe
 */
public class MathematicalCalculations {
    
    /** Creates a new instance of MathematicalCalculations */
    public MathematicalCalculations() {
    }
    
    /**
     * trims a double value to a user defined number of decimals with precise rounding
     * @param val the value to be trimmed
     * @param prec the number of decimals
     * @return the trimmed value
     */
    public static double trim(double val, int prec){
        String zeros = "0.";
        for(int i = 0; i < prec; i++)
            zeros = zeros + "0";
        java.text.DecimalFormat formatter = new java.text.DecimalFormat(zeros);
        java.text.DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dfs);
        String output = formatter.format(val);
        try{
            return Double.parseDouble(output);
        }catch(NumberFormatException nof){
            System.err.println("Number Format Exception: " + nof + " because of " + val);
            return JAMS.getMissingDataValue();
        }
    }
    
    /**
     * converts angles in degree to radians
     * @param angleDeg the angle in degree
     * @return the angle in radians
     */
    public static double deg2rad(double angleDeg) {
        double rad = (Math.PI / 180) * angleDeg;
        return rad;
    }
    
    /**
     * converts angles in radians to degree
     * @param angleRad the angle in radians
     * @return the angle in degree
     */
    public static double rad2deg(double angleRad) {
        double deg = (180 / Math.PI) * angleRad;
        return deg;
    }

    /**
     * Round a double value to a specified number of decimal
     * places.
     *
     * @param val the value to be rounded.
     * @param places the number of decimal places to round to.
     * @return rounded to places decimal places.
     *
     */
    public static double round(double val, int places) {
        long factor = (long) Math.pow(10, places);
        long tmp = Math.round(val * factor);
        return (double) tmp / factor;
    }

    /**
     * Round a float value to a specified number of decimal
     * places.
     *
     * @param val the value to be rounded.
     * @param places the number of decimal places to round to.
     * @return rounded to places decimal places.
     */
    public static float round(float val, int places) {
        return (float) round((double) val, places);
    }
    
}
