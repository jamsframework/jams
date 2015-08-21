package climate;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.Regression;

@Description
    ("Component summary info ...")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("I/O")
@VersionInfo
    ("$Id: CalcAbsoluteHumidity.java 994 2010-02-19 20:44:19Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/climate/CalcAbsoluteHumidity.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcAbsoluteHumidity  {
        
    @Description("the relative humidity values")
    @In public double[] rhum;

    @Description("temperature for the computation")
    @In public double[] temperature;

    @Description("absolute humidity values")
    @Out public double[] ahum;
    
    @Description("Array of temperature station elevations")
    @In public double[] tempElevation;

    @Description("Array of temperature station's x coordinate")
    @In public double[] tempXCoord;

    @Description("Array of temperature station's y coordinate")
    @In public double[] tempYCoord;

    @Description("Array of rhum station elevations")
    @In public double[] rhumElevation;

    @Description("Array of rhum station's x coordinate")
    @In public double[] rhumXCoord;

    @Description("Array of rhum station's y coordinate")
    @In public double[] rhumYCoord;

    @Description("rsqr for ahum stations")
    @Out public double[] regCoeffAhum;
    
    @Execute
    public void execute() {
        // TODO
//        double[] rhum =  Arrays.toDoubleArray(this.rhum.getStore());
//        double[] temperature = Arrays.toDoubleArray(this.temperature.getStore());
        
        double[] ahum = new double[rhum.length];
        double[] rhumElev = new double[rhum.length];
        double[] rhumX = new double[rhum.length];
        double[] rhumY = new double[rhum.length];
        
        double[] tempElev = new double[temperature.length];
        double[] tempX = new double[temperature.length];
        double[] tempY = new double[temperature.length];
        
        //parameterization of rhum stations
        for(int i = 0; i < rhum.length; i++){
            rhumElev[i] = this.rhumElevation[i];
            rhumX[i] = this.rhumXCoord[i];
            rhumY[i] = this.rhumYCoord[i];
        }
        
        //parameterization of temp stations
        for(int i = 0; i < temperature.length; i++){
            tempElev[i] = this.tempElevation[i];
            tempX[i] = this.tempXCoord[i];
            tempY[i] = this.tempYCoord[i];
        }
        
        //temperature for each rhum station
        double rhumTemp;
        for (int r = 0; r < ahum.length; r++) {
            if(rhum[r] > 0){
                rhumTemp = 0;
                double absDist = -1;
                int t = 0;
                while(absDist != 0 && t < temperature.length){
                    absDist = (tempX[t] - rhumX[r]) - (tempY[t] - rhumY[r]) - (tempElev[t] - rhumElev[r]);
                    t++;
                }
                rhumTemp = temperature[t-1];
                if(rhumTemp > -9999){
                    //calculate saturation vapour pressure
                    double est = 6.11 * Math.exp((17.62*rhumTemp)/(243.12+rhumTemp));
                    
                    //compute maximum humidity
                    double maxHum = est * 216.7 /(rhumTemp + 273.15);
                    
                    //compute absolute humidity
                    ahum[r] = maxHum * (rhum[r] / 100.);
                } else{
                    ahum[r] = -9999;
                }
            } else{
                ahum[r] = -9999;
            }
        }

        // TODO this block has unknown calls
//        if (this.ahum.getDim(0) != ahum.length)
//            this.ahum = new double[ahum.length];
//
//        //this.ahum = new double[ahum.length];
//        Arrays.fromDoubleArray(ahum, this.ahum.getStore());
//
//        double[] rhumElevation = Arrays.toDoubleArray(this.rhumElevation.getStore());
//        double[] reg = Regression.calcLinReg(rhumElevation, ahum);
//
//        if (regCoeffAhum.getDim(0) != reg.length)
//            this.regCoeffAhum = new double[reg.length];
//        Arrays.fromDoubleArray(reg, this.regCoeffAhum.getStore());
    }
    
}
