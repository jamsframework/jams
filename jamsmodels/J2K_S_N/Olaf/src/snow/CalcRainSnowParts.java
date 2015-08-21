package snow;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Divides precipitation into rain and snow based on mean temperature")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Snow")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/snow/CalcRainSnowParts.java $")
@VersionInfo
    ("$Id: CalcRainSnowParts.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcRainSnowParts  {
    
    @Description("HRU attribute name area")
    @In public double area;

    @Description("Snow parameter TRS")
    @Role(PARAMETER)
    @In public double snow_trs;

    @Description("Snow parameter TRANS")
    @Role(PARAMETER)
    @In public double snow_trans;

    @Description("state variable min temperature or mean")
    @In public double tmin;

    @Description("state variable mean temperature")
    @In public double tmean;

    @Description("state variable precipitation")
    @In public double precip;

    @Description("state variable rain")
    @Out public double rain;

    @Description("state variable snow")
    @Out public double snow;
    
    @Execute
    public void execute() {
        double temp = (tmin + tmean) / 2.0;
        //determinining relative snow amount of total precip depending on temperature
        double pSnow = (snow_trs + snow_trans - temp) / (2 * snow_trans);
        
        //fixing upper and lower bound for pSnow (has to be between 0 and 1
        if(pSnow > 1.0) {
            pSnow = 1.0;
        } else if(pSnow < 0) {
            pSnow = 0;
        }
        
        //converting to absolute litres
        double precip_area = precip * area;
        if (precip_area < 0){
           precip_area = 0;
        }
        
        //dividing input precip into rain and snow
        rain = (1 - pSnow) * precip_area;
        snow = pSnow * precip_area;
    }
}
