package regionalization;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates weights for the regionalisation procedure.")
@Author
    (name= "Peter Krause")
@Keywords
    ("Regionalization")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/regionalization/CalcNidwWeights.java $")
@VersionInfo
    ("$Id: CalcNidwWeights.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcNidwWeights  {
    
    @Description("Entity x-coordinate")
    @In public double x;
    
    @Description("Entity y-coordinate")
    @In public double y;
    
    @Description("Array of station's x coordinates")
    @In public double[] statX;
    
    @Description("Array of station's y coordinates")
    @In public double[] statY;
    
    @Description("Number of IDW stations")
    @In public int nidw;
    
    @Description("Power of IDW function")
    @In public double pidw;
    
    @Description("Weights for IDW part of regionalisation.")
    @Out public double[] statWeights;
    
    @Execute
    public void execute() {
        statWeights = IDW.calcNidwWeights(x, y, statX, statY, pidw, nidw);
    }
}
