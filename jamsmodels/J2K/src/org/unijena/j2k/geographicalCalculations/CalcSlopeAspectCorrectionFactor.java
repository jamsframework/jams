/*
 * CalcSlopeAspectCorrectionFactor.java
 *
 * Created on 24. November 2005, 11:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.unijena.j2k.geographicalCalculations;

/**
 *
 * @author c0krpe
 */
public class CalcSlopeAspectCorrectionFactor {
    
    /** Creates a new instance of CalcSlopeAspectCorrectionFactor */
    public CalcSlopeAspectCorrectionFactor() {
    }
    
    public static double calc_slopeAspectCorrectionFactor(int julDay, double latitude, double slope, double aspect){
        double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(latitude);
        double convAsp = 180 - aspect;
        double aspRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(convAsp);
        double slopeRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(slope);
        double declRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
        
        double sloped = (Math.sin(declRad) * Math.sin(latRad) * Math.cos(slopeRad))
        - (Math.sin(declRad) * Math.cos(latRad) * Math.sin(slopeRad) * Math.cos(aspRad))
        + (Math.cos(declRad) * Math.cos(latRad) * Math.cos(slopeRad) * 1)
        + (Math.cos(declRad) * Math.sin(latRad) * Math.sin(slopeRad) * Math.cos(aspRad) * 1)
        + (Math.cos(declRad) * Math.sin(slopeRad) * Math.sin(aspRad) * 0);
        double horizontal = (Math.sin(declRad) * Math.sin(latRad) + Math.cos(declRad) * Math.cos(latRad) * 1);
        double slopeAspect = sloped / horizontal;
        
        if(slopeAspect < 0) //can happen
            slopeAspect = 0;
          
        return slopeAspect;        
    }
    
}
