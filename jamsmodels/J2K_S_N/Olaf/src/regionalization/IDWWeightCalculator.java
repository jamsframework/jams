package regionalization;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates inverse distance weights for climate regionalisation")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Regionalization")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/regionalization/IDWWeightCalculator.java $")
@VersionInfo
    ("$Id: IDWWeightCalculator.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class IDWWeightCalculator  {
    
    @Description("Entity x-coordinate")
    @In public double x;
    
    @Description("Entity y-coordinate")
    @In public double y;
    
    @Description("Array of station's x coordinates")
    @In public double[] statX;
    
    @Description("Array of station's y coordinates")
    @In public double[] statY;
    
    @Description("Power of IDW function")
    @In public double pidw;
    
    @Description("weights for IDW part of regionalisation")
    @Out public double[] statWeights;
    
    @Description("position array to determine best weights")
    @Out public int[] wArray;
    
    @Description("DB function")
    @Role(PARAMETER)
    @In public boolean equalWeights;
    
    @Execute
    public void execute() {
        if(!equalWeights){
            double[] dist = IDW.calcDistances(x, y, statX, statY, pidw);
            statWeights = IDW.calcWeights(dist);
            wArray = IDW.computeWeightArray(statWeights);
        } else {
            int nstat = statX.length;
            statWeights = IDW.equalWeights(nstat);
            wArray = new int[nstat];
            for(int i = 0; i < nstat; i++) {
                wArray[i] = i;
            }
        }
    }
}
