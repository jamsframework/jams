package potet;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates reference ET")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/potet/RefET.java $")
@VersionInfo
    ("$Id: RefET.java 970 2010-02-11 20:53:38Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class RefET  {
    
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
    
    @Description("attribute elevation")
    @In public double elevation;
    
    @Description("attribute area")
    @In public double area;
    
    @Description("potential refET")
    @Unit("mm/timeUnit")
    @Out public double refET;
    
    @Execute
    public void execute() {
        
        double abs_temp = Climate.absTemp(tmean, "C");
        double delta_s = Climate.slopeOfSaturationPressureCurve(tmean);
        double pz = Climate.atmosphericPressure(elevation, abs_temp);
        double est = Climate.saturationVapourPressure(tmean);
        double ea = Climate.vapourPressure(rhum, est);
        double latH = Climate.latentHeatOfVaporization(tmean);
        double psy = Climate.psyConst(pz, latH);
        double G = 0.1 * netRad; // groundHeatFlux
        
        double tempFactor = 0;
        
        if(tempRes.equals("d")) {
            tempFactor = 891;
        } else if(tempRes.equals("h")) {
            tempFactor = 37;
        } else {
            throw new RuntimeException(tempRes);
        }

        refET = (0.408 * delta_s *(netRad - G) + psy * (tempFactor/(tmean + 273)) * wind * (est - ea))
                  / (delta_s + psy * (1 + 0.34 * wind));
        
        // mm -> litre
        refET = refET * area;
        
        //avoiding negative potETPs
        if(refET < 0){
            refET = 0;
        }
    }
    
}
