package potet;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates PET using the Penman-Monteith method")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/potet/PenmanMonteith.java $")
@VersionInfo
    ("$Id: PenmanMonteith.java 970 2010-02-11 20:53:38Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class PenmanMonteith  {
    
    final static double CP  = 1.031E-3;
    final static double RSS = 150;
    
    @Description("temporal resolution")
    @Unit("d or h")
    @Role(PARAMETER)
    @In public String tempRes;
    
    @Description("wind")
    @In public double wind;
    
    @Description("mean temperature")
    @In public double tmean;

    @Description("relative humidity")
    @In public double rhum;
    
    @Description("net radiation")
    @In public double netRad;

    @Description("state variable rsc0")
    @In public double actRsc0;

    @Description("attribute elevation")
    @In public double elevation;

    @Description("attribute area")
    @In public double area;

    @Description("attribute LAI")
    @In public double actLAI;

    @Description("effective height")
    @In public double actEffH;

    @Description("potential ET")
    @Unit("mm/ timeUnit")
    @Out public double potET;

    @Description("potential ET")
    @Unit("mm/ timeUnit")
    @Out public double actET;

    @Description("rs")
    @Out public double rs;

    @Description("ra")
    @Out public double ra;

    double tempFactor = 0;

    @Execute
    public void execute() {
        if (tempFactor == 0) {
            if (tempRes.equals("d")) {
                tempFactor = 86400;
            } else if (tempRes.equals("h")) {
                tempFactor = 3600;
            }
        }
        
        double abs_temp = Climate.absTemp(tmean, "C");
        double delta_s = Climate.slopeOfSaturationPressureCurve(tmean);
        double pz = Climate.atmosphericPressure(elevation, abs_temp);
        double est = Climate.saturationVapourPressure(tmean);
        double ea = Climate.vapourPressure(rhum, est);
        double latH = Climate.latentHeatOfVaporization(tmean);
        double psy = Climate.psyConst(pz, latH);

        double G = 0.1 * netRad; // calc_groundHeatFlux
        double vT = Climate.virtualTemperature(abs_temp, pz, ea);
        double pa = Climate.airDensityAtConstantPressure(vT, pz);
        
        rs = calcRs(actLAI, actRsc0, RSS);
        ra = calcRa(actEffH, wind);
        
        double Letp = calcETAllen(delta_s, netRad, G, pa, CP, est, ea, ra, rs, psy, tempFactor);
        potET = Letp / latH;
        
        // mm -> liter
        potET *= area;
        
        //avoiding negative potETPs
        if(potET < 0){
            potET = 0;
        }
        actET = 0;   // reset actET
    }
    
    private static double calcETAllen(double ds, double netRad, double G, double pa,
            double CP, double est, double ea, double ra, double rs, double psy, double tempFactor){
        
        return (ds * (netRad - G) + ((pa * CP * (est-ea)/ra)*tempFactor)) / (ds + psy * (1+rs/ra));
    }
    
    private static double calcRa(double eff_height, double wind_speed){
        double ra;
        if(wind_speed <= 0) {
            wind_speed = 0.5;
        }
        if(eff_height < 10){
            //old equation, don't use this one
            //ra = (1.5 * Math.pow(Math.log(2/(0.125 * eff_height)),2)) / (Math.pow(0.41,2) * wind_speed);
            //J2K equation
            //ra = (4.72 * Math.pow(Math.log(2.0 / (0.125 * eff_height)),2)) / (1 + 0.54 * wind_speed);
            //LARSIM equation
            //ra = (6.25 / wind_speed) * Math.pow(Math.log(2 / (0.1 * eff_height)), 2);
            ra = (9.5 / wind_speed) * Math.pow(Math.log(2 / (0.1 * eff_height)), 2);
        } else{
            //ra = 64 / (1+0.54*wind_speed);//(Math.pow(0.41,2) * wind_speed);
//            ra = 20 / (Math.pow(0.41, 2) * wind_speed);
            ra = 20 / (0.1681 * wind_speed);
        }
        return ra;
    }
    
    private static double calcRs(double LAI, double rsc, double rss){
        double A = Math.pow(0.7, LAI);
        return 1. / (((1-A) / rsc) + ((A / rss)));
    }
}
