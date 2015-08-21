package groundwater;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Initialize groundwater processes")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Groundwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/groundwater/InitJ2KProcessGroundWater.java $")
@VersionInfo
    ("$Id: InitJ2KProcessGroundWater.java 893 2010-01-29 16:06:46Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class InitJ2KProcessGroundWater  {

    @Description("relative initial RG1 storage")
    @Role(PARAMETER)
    @In public double initRG1;

    @Description("relative initial RG2 storage")
    @Role(PARAMETER)
    @In public double initRG2;

    @Description("")
    @In public double RG1_max;

    @Description("")
    @In public double RG2_max;
    
    @Description("area")
    @In public double area;
    
    @Description("maximum RG1 storage")
    @Out public double maxRG1;
    
    @Description("maximum RG2 storage")
    @Out public double maxRG2;
    
    @Description("actual RG1 storage")
    @Out public double actRG1;
    
    @Description("actual RG2 storage")
    @Out public double actRG2;
    
    @Execute
    public void execute() {
        maxRG1 = RG1_max * area;
        maxRG2 = RG2_max * area;
        actRG1 = maxRG1 * initRG1;
        actRG2 = maxRG2 * initRG2;
    }
    
}
