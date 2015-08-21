package climate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Correction for daily measured rainfall using the Richter (1985) method")
@Author
    (name= "Peter Krause ")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/climate/RainCorrectionRichter.java $")
@VersionInfo
    ("$Id: RainCorrectionRichter.java 961 2010-02-11 20:35:32Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class RainCorrectionRichter  {

    private static final Logger log = Logger.getLogger("oms3.model." +
            RainCorrectionRichter.class.getSimpleName());
    
    @Description("Time")
    @In public java.util.Calendar time;

    @Description("the precip values")
    @In public double[] precip;
    
    @Description("temperature for the correction function.")
    @In public double[] temperature;
  
    @Description("number of temperature station for IDW")
    @Role(PARAMETER)
    @In public int tempNIDW;

    @Description("power for IDW function")
    @Role(PARAMETER)
    @In public double pIDW;
    
    @Description("regression threshold")
    @Role(PARAMETER)
    @In public double regThres;
    
    @Description("snow_trs")
    @Role(PARAMETER)
    @In public double snow_trs;
    
    @Description("snow_trans")
    @Role(PARAMETER)
    @In public double snow_trans;

    @Description("Array of temperature station elevations")
    @In public double[] tempElevation;

    @Description("Array of temperature station's x coordinate")
    @In public double[] tempXCoord;

    @Description("Array of temperature station's y coordinate")
    @In public double[] tempYCoord;

    @Description("Regression coefficients for temperature")
    @In public double[] tempRegCoeff;

    @Description("Array of precip station elevations")
    @In public double[] rainElevation;

    @Description("Array of precip station's x coordinate")
    @In public double[] rainXCoord;

    @Description("Array of precip station's y coordinate")
    @In public double[] rainYCoord;

    @Description("corrected precip values.")
    @Out public double[] rcorr;
    
    private final int NODATA = -9999;
    
    @Execute
    public void execute() {
        if (rcorr == null) {
            rcorr = new double[precip.length];
        }

        double[] rainTemp = new double[precip.length];
        double[] rainElev = new double[precip.length];
        double[] rainX = new double[precip.length];
        double[] rainY = new double[precip.length];

        double[] statWeights = new double[temperature.length];

        //parameterization of rain stations
        for (int i = 0; i < precip.length; i++) {
            rainElev[i] = rainElevation[i];
            rainX[i] = rainXCoord[i];
            rainY[i] = rainYCoord[i];
        }
        
        double rsq = tempRegCoeff[2];
        double grad = tempRegCoeff[1];
      
        //temperature for each rain station
         //temperature for each rain station
        for (int r = 0; r < rcorr.length; r++) {
            rainTemp[r] = 0;
            double[] dist = IDW.calcDistances(rainX[r], rainY[r], tempXCoord, tempYCoord, pIDW);
            statWeights = IDW.calcWeights(dist, temperature);
            int[] wArray = IDW.computeWeightArray(statWeights);

 //selecting the nidw closest temperature stations and avoiding no data values
            int counter = 0;
            boolean cont = true;
            double[] data = new double[tempNIDW];
            double[] weights = new double[tempNIDW];
            double[] elev = new double[tempNIDW];
            int element = counter;
            while (counter < tempNIDW && cont) {
                int t = wArray[element];
                //check if data is valid or no data
                if (temperature[t] == NODATA) {
                    element++;
                    if (element >= wArray.length) {
                        System.out.println("BREAK1: too less data NIDW had been reduced!");
                        cont = false;
                    } else {
                        t = wArray[element];
                    }
                } else {
                    data[counter] = temperature[t];
                    elev[counter] = tempElevation[t];
                    weights[counter] = statWeights[t];
                    counter++;
                    element++;
                }
            }
            //normalising weights
            double weightsum = 0;
            for (int i = 0; i < counter; i++) {
                weightsum += weights[i];
            }
            for (int i = 0; i < counter; i++) {
                weights[i] = weights[i] / weightsum;
            }
            for (int t = 0; t < tempNIDW; t++) {
                if (rsq >= regThres) {
                    //Elevation correction is applied
                    double deltaElev = rainElevation[r] - elev[t];  //Elevation difference between unit and Station
                    rainTemp[r] += ((deltaElev * grad + data[t]) * weights[t]);
                } else {
                    //No elevation correction
                    rainTemp[r] += (data[t] * weights[t]);
                }
            }
                
            //determine rain and snow amount of precip
            double pSnow = (snow_trs + snow_trans - rainTemp[r])/(2 * snow_trans);
            
            //fixing upper and lower bound for pSnow (has to be between 0 and 1
            if (pSnow > 1.0) {
                pSnow = 1.0;
            } else if (pSnow < 0) {
                pSnow = 0;
            }
            
            //dividing input precip into rain and snow
            double rain = (1 - pSnow) * precip[r];
            double snow = pSnow * precip[r];
            
            //Calculating relative Winderror acc to RICHTER 1995
            if(snow > 0){//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                if(snow <= 0.1) {
                    snow = snow + (snow * 0.938);
                } else{
                    double relSnow = 0.5319 * Math.pow(snow, -0.197);
                    snow = snow + (snow * relSnow);
                }
            }
            
            if(rain > 0){ //if(pSnow < 1.0){//
                if(rain < 0.1) {
                    rain += (rain * 0.492);
                } else {
                    rain += (rain * (0.1349 * Math.pow(rain, -0.494)));
                }
            }
            
            // Calculating error from evaporation and wetting acc. to Richter
            double wetErr = 0;
            if(precip[r] < 0.1) {
                wetErr = 0;
            } else {
                int mo = time.get(Calendar.MONTH);
                if(mo >= 4 && mo < 10){ //Summer half of the year
                    if(precip[r] >= 9.0) {
                        wetErr = 0.47;
                    } else {
                        wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                    }
                } else {   //Winter half of the year
                    if(precip[r] >= 9.0) {
                        wetErr = 0.3;
                    } else {
                        wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                    }
                }
            }
            //Calculating corrected rain_value
            if (precip[r] == NODATA) {
                rcorr[r] = NODATA;
            } else {
                rcorr[r] = rain + snow + wetErr;
            }
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("rcorr : " + Arrays.toString(rcorr));
        }
    }
}
