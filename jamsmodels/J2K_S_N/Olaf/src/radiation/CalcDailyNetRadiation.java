package radiation;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates daily net radiation")
@Author
    (name= "Peter Krause")
@Keywords
    ("Radiation")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/radiation/CalcDailyNetRadiation.java $")
@VersionInfo
    ("$Id: CalcDailyNetRadiation.java 970 2010-02-11 20:53:38Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcDailyNetRadiation  {
    
    @Description("mean tempeature")
    @In public double tmean;

    @Description("relative humidity")
    @In public double rhum;
    
    @Description("solar radiation")
    @In public double extRad;
    
    @Description("solar radiation")
    @In public double solRad;
    
    @Description("albedo")
    @In public double albedo;
    
    @Description("albedo")
    @In public double elevation;
    
    @Description("daily net radiation")
    @Unit("MJ/m2")
    @Out public double netRad;
    
    @Execute
    public void execute() {
        double sat_vapour_pressure = Climate.saturationVapourPressure(tmean);
        double act_vapour_pressure = Climate.vapourPressure(rhum, sat_vapour_pressure);
        double clearSkyRad = Solrad.clearSkySolarRadiation(elevation, extRad);
        double netSWRadiation = Solrad.netShortwaveRadiation(albedo, solRad);
        double netLWRadiation = DailySolrad.dailyNetLongwaveRadiation(tmean, act_vapour_pressure, solRad, clearSkyRad, false);
        netRad = Solrad.netRadiation(netSWRadiation, netLWRadiation);
    }
}
