/*
 * CalcSlopeAspectCorrectionFactor.java
 *
 * Created on 24. November 2005, 11:58
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
public class Geo {
    
    /** Creates a new instance of CalcSlopeAspectCorrectionFactor */
    private Geo() {
    }
    
    public static double slopeAspectCorrFactor(int julDay, double latitude, double slope, double aspect){
        
        double latRad = MathCalc.rad(latitude);
        double convAsp = 180 - aspect;
        double aspRad = MathCalc.rad(convAsp);
        double slopeRad = MathCalc.rad(slope);
        double declRad = Solrad.sunDeclination(julDay);

        double sin_declRad = Math.sin(declRad);
        double cos_declRad = Math.cos(declRad);
        double sin_latRad = Math.sin(latRad);
        double cos_latRad = Math.cos(latRad);
        double cos_aspRad = Math.cos(aspRad);
        double sin_slopeRad = Math.sin(slopeRad);
        double cos_slopeRad = Math.cos(slopeRad);

        double sloped = (sin_declRad * sin_latRad * cos_slopeRad)
         - (sin_declRad * cos_latRad * sin_slopeRad * cos_aspRad)
         + (cos_declRad * cos_latRad * cos_slopeRad)
         + (cos_declRad * sin_latRad * sin_slopeRad * cos_aspRad);

        double horizontal = (sin_declRad * sin_latRad + cos_declRad * cos_latRad);
        double slopeAspect = sloped / horizontal;

        if(slopeAspect < 0) {
            slopeAspect = 0;
        }
        return slopeAspect;
    }
    
}
