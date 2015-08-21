package regionalization;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Climate data regionalization")
@Author
    (name= "Sven Krahlisch")
@Keywords
    ("Regionalization")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/regionalization/Regionalization.java $")
@VersionInfo
    ("$Id: Regionalization.java 893 2010-01-29 16:06:46Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class Regionalization  {
    
    @Description("Array of data values for current time step")
    @In public double[] dataArray;
    
    @Description("Regression coefficients")
    @In public double[] regCoeff;
    
    @Description("Array of station elevations")
    @In public double[] statElevation;
    
    @Description("Array of station's x coordinates")
    @In public double[] statX;
    
    @Description("Array of station's y coordinates")
    @In public double[] statY;
    
    @Description("Array of station's weights")
    @In public double[] statWeights;
    
    @Description("Attribute name elevation")
    @In public double entityElevation;
    
    @Description("Apply elevation correction to measured data")
    @Role(PARAMETER)
    @In public int elevationCorrection;

    @Description("Minimum value for elevation correction application")
    @Role(PARAMETER)
    @In public double rsqThreshold;
    
    @Description("Attribute name y coordinate")
    @Unit("hru")
    @Out public double dataValue;

    @Execute
    public void execute() {
        double gradient = regCoeff[1];
        double rsq = regCoeff[2];
        dataValue = 0;

        if ((rsq >= rsqThreshold) && (elevationCorrection == 1)) {
            //Elevation correction is applied
            for (int i = 0; i < statElevation.length; i++) {
                //Elevation difference between unit and Station
                double deltaElev = entityElevation - statElevation[i];  
                dataValue += (deltaElev * gradient + dataArray[i]) * statWeights[i];
            }
        } else {
            //No elevation correction
            for (int i = 0; i < statElevation.length; i++) {
                dataValue += dataArray[i] * statWeights[i];
            }
        }
    }
}
