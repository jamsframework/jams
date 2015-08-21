package regionalization;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Regionalization.")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Regionalization")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/regionalization/Regionalization1.java $")
@VersionInfo
    ("$Id: Regionalization1.java 928 2010-02-09 17:53:39Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class Regionalization1  {
    
    @Description("Array of data values for current time step")
    @In public double[] dataArray;
    
    @Description("Regression coefficients")
    @In public double[] regCoeff;
    
    @Description("Array of station elevations")
    @In public double[] statElevation;
    
    @Description("Array position of weights")
    @In public int[] statOrder;
    
    @Description("Array of station's weights")
    @In public double[] statWeights;
    
    @Description("Attribute name y coordinate")
    @Unit("hru")
    @Out public double dataValue;
    
    @Description("Attribute name elevation")
    @In public double entityElevation;
    
    @Description("Apply elevation correction to measured data")
    @In public int elevationCorrection;
    
    @Description("Minimum rï¿½?ï¿½ value for elevation correction application")
    @In public double rsqThreshold;
    
    @Description("Absolute possible minimum value for data set")
    @In public double fixedMinimum;
    
    int nidw = 1;
    static final double NODATA = -9999;

    @Execute
    public void execute() {

        double gradient = regCoeff[1];
        double rsq = regCoeff[2];

        double[] sourceElevations = statElevation;
        double[] sourceData = dataArray;
        double[] sourceWeights = statWeights;
        double targetElevation = entityElevation;

        double value = 0;
        double deltaElev = 0;

        double[] data = new double[nidw];
        double[] weights = new double[nidw];
        double[] elev = new double[nidw];
        
        //make sure that the arrays are intialized with 0s
        for(int i = 0; i < nidw;i++){
            data[i] = 0;
            weights[i] = 0;
            elev[i] = 0;
        }

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!

        //Retreiving data, elevations and weights
        int[] wA  = statOrder;
        int counter = 0;
        int element = counter;
        boolean cont = true;
        boolean valid = false;

        while(counter < nidw && cont){
            int t = wA[element];
            //check if data is valid or no data
            if(sourceData[t] == NODATA){
                element++;
                if(element >= wA.length){
                    System.out.println("BREAK1: too less data NIDW had been reduced!");
                    cont = false;
                    //value = NODATA;
                } else{
                    t = wA[element];
                }
            } else{
                valid = true;
                data[counter] = sourceData[t];
                weights[counter] = sourceWeights[t];
                elev[counter] = sourceElevations[t];
                counter++;
                element++;
                if(element >= wA.length){
//                    if(element <= nIDW)
//                        System.out.println("NIDW has been reduced, because of too less valid data!");
                    cont = false;
                }
            }
        }
        
        //normalising weights
        double weightsum = 0;
        for(int i = 0; i < counter; i++) {
            weightsum += weights[i];
        }
        for(int i = 0; i < counter; i++) {
            weights[i] /= weightsum;
        }

        if (valid) {
            for (int i = 0; i < counter; i++) {
                if ((rsq >= rsqThreshold) && (elevationCorrection == 1)) {      //Elevation correction is applied
                    deltaElev = targetElevation - elev[i];                      //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + data[i]) * weights[i]);
                    //checking for minimum
                    if (tVal < fixedMinimum) {
                        tVal = fixedMinimum;
                    }
                    value += tVal;
                } else { //No elevation correction
                    value += (data[i] * weights[i]);
                }
            }
        } else {
            value = NODATA;
        }
        dataValue = value;
    }
}
