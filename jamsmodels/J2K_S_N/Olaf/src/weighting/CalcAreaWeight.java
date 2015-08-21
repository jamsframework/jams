package weighting;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Calculates areal HRU weights")
@Keywords
    ("Utilities")
@Author
    (name= "Peter Krause, Sven Kralisch")
@VersionInfo
    ("$Id: CalcAreaWeight.java 957 2010-02-11 20:24:54Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/weighting/CalcAreaWeight.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class CalcAreaWeight  {
    
    @Description("basin area")
    @In public double basin_area;
    
    @Description("hru_area")
    @In public double hru_area;
    
    @Description("areaweight")
    @Out public double areaweight;
    
    @Execute
    public void execute() {
        areaweight = basin_area / hru_area;
    }
    
}
