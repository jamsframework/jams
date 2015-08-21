package radiation;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("CalcDailyNetRadiation")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Radiation")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/radiation/CalcDailyNetRadiationSolrad.java $")
@VersionInfo
    ("$Id: CalcDailyNetRadiationSolrad.java 970 2010-02-11 20:53:38Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcDailyNetRadiationSolrad  {
    
//    @Description("sunshine hours [h/d]")
//    @In
//    public double sunh;
    
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
    
//    @Description("latitude [deg]")
//    @In
//    public double latitude;
//    
//    @Description("Time")
//    @In
//    public java.util.Calendar time;

    @Description("daily net radiation")
    @Unit("MJ/mï¿½?ï¿½")
    @Out public double netRad;
    
//     @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            update = JAMSVarDescription.UpdateType.RUN,
//            description = "daily shortwave radiation [MJ/mï¿½?ï¿½]"
//            )
//            public Attribute.Double swRad;
    @Description("daily shortwave radiation")
    @Unit("MJ/mï¿½?ï¿½")
    @Out public double swRad;
    
//    
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            update = JAMSVarDescription.UpdateType.RUN,
//            description = "daily longwave radiation [MJ/mï¿½?ï¿½]"
//            )
//            public Attribute.Double lwRad;
    @Description("daily longwave radiation")
    @Unit("MJ/mï¿½?ï¿½")
    @Out public double lwRad;
//    
//    @JAMSVarDescription(
//            access = JAMSVarDescription.AccessType.WRITE,
//            update = JAMSVarDescription.UpdateType.RUN,
//            description = "daily net radiation for refET [MJ/mï¿½?ï¿½]"
//            )
//            public Attribute.Double refETNetRad;
    @Description("daily net radiation for refET")
    @Unit("MJ/mï¿½?ï¿½")
    @Out public double refETNetRad;
    
    @Execute public void execute() {        
        double elev = elevation;
        double temp = tmean;
        double rh   = rhum;
        double sR   = solRad;
        double alb  = albedo;
        double extraTerrestialRad = extRad;

        double sat_vapour_pressure = Climate.saturationVapourPressure(temp);
        double act_vapour_pressure = Climate.vapourPressure(rh, sat_vapour_pressure);

        double clearSkyRad = Solrad.clearSkySolarRadiation(elev, extraTerrestialRad);
        double netSWRadiation = Solrad.netShortwaveRadiation(alb, sR);
        double netRefETSWRadiation = Solrad.netShortwaveRadiation(0.23, sR);
        double netLWRadiation = DailySolrad.dailyNetLongwaveRadiation(temp, act_vapour_pressure, sR, clearSkyRad, false);

        double nR_norm = Solrad.netRadiation(netSWRadiation, netLWRadiation);
        double nR_refET = Solrad.netRadiation(netRefETSWRadiation, netLWRadiation);

        this.netRad = nR_norm;
        this.refETNetRad = nR_refET;
        this.swRad = netSWRadiation;
        this.lwRad = netLWRadiation;
    }
}
