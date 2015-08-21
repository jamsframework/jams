package radiation;

import java.util.Calendar;
import java.util.GregorianCalendar;
import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates extraterrestrial radiation")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Radiation")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/radiation/CalcExtraterrRadiation.java $")
@VersionInfo
    ("$Id: CalcExtraterrRadiation.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcExtraterrRadiation  {

    @Description("temporal resolution")
    @Unit("d | h")
    @Role(PARAMETER)
    @In public String tempRes;

    @Description("location from Greenwich")
    @Unit("w | e")
    @Role(PARAMETER)
    @In public String locGrw;
    
    @Description("longitude of time zone")
    @Unit("deg")
    @Role(PARAMETER)
    @In public double longTZ;

    @Description("entity latidute")
    @Unit("deg")
    @In public double latitude;
    
    @Description("entity longitude")
    @Unit("deg")
    @In public double longitude;
    
    @Description("extraterrestric radiation of each time step of the year")
    @Unit("MJ/m2 timeUnit")
    @Out public double[] extRadArray;
    
    @Execute
    public void execute() {
        
        if(tempRes.equals("d")) {
            extRadArray = new double[366];
        } else if(tempRes.equals("h")) {
            extRadArray = new double[366*24];
        }
        
        if(locGrw.equals("e")){
            longitude = 360 - longitude;
            longTZ = 360 - longTZ;
        }
        
        double latRad = MathCalc.rad(latitude);
        GregorianCalendar cal = new GregorianCalendar(2000, 00, 01);
        for(int i = 0; i < 366; i++){
            int hour = 0;
            int jDay = i+1;
            double declination = Solrad.sunDeclination(jDay);
            double solarConstant = Solrad.solarConstant(jDay);
            double invRelDistEarthSun = Solrad.inverseRelativeDistanceEarthSun(jDay);
            
            if(tempRes.equals("d")) {
                double sunsetHourAngle = DailySolrad.sunsetHourAngle(latRad, declination);
                extRadArray[i] = DailySolrad.extraTerrestrialRadiation(solarConstant, invRelDistEarthSun, sunsetHourAngle, latRad, declination);
            } else if(tempRes.equals("h")){
                while(hour < 24){
                    double midTimeHourAngle = HourlySolrad.midTimeHourAngle(cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE), jDay, longitude, longTZ, false);
                    
                    double startTimeHourAngle = HourlySolrad.startTimeHourAngle(midTimeHourAngle);
                    double endTimeHourAngle = HourlySolrad.endTimeHourAngle(midTimeHourAngle);
                    extRadArray[i] = HourlySolrad.hourlyExtraterrestrialRadiation(solarConstant,
                            invRelDistEarthSun, startTimeHourAngle, endTimeHourAngle, latRad, declination);
                    hour++;
                    cal.add(Calendar.HOUR_OF_DAY, 1);
                }
            }
        }
    }
}
