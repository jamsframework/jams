package interception;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Calculates daily interception based on the Dickinson (1984) method")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Interception")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/interception/J2KProcessInterception.java $")
@VersionInfo
    ("$Id: J2KProcessInterception.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KProcessInterception  {
    
    @Description("Snow parameter TRS")
    @Role(PARAMETER)
    @In public double snow_trs;

    @Description("Snow parameter TRANS")
    @Role(PARAMETER)
    @In public double snow_trans;

    @Description("Interception parameter a_rain")
    @Role(PARAMETER)
    @In public double a_rain;

    @Description("Interception parameter a_snow")
    @Role(PARAMETER)
    @In public double a_snow;
    
    @Description("area")
    @In public double area;

    @Description("tmean")
    @In public double tmean;

    @Description("rain")
    @In public double rain;
    
    @Description("snow")
    @In public double snow;
    
    @Description("potET")
    @In public double potET;
    
    @Description("LAI")
    @In public double actLAI;
    
    @Description("state variable net-rain")
    @Out public double netRain;
    
    @Description("state variable netSnow")
    @Out public double netSnow;
    
    @Description("state variable throughfall")
    @Out public double throughfall;
    
    @Description("state variable dy-interception")
    @Out public double interception;
    
    @Description("state variable interception storage")
    @In @Out public double intercStorage;

    @Description("actET")
    @In @Out public double actET;
  
    @Execute
    public void execute() {
        throughfall = 0;
        interception = 0;

        double sum_precip = rain + snow;
        double deltaETP = potET - actET;
        
        double relRain, relSnow;
        if(sum_precip > 0){
            relRain = rain / sum_precip;
            relSnow = snow / sum_precip;
        } else{
            relRain = 1.0; //throughfall without precip is in general considered to be liquid
            relSnow = 0;
        }
        
        //determining if precip falls as rain or snow
        double alpha = 0;
        if(tmean < (snow_trs - snow_trans)){
            //alpha = alpha_snow;
            alpha = a_snow;
        } else {
            //alpha = alpha_rain;
            alpha = a_rain;
        }
        
        //determinining maximal interception capacity of actual day
        double maxIntcCap = (actLAI * alpha) * area;
        
        //if interception storage has changed from snow to rain then throughfall
        //occur because interception storage of antecedend day might be larger
        //then the maximum storage capacity of the actual time step.
        if(intercStorage > maxIntcCap){
            throughfall = intercStorage - maxIntcCap;
            intercStorage = maxIntcCap;
        }
        
        //determining the potential storage volume for daily Interception
        double deltaIntc = maxIntcCap - intercStorage;
        
        //reducing rain and filling of Interception storage
        if(deltaIntc > 0){
            //double save_rain = sum_precip;
            if(sum_precip > deltaIntc){
                intercStorage = maxIntcCap;
                sum_precip = sum_precip - deltaIntc;
                throughfall = throughfall + sum_precip;
                interception = deltaIntc;
                deltaIntc = 0;
            } else{
                intercStorage = (intercStorage + sum_precip);
                interception = sum_precip;
                sum_precip = 0;
            }
        } else{
            throughfall = throughfall + sum_precip;
        }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration
        if(deltaETP > 0){
            if(intercStorage > deltaETP){
                intercStorage = intercStorage - deltaETP;
                actET += deltaETP;
                deltaETP = 0;
                
            } else{
                deltaETP -=  intercStorage;
                actET += (potET - deltaETP);
                intercStorage = 0;
            }
        } else{
            actET = deltaETP;
        }
        
        netRain = throughfall * relRain;
        netSnow = throughfall * relSnow;
    }
}
