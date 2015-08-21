package radiation;

import java.util.Calendar;
import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates daily solar radiation")
@Author
    (name= "Peter Krause")
@Keywords
    ("Radiation, J2000")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/radiation/CalcDailySolarRadiation.java $")
@VersionInfo
    ("$Id: CalcDailySolarRadiation.java 978 2010-02-12 15:21:08Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcDailySolarRadiation  {
    
    @Description("Angstrom factor a")
    @Role(PARAMETER)
    @In public double angstrom_a;

    @Description("Angstrom factor b")
    @Role(PARAMETER)
    @In public double angstrom_b;
    
    @Description("Time")
    @In public java.util.Calendar time;

    @Description("sunshine hours")
    @Unit("h/d")
    @In public double sunh;

    @Description("slope aspect correction factor")
    @In public double actSlAsCf;
    
    @Description("latitude")
    @Unit("deg")
    @In public double latitude;
    
    @Description("daily extraterrestic radiation")
    @Unit("MJ/mï¿½?ï¿½d")
    @In public double actExtRad;
    
    @Description("daily solar radiation")
    @Unit("MJ/m2/day")
    @Out public double solRad;

    @Description("daily solar radiation")
    @Unit("MJ/m2/day")
    @Out public double sunhmax;
    
    @Execute
    public void execute() {
        int julDay = time.get(Calendar.DAY_OF_YEAR);

        double declination = Solrad.sunDeclination(julDay);
        double latRad = MathCalc.rad(latitude);
        double sunsetHourAngle = DailySolrad.sunsetHourAngle(latRad, declination);
        double maximumSunshine = DailySolrad.maxSunshineHours(sunsetHourAngle);

        double solarRadiation = Solrad.solarRadiation(sunh, maximumSunshine, actExtRad, angstrom_a, angstrom_b);
        
        //considering slope and aspect
        solRad = solarRadiation * actSlAsCf;
        sunhmax = maximumSunshine;
    }
}
